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

import com.mgmtp.a12.rmc.utils.TestDataUtils;
import com.mgmtp.a12.rmc.utils.TestModel;
import java.util.List;

public final class MappingModelTestData {
  public static final TestModel SOURCE_DOCUMENT_MODEL =
      new TestModel("SourceDocumentModel", "mappingmodel/SourceDocumentModel.json");
  public static final TestModel TARGET_DOCUMENT_MODEL =
      new TestModel("TargetDocumentModel", "mappingmodel/TargetDocumentModel.json");
  public static final TestModel ADM_COMBINATION_MODEL =
      new TestModel("Adm_CombinationModel", "mappingmodel/Adm_CombinationModel.json");
  public static final TestModel ADM_MAPPING_MODEL =
      new TestModel("Adm_MappingModel", "mappingmodel/Adm_MappingModel.json");
  public static final TestModel ADM_NOT_REFERENCED =
      new TestModel("Adm_NotReferenced", "mappingmodel/Adm_NotReferenced.json");
  public static final TestModel MAPPING_MODEL =
      new TestModel("MappingModel", "mappingmodel/MappingModel.json");
  public static final TestModel STRUCTURAL_MAPPING_MODEL =
      new TestModel("StructuralMapping", "mappingmodel/StructuralMappingModel.json");
  public static final TestModel TD_CUSTOM =
      new TestModel("TD_Custom", "mappingmodel/TD_Custom.json");

  private MappingModelTestData() {}

  public static List<TestModel> all() {
    return List.of(
        SOURCE_DOCUMENT_MODEL,
        TARGET_DOCUMENT_MODEL,
        ADM_COMBINATION_MODEL,
        ADM_MAPPING_MODEL,
        ADM_NOT_REFERENCED,
        MAPPING_MODEL,
        STRUCTURAL_MAPPING_MODEL,
        TD_CUSTOM);
  }

  public static String inputDir() {
    return TestDataUtils.getModelsInputDir();
  }
}
