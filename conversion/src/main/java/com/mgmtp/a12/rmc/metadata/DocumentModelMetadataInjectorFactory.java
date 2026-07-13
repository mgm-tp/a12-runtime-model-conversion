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

import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.rmc.utils.ModelConstants;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Factory class to construct a {@link DocumentModelMetadataInjector} instance based on the document
 * model type (generated, CDM, or standard DM).
 */
public final class DocumentModelMetadataInjectorFactory {

  private final DocumentModelService documentModelService;

  public DocumentModelMetadataInjectorFactory(DocumentModelService documentModelService) {
    this.documentModelService = documentModelService;
  }

  /**
   * Create an instance of the {@link DocumentModelMetadataInjector} appropriate for the given
   * document model.
   *
   * @param documentModel selector for the proper instance
   * @param locale required by Kernel
   * @return new instance of the {@link DocumentModelMetadataInjector} based on the document model
   */
  public DocumentModelMetadataInjector getInstance(IDocumentModel documentModel, Locale locale) {
    if (isGenerated(documentModel)) {
      return new GeneratedModelDocumentModelMetadataInjector(
          documentModel, documentModelService, locale);
    } else if (isCdm(documentModel)) {
      return new CdmDocumentModelMetadataInjector(documentModel, documentModelService, locale);
    } else {
      return new DmDocumentModelMetadataInjector(documentModel, documentModelService, locale);
    }
  }

  private static boolean isCdm(IDocumentModel documentModel) {
    Header header = documentModel.getHeader();
    return ModelConstants.MODEL_TYPE_DOCUMENT.equals(header.getModelType())
        && getAnnotations(header)
            .map(Annotation::getName)
            .anyMatch(MetadataConstants.CDM_QUERY_ROOT_ANNOTATION::equals);
  }

  private static boolean isGenerated(IDocumentModel documentModel) {
    return documentModel.getHeader().getId().endsWith(MetadataConstants.GENERATED_DM_SUFFIX);
  }

  /** Null-safe stream of annotations from a header. */
  private static Stream<Annotation> getAnnotations(Header header) {
    return Optional.of(header).map(Header::getAnnotations).stream().flatMap(Collection::stream);
  }
}
