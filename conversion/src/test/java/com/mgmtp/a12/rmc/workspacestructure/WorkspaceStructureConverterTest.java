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

import static org.junit.jupiter.api.Assertions.*;

import com.mgmtp.a12.dataservices.wcf.domain.FileTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.rmc.utils.TestWorkspace;
import com.mgmtp.a12.rmc.workspacestructure.WorkspaceMetadata.AttachmentEntry;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class WorkspaceStructureConverterTest {

  private final WorkspaceStructureConverter converter = new WorkspaceStructureConverter();
  private final ObjectMapper mapper = new ObjectMapper();

  private Workspace result;

  @BeforeEach
  void setUp() throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(
            WorkspaceStructureTestData.smallWorkspaceInputDir(),
            WorkspaceStructureTestData.smallWorkspaceModels(),
            WorkspaceStructureTestData.smallWorkspaceAll());
    result = converter.convert(input.getWorkspace());
  }

  @Test
  @DisplayName("Should produce exactly 3 document entries, 1 link entry, 1 attachment entry")
  void producesCorrectOutputCounts() {
    long docs =
        result.getFiles().keySet().stream()
            .filter(k -> k.startsWith(Path.of("data/documents/").toString()))
            .count();
    long links =
        result.getFiles().keySet().stream()
            .filter(k -> k.startsWith(Path.of("data/links/").toString()))
            .count();
    long attachments =
        result.getFiles().keySet().stream()
            .filter(k -> k.startsWith(Path.of("data/attachments/").toString()))
            .count();

    assertEquals(3, docs);
    assertEquals(1, links);
    assertEquals(1, attachments);
  }

  @Test
  @DisplayName("Should produce correct number of workspace data items")
  void producesWorkspaceMetadata() {
    FileTuple metaTuple =
        result.getFiles().get(Path.of("data/meta/workspacedata_items.json").toString());
    assertNotNull(metaTuple);
    WorkspaceMetadata meta = mapper.readValue(metaTuple.getContent(), WorkspaceMetadata.class);
    assertEquals(2, meta.getDocuments().size(), "3 documents expected in metadata");
    assertEquals(1, meta.getLinks().size(), "1 link expected in metadata");
    assertEquals(1, meta.getAttachments().size(), "1 attachment expected in metadata");
  }

  @Test
  @DisplayName("Should preserve known UUID for known entries")
  void preservesKnownCompanyUuid() {
    assertTrue(
        result
            .getFiles()
            .containsKey(
                Path.of("data/documents/Company_DM/a27dcc68-2553-4cd9-b2bb-58761a03a9f9.json")
                    .toString()),
        "Company_DM document should use UUID from workspacedata_items.json");
    assertTrue(
        result
            .getFiles()
            .containsKey(
                Path.of(
                        "data/documents/PersonCompany_LinkFields_DM/3ffeb8c4-0e07-4322-9097-6b69f087fcd1.json")
                    .toString()),
        "Link data document should use UUID from workspacedata_items.json");
    assertTrue(
        result
            .getFiles()
            .containsKey(
                Path.of("data/links/PersonCompany/6e28492e-d060-4730-9ea3-e2e7ddc7eafe.json")
                    .toString()),
        "PersonCompany link should use UUID from workspacedata_items.json");
    assertTrue(
        result
            .getFiles()
            .containsKey(
                Path.of("data/attachments/d1e4ac70-648d-43f6-b2e6-cbfabd7aae2e.gif").toString()),
        "17.gif attachment should use UUID from workspacedata_items.json");
  }

  @Test
  @DisplayName("Should strip documentModelName from Company document json content")
  void stripsDocumentModelNameFromCompanyDocumentContent() {
    FileTuple doc =
        result
            .getFiles()
            .get(
                Path.of("data/documents/Company_DM/a27dcc68-2553-4cd9-b2bb-58761a03a9f9.json")
                    .toString());
    assertNotNull(doc);
    var node = mapper.readTree(doc.getContent());
    assertFalse(node.has("documentModelName"), "documentModelName must be removed");
    assertTrue(node.has("Company"), "Inner document content must be present");
  }

  @Test
  @DisplayName("Should move other files correctly")
  void copiesOtherFiles() {
    assertFalse(
        result.getFiles().containsKey("resources/schemas/some.xsd"),
        "Original resource file should not be present");
    assertTrue(
        result.getFiles().containsKey(Path.of("data/resources/schemas/some.xsd").toString()),
        "resources/schemas/some.xsd should be copied to data/resources/schemas/some.xsd");
    assertFalse(
        result.getFiles().containsKey(Path.of("auth/roles.yaml").toString()),
        "auth/roles.yaml should be removed from its original path");
    assertTrue(
        result.getFiles().containsKey(Path.of("data/user/roles.yaml").toString()),
        "auth/roles.yaml should be placed at data/user/roles.yaml");
    assertFalse(
        result.getFiles().containsKey(Path.of("auth/users.yaml").toString()),
        "auth/users.yaml should be removed from its original path");
    assertTrue(
        result.getFiles().containsKey(Path.of("data/user/users.yaml").toString()),
        "auth/users.yaml should be placed at data/user/users.yaml");
    assertFalse(
        result.getFiles().containsKey(Path.of("settings.json").toString()),
        "settings.json should be removed from its original path");
  }

  @Test
  @DisplayName("Should preserve UUID and annotations for known attachment")
  void preservesAttachmentUuidAndAnnotations() {
    FileTuple metaTuple =
        result.getFiles().get(Path.of("data/meta/workspacedata_items.json").toString());
    assertNotNull(metaTuple);
    WorkspaceMetadata meta = mapper.readValue(metaTuple.getContent(), WorkspaceMetadata.class);

    AttachmentEntry entry = meta.getAttachments().get("d1e4ac70-648d-43f6-b2e6-cbfabd7aae2e");
    assertNotNull(entry, "Known attachment UUID should be preserved from workspacedata_items.json");
    assertEquals(1, entry.annotations().size());
    assertEquals("category", entry.annotations().getFirst().name());
    assertEquals("logo", entry.annotations().getFirst().value());
  }
}
