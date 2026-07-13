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
package com.mgmtp.a12.rmc.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import com.mgmtp.a12.dataservices.wcf.annotations.WcfConverter;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.rmc.kernel.KernelModelsConverter;
import com.mgmtp.a12.rmc.mappingmodel.MappingModelTestData;
import com.mgmtp.a12.rmc.metadata.MetadataConverter;
import com.mgmtp.a12.rmc.utils.ModelConstants;
import com.mgmtp.a12.rmc.utils.TestWorkspace;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetadataAndKernelPipelineTest {

  private final MetadataConverter metadataConverter = new MetadataConverter();
  private final KernelModelsConverter kernelModelsConverter = new KernelModelsConverter();

  @Test
  @DisplayName("MetadataConverter order should be after than KernelModelsConverter order")
  void metadataConverterOrderShouldBeLowerThanKernelModelsConverter() {
    int metadataOrder = MetadataConverter.class.getAnnotation(WcfConverter.class).order();
    int kernelOrder = KernelModelsConverter.class.getAnnotation(WcfConverter.class).order();

    assertTrue(
        metadataOrder > kernelOrder,
        "MetadataConverter (order=%d) must run after KernelModelsConverter (order=%d)"
            .formatted(metadataOrder, kernelOrder));
  }

  @Test
  @DisplayName("Adding metaInfo will fail, as this needs expandedModels (wrong order)")
  void shouldHaveMetaGroupsInDocumentModelsAfterPipeline()
      throws HeaderParseException, IOException {
    var input = new TestWorkspace(MappingModelTestData.inputDir(), MappingModelTestData.all());
    Workspace workspace = input.getWorkspace();

    // Precondition: raw DMs do NOT have __meta
    assertFalse(
        workspace.getModels().get("SourceDocumentModel").getContent().contains("\"__meta\""),
        "Source DM should not have __meta before MetadataConverter runs");
    assertFalse(
        workspace.getModels().get("TargetDocumentModel").getContent().contains("\"__meta\""),
        "Target DM should not have __meta before MetadataConverter runs");

    // Step 1: MetadataConverter injects __meta (order=40)
    try {
      metadataConverter.convert(workspace);
    } catch (IllegalStateException e) {
      assertEquals(
          "Error while joining document models: Unexpected model ID: TD_Custom (CM_SourceDocumentModel -> SourceDocumentModel) [ERROR]",
          e.getMessage());
    }
  }

  @Test
  @DisplayName("DMs should have __meta when KernelModelsConverter runs first (correct order)")
  void shouldNotHaveMetaGroupsWhenKernelRunsFirst() throws HeaderParseException, IOException {
    var input = new TestWorkspace(MappingModelTestData.inputDir(), MappingModelTestData.all());
    Workspace workspace = input.getWorkspace();

    Workspace afterKernel = kernelModelsConverter.convert(workspace);

    // DMs still don't have __meta (Kernel processed them without metadata awareness)
    assertFalse(
        afterKernel.getModels().get("SourceDocumentModel").getContent().contains("\"__meta\""),
        "Source DM should NOT have __meta when Kernel runs before MetadataConverter");
    assertFalse(
        afterKernel.getModels().get("TargetDocumentModel").getContent().contains("\"__meta\""),
        "Target DM should NOT have __meta when Kernel runs before MetadataConverter");

    // MetadataConverter adds __meta AFTER
    Workspace afterMeta = metadataConverter.convert(afterKernel);

    assertTrue(
        afterMeta.getModels().get("SourceDocumentModel").getContent().contains("\"__meta\""),
        "Source DM gets __meta only after MetadataConverter (too late for mapping)");
    assertTrue(
        afterMeta.getModels().get("TargetDocumentModel").getContent().contains("\"__meta\""),
        "Target DM gets __meta only after MetadataConverter (too late for mapping)");

    // Mapping precomputation fragment was correctly produced
    var precompTuple = afterMeta.getModels().get(MappingModelTestData.ADM_MAPPING_MODEL.id());
    assertNotNull(precompTuple, "Precomputation fragment should exist after pipeline");
    assertEquals(
        ModelConstants.RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE,
        precompTuple.getHeader().getModelType(),
        "ADM should be converted to precomp fragment type");
  }
}
