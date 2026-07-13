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
package com.mgmtp.a12.rmc.workspacestructure;

import com.mgmtp.a12.rmc.utils.TestFile;
import com.mgmtp.a12.rmc.utils.TestModel;
import java.nio.file.Path;
import java.util.List;

public final class WorkspaceStructureTestData {

  public static final TestFile COMPANY_DM_MOORE_LLC =
      new TestFile(
          "Company_DM_Moore_LLC",
          Path.of("data/documents/Company_DM/Company_DM_Moore_LLC.json").toString());
  public static final TestFile EMPLOYEE_DM_SKILES =
      new TestFile(
          "Employee_DM_Skiles",
          Path.of("data/documents/Employee_DM/Employee_DM_Skiles.json").toString());
  public static final TestFile MOORE_LLC_SKILES_LINK_DATA =
      new TestFile(
          "Moore_LLC_Skiles_LinkData",
          Path.of("data/documents/Moore_LLC_Skiles_LinkData.json").toString());
  public static final TestFile MOORE_LLC_SKILES_LINK =
      new TestFile("Moore_LLC_Skiles", Path.of("data/links/Moore_LLC_Skiles.json").toString());
  public static final TestFile ATTACHMENT_GIF =
      new TestFile("17.gif", Path.of("data/attachments/17.gif").toString());
  public static final TestFile SME_WORKSPACEDATA_ITEMS =
      new TestFile("workspacedata_items", Path.of("data/workspacedata_items.json").toString());
  public static final TestFile SME_SETTINGS =
      new TestFile("settings", Path.of("settings.json").toString());
  public static final TestFile SOME_XSD =
      new TestFile("some.xsd", Path.of("resources/schemas/some.xsd").toString());

  // models
  public static final TestModel COMPANY_DM =
      new TestModel("Company_DM", Path.of("models/Company_DM.json").toString());
  public static final TestModel EMPLOYEE_DM =
      new TestModel("Employee_DM", Path.of("models/Employee_DM.json").toString());
  public static final TestModel PERSON_COMPANY_LINK_FIELDS_DM =
      new TestModel(
          "PersonCompany_LinkFields_DM",
          Path.of("models/PersonCompany_LinkFields_DM.json").toString());

  // auth data
  public static final TestFile AUTH_USERS =
      new TestFile("users", Path.of("auth/users.yaml").toString());
  public static final TestFile AUTH_ROLES =
      new TestFile("roles", Path.of("auth/roles.yaml").toString());

  public static List<TestModel> smallWorkspaceModels() {
    return List.of(COMPANY_DM, EMPLOYEE_DM, PERSON_COMPANY_LINK_FIELDS_DM);
  }

  public static List<TestFile> smallWorkspaceAll() {
    return List.of(
        COMPANY_DM_MOORE_LLC,
        EMPLOYEE_DM_SKILES,
        MOORE_LLC_SKILES_LINK_DATA,
        MOORE_LLC_SKILES_LINK,
        ATTACHMENT_GIF,
        SME_WORKSPACEDATA_ITEMS,
        SME_SETTINGS,
        SOME_XSD,
        AUTH_USERS,
        AUTH_ROLES);
  }

  public static String smallWorkspaceInputDir() {
    return Path.of(
            System.getProperty("user.dir"),
            "src",
            "test",
            "resources",
            "workspacedata",
            "smallWorkspace")
        .toAbsolutePath()
        .toString();
  }
}
