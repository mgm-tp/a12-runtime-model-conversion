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
package com.mgmtp.a12.rmc.exclusions;

import static org.junit.jupiter.api.Assertions.*;

import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.rmc.utils.TestWorkspace;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExclusionsConverterTest {

  private final ExclusionsConverter converter = new ExclusionsConverter();

  private Workspace result;

  @BeforeEach
  void setUp() throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(
            ExclusionsTestData.inputDir(), ExclusionsTestData.models(), ExclusionsTestData.files());
    result = converter.convert(input.getWorkspace());
  }

  @Test
  @DisplayName(
      "Should remove excluded model Company_DM from workspace and keep other models and files")
  void removesExcludedModelAndFile() {
    assertFalse(
        result.getModels().containsKey("Company_DM"),
        "Company_DM should be excluded via settings.json");
    assertFalse(
        result.getFiles().containsKey("data/links/Moore_LLC_Skiles.json"),
        "Moore_LLC_Skiles.json should be excluded via settings.json");

    assertTrue(result.getModels().containsKey("Employee_DM"));
    assertTrue(result.getModels().containsKey("PersonCompany_LinkFields_DM"));
    assertTrue(
        result
            .getFiles()
            .containsKey(
                Path.of("data/documents/Company_DM/Company_DM_Moore_LLC.json").toString()));
    assertTrue(
        result
            .getFiles()
            .containsKey(Path.of("data/documents/Employee_DM/Employee_DM_Skiles.json").toString()));
    assertTrue(result.getFiles().containsKey(Path.of("data/attachments/17.gif").toString()));
  }
}
