/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.rmc.metadata;

import com.mgmtp.a12.dataservices.wcf.WorkspaceConverter;
import com.mgmtp.a12.dataservices.wcf.annotations.WcfConverter;
import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.dataservices.wcf.domain.internal.DefaultModelTuple;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.rmc.kernelextensions.ProblemReporter;
import com.mgmtp.a12.rmc.utils.ModelTupleIdentifier;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import tools.jackson.core.JacksonException;

/**
 * WCF converter that injects __meta metadata groups into Document Models.
 *
 * <p>This moves metadata injection from DS runtime (ExpandIncludesListener) to WCF build time,
 * ensuring document models already contain metadata structure in the workspace.
 *
 * <p><b>Ordering constraint:</b> This converter must run <em>after</em> {@link
 * com.mgmtp.a12.rmc.kernel.KernelModelsConverter KernelModelsConverter} (order=50) so that already
 * expanded documents are used. Kernel handles mapping models with '__meta' root groups as a normal
 * document model with multiple root groups. It must run <em>after</em> {@link
 * com.mgmtp.a12.rmc.exclusions.ExclusionsConverter ExclusionsConverter} (order=10) so that excluded
 * models are removed before metadata injection.
 *
 * <p>The metadata model resource can be customized via the system property {@code
 * com.mgmtp.a12.dataservices.rmc.metadata.resource}. This allows customer projects to provide their
 * own metadata definition with different fields. The value must be a classpath resource path (e.g.,
 * {@code /com/example/custom-meta-data.json}). If not set, the bundled default metadata model is
 * used.
 */
// Order 60: must be after ExclusionsConverter (10) and after KernelModelsConverter (50).
// See MetadataBeforeKernelPipelineTest for verification of this ordering constraint.
@WcfConverter(order = 60, description = "Injects __meta metadata groups into Document Models.")
public class MetadataConverter implements WorkspaceConverter {

  static final String METADATA_RESOURCE_PROPERTY =
      "com.mgmtp.a12.dataservices.rmc.metadata.resource";

  private static final String DEFAULT_METADATA_RESOURCE =
      "/com/mgmtp/a12/rmc/metadata/document-meta-data.json";

  private final DocumentModelSerializer dmSerializer = new DocumentModelSerializer();
  private final DocumentModelService documentModelService = new DocumentModelService();

  @Override
  public Workspace convert(Workspace workspace) {
    IDocumentModel metadataModel = loadMetadataModel();
    var factory = new DocumentModelMetadataInjectorFactory(documentModelService);

    var documentModelTuples =
        workspace.getModels().values().stream()
            .filter(ModelTupleIdentifier::isDocumentModel)
            .filter(mt -> !ModelTupleIdentifier.isAdditiveDocumentModel(mt))
            .toList();

    for (ModelTuple tuple : documentModelTuples) {
      IDocumentModel documentModel = deserializeDocumentModel(tuple.getContent());
      DocumentModelMetadataInjector injector = factory.getInstance(documentModel, Locale.US);
      IDocumentModel enrichedModel = injector.getDocumentModelWithMetadata(metadataModel);
      String serialized = serializeDocumentModel(enrichedModel);
      workspace
          .getModels()
          .put(tuple.getHeader().getId(), new DefaultModelTuple(tuple.getHeader(), serialized));
    }

    return workspace;
  }

  private IDocumentModel loadMetadataModel() {
    String resourcePath = System.getProperty(METADATA_RESOURCE_PROPERTY, DEFAULT_METADATA_RESOURCE);
    try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new IllegalStateException(
            "Metadata resource not found on classpath: " + resourcePath);
      }
      var reader = new InputStreamReader(is, StandardCharsets.UTF_8);
      var internalModel = dmSerializer.deserialize(reader);
      return documentModelService.convertToExternal(internalModel);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load metadata model", e);
    }
  }

  private IDocumentModel deserializeDocumentModel(String content) {
    try {
      var internalModel = dmSerializer.deserialize(new StringReader(content));
      return documentModelService.convertToExternal(internalModel);
    } catch (JacksonException e) {
      throw new IllegalArgumentException("Failed to deserialize document model", e);
    }
  }

  private String serializeDocumentModel(IDocumentModel documentModel) {
    var internalModel = documentModelService.convertFromExternal(documentModel);
    var writer = new StringWriter();
    try {
      dmSerializer.serialize(internalModel, writer, new ProblemReporter());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to serialize document model", e);
    }
    return writer.toString();
  }
}
