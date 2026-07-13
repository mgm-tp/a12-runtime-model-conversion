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

import static org.junit.jupiter.api.Assertions.*;

import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.rmc.utils.TestWorkspace;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MetadataConverterTest {

  private final MetadataConverter converter = new MetadataConverter();

  @AfterEach
  void cleanUp() {
    System.clearProperty(MetadataConverter.METADATA_RESOURCE_PROPERTY);
  }

  @Test
  void shouldAddMetaGroupToSimpleDocumentModel() throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(
            MetadataConverterTestData.inputDir(),
            List.of(MetadataConverterTestData.SIMPLE_DOCUMENT_MODEL));

    Workspace result = converter.convert(input.getWorkspace());

    assertEquals(1, result.getModels().size(), "Model count should remain 1");
    var content = result.getModels().get("SimpleDocumentModel").getContent();
    assertTrue(content.contains("\"__meta\""), "Model should contain __meta group");
    assertTrue(content.contains("\"docRef\""), "Meta group should contain docRef field");
    assertTrue(content.contains("\"modelReference\""), "Meta group should contain modelReference");
    assertTrue(content.contains("\"creator\""), "Meta group should contain creator field");
    assertTrue(content.contains("\"createdAt\""), "Meta group should contain createdAt field");
    assertTrue(content.contains("\"modifier\""), "Meta group should contain modifier field");
    assertTrue(content.contains("\"modifiedAt\""), "Meta group should contain modifiedAt field");
    assertTrue(content.contains("\"extensions\""), "Meta group should contain extensions subgroup");
  }

  @Test
  void shouldAddMetaGroupToCdmRelationshipGroups() throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(
            MetadataConverterTestData.inputDir(),
            List.of(MetadataConverterTestData.CDM_DOCUMENT_MODEL));

    Workspace result = converter.convert(input.getWorkspace());

    assertEquals(1, result.getModels().size(), "Model count should remain 1");
    var content = result.getModels().get("CdmDocumentModel").getContent();
    assertTrue(content.contains("\"__meta\""), "CDM model should contain __meta group");

    // CDM models get __meta at root (via ADDITIVE_MODEL joining) plus in relationship groups.
    // Count occurrences of __meta to verify multiple insertions.
    int metaCount = countOccurrences(content, "\"__meta\"");
    assertTrue(
        metaCount >= 3,
        "CDM model should have __meta at root, relationship group, and relationship child. Found: "
            + metaCount);
  }

  @Test
  void shouldAddMetaGroupToAllRootGroupsOfGeneratedModel()
      throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(
            MetadataConverterTestData.inputDir(),
            List.of(MetadataConverterTestData.GENERATED_DOCUMENT_MODEL));

    Workspace result = converter.convert(input.getWorkspace());

    assertEquals(1, result.getModels().size(), "Model count should remain 1");
    var content = result.getModels().get("MyModel__generated").getContent();

    // Generated models should get __meta in each root group (2 root groups in our fixture)
    int metaCount = countOccurrences(content, "\"__meta\"");
    assertTrue(
        metaCount >= 2,
        "Generated model should have __meta in each root group. Found: " + metaCount);
  }

  @Test
  void shouldBeIdempotent() throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(
            MetadataConverterTestData.inputDir(),
            List.of(MetadataConverterTestData.SIMPLE_DOCUMENT_MODEL));

    Workspace firstPass = converter.convert(input.getWorkspace());
    Workspace secondPass = converter.convert(firstPass);

    var contentAfterFirst = firstPass.getModels().get("SimpleDocumentModel").getContent();
    var contentAfterSecond = secondPass.getModels().get("SimpleDocumentModel").getContent();

    int metaCountFirst = countOccurrences(contentAfterFirst, "\"__meta\"");
    int metaCountSecond = countOccurrences(contentAfterSecond, "\"__meta\"");
    assertEquals(
        metaCountFirst, metaCountSecond, "Running converter twice should not double the metadata");
  }

  @Test
  void shouldProcessMultipleModelsInWorkspace() throws IOException, HeaderParseException {
    var input =
        new TestWorkspace(MetadataConverterTestData.inputDir(), MetadataConverterTestData.all());

    Workspace result = converter.convert(input.getWorkspace());

    assertEquals(3, result.getModels().size(), "All 3 models should be present");
    for (var entry : result.getModels().entrySet()) {
      assertTrue(
          entry.getValue().getContent().contains("\"__meta\""),
          "Model " + entry.getKey() + " should contain __meta group");
    }
  }

  @Test
  void shouldAddMinimalMetaGroupWhenCustomResourceConfigured()
      throws IOException, HeaderParseException {
    System.setProperty(
        MetadataConverter.METADATA_RESOURCE_PROPERTY,
        "/models/metadata/minimal-document-meta-data.json");
    var customConverter = new MetadataConverter();

    var input =
        new TestWorkspace(
            MetadataConverterTestData.inputDir(),
            List.of(MetadataConverterTestData.SIMPLE_DOCUMENT_MODEL));

    Workspace result = customConverter.convert(input.getWorkspace());

    var content = result.getModels().get("SimpleDocumentModel").getContent();
    assertTrue(content.contains("\"__meta\""), "Model should contain __meta group");
    assertTrue(content.contains("\"docRef\""), "Meta should contain docRef");
    assertTrue(content.contains("\"modelReference\""), "Meta should contain modelReference");
    assertFalse(content.contains("\"creator\""), "Minimal meta should not contain creator");
    assertFalse(content.contains("\"createdAt\""), "Minimal meta should not contain createdAt");
    assertFalse(content.contains("\"modifier\""), "Minimal meta should not contain modifier");
    assertFalse(content.contains("\"modifiedAt\""), "Minimal meta should not contain modifiedAt");
  }

  private static int countOccurrences(String text, String pattern) {
    int count = 0;
    int idx = 0;
    while ((idx = text.indexOf(pattern, idx)) != -1) {
      count++;
      idx += pattern.length();
    }
    return count;
  }
}
