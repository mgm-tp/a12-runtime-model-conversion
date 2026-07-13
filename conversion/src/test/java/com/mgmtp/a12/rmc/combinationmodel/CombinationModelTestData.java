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

import com.mgmtp.a12.rmc.utils.TestDataUtils;
import com.mgmtp.a12.rmc.utils.TestModel;
import java.util.List;

public final class CombinationModelTestData {
  public static final TestModel BASE_DOCUMENT_MODEL =
      new TestModel("BaseDocumentModel", "combinationmodel/BaseDocumentModel.json");
  public static final TestModel COMBINATION_MODEL =
      new TestModel("CombinationModel", "combinationmodel/CombinationModel.json");
  public static final TestModel TD_MODEL =
      new TestModel("TD_Address", "combinationmodel/TD_Address.json");
  public static final TestModel ADM_COMBINATION_MODEL =
      new TestModel("Adm_CombinationModel", "combinationmodel/Adm_CombinationModel.json");
  public static final TestModel SELECTION_MODEL =
      new TestModel("SelectionModel", "combinationmodel/SelectionModel.json");
  public static final TestModel SECOND_COMBINATION_MODEL =
      new TestModel("SecondCombinationModel", "combinationmodel/SecondCombinationModel.json");
  public static final TestModel SECOND_SELECTION_MODEL =
      new TestModel("SecondSelectionModel", "combinationmodel/SecondSelectionModel.json");
  public static final TestModel SELECTION_MODEL_NOT_REFERENCED =
      new TestModel(
          "SelectionModel_NotReferenced", "combinationmodel/SelectionModel_NotReferenced.json");

  private CombinationModelTestData() {}

  public static List<TestModel> all() {
    return List.of(
        BASE_DOCUMENT_MODEL,
        COMBINATION_MODEL,
        TD_MODEL,
        ADM_COMBINATION_MODEL,
        SELECTION_MODEL,
        SECOND_COMBINATION_MODEL,
        SECOND_SELECTION_MODEL,
        SELECTION_MODEL_NOT_REFERENCED);
  }

  public static String inputDir() {
    return TestDataUtils.getModelsInputDir();
  }
}
