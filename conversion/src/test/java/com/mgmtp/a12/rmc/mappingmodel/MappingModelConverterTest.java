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
package com.mgmtp.a12.rmc.mappingmodel;

import static org.junit.jupiter.api.Assertions.*;

import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.rmc.kernel.KernelModelsConverter;
import com.mgmtp.a12.rmc.utils.ModelConstants;
import com.mgmtp.a12.rmc.utils.TestWorkspace;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MappingModelConverterTest {

  private Workspace result;

  @BeforeEach
  void setUp() throws HeaderParseException, IOException {
    var converter = new KernelModelsConverter();
    var input = new TestWorkspace(MappingModelTestData.inputDir(), MappingModelTestData.all());
    result = converter.convert(input.getWorkspace());
  }

  @Test
  @DisplayName("Should remove additive document models not referenced by any mapping model")
  void shouldRemoveUnreferencedAdditiveModels() {
    int expectedSize = MappingModelTestData.all().size() - 3;
    assertEquals(
        expectedSize, result.getModels().size(), "Two unreferenced ADMs and TD should be removed");
    assertNull(
        result.getModels().get(MappingModelTestData.ADM_COMBINATION_MODEL.id()),
        "ADM_COMBINATION_MODEL should be removed as it's not referenced in a Mapping Model");
    assertNull(
        result.getModels().get(MappingModelTestData.ADM_NOT_REFERENCED.id()),
        "ADM_NOT_REFERENCED should be removed as it's not referenced in any Model");
  }

  @Test
  @DisplayName(
      "Should convert additive model type in additive models referenced by a Mapping Model")
  void shouldConvertAdditiveModelType() {
    var admInMappingTuple = result.getModels().get(MappingModelTestData.ADM_MAPPING_MODEL.id());

    assertNotNull(admInMappingTuple, "Referenced ADM should still be present");
    assertEquals(
        ModelConstants.RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE,
        admInMappingTuple.getHeader().getModelType(),
        "Header model type should be converted");

    var serializer = new DocumentModelSerializer();
    var admModel = serializer.deserialize(new StringReader(admInMappingTuple.getContent()));
    assertEquals(
        ModelConstants.RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE,
        admModel.getHeader().getModelType(),
        "Content model type should be converted");
  }
}
