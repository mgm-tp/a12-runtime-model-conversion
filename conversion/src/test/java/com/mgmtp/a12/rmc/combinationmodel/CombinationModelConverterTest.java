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
package com.mgmtp.a12.rmc.combinationmodel;

import static org.junit.jupiter.api.Assertions.*;

import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.rmc.kernel.KernelModelsConverter;
import com.mgmtp.a12.rmc.utils.ModelConstants;
import com.mgmtp.a12.rmc.utils.TestWorkspace;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CombinationModelConverterTest {

  @Test
  void testConvert() throws IOException, HeaderParseException {
    KernelModelsConverter converter = new KernelModelsConverter();

    var input =
        new TestWorkspace(CombinationModelTestData.inputDir(), CombinationModelTestData.all());

    assertFalse(
        input
            .getWorkspace()
            .getModels()
            .get(CombinationModelTestData.COMBINATION_MODEL.id())
            .getContent()
            .contains("BaseDocumentModelRoot"));

    Workspace result = converter.convert(input.getWorkspace());

    assertEquals(3, result.getModels().size());
    List.of("BaseDocumentModelRoot", "BaseDocumentModelField", "AddedGroup", "AddedField")
        .forEach(
            element ->
                assertTrue(
                    result
                        .getModels()
                        .get(CombinationModelTestData.COMBINATION_MODEL.id())
                        .getContent()
                        .contains(element)));

    var combModel = result.getModels().get(CombinationModelTestData.COMBINATION_MODEL.id());

    // Contained in BaseDocumentModel but removed by selection model
    assertFalse(combModel.getContent().contains("SubGroup"));
    assertFalse(combModel.getContent().contains("SubGroupField"));

    // Selection models should be removed from workspace after conversion
    assertNull(result.getModels().get(CombinationModelTestData.SELECTION_MODEL.id()));
    assertNull(result.getModels().get(CombinationModelTestData.SECOND_SELECTION_MODEL.id()));
    assertNull(
        result.getModels().get(CombinationModelTestData.SELECTION_MODEL_NOT_REFERENCED.id()));
    assertNull(result.getModels().get(CombinationModelTestData.ADM_COMBINATION_MODEL.id()));
    assertNull(result.getModels().get(CombinationModelTestData.TD_MODEL.id()));

    // References to selection models should be removed
    var modelsReferencingSelModels =
        this.getModelsReferencingType(result, ModelConstants.MODEL_TYPE_SELECTION);
    assertEquals(0, modelsReferencingSelModels.count());

    // References to other documents should be removed
    var modelsReferencingDms =
        this.getModelsReferencingType(result, ModelConstants.MODEL_TYPE_DOCUMENT);
    assertEquals(0, modelsReferencingDms.count());
  }

  private Stream<ModelTuple> getModelsReferencingType(Workspace workspace, String referencedType) {
    return workspace.getModels().values().stream()
        .filter(
            mt ->
                mt.getHeader().getModelReferences().stream()
                    .anyMatch(ref -> referencedType.equals(ref.getModelType())));
  }
}
