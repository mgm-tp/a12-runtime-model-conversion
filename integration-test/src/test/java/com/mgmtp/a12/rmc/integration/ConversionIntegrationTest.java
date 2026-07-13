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
package com.mgmtp.a12.rmc.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Integration test for the Conversion FAT JAR with the WCF CLI.
 *
 * <p>Run the task 'runWcfCliExec' to execute the complete integration test: gradle
 * :integration-test:runWcfCliExec
 */
class ConversionIntegrationTest {

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();

  // Path constants
  private static final String INPUT_MODELS_DIR =
      "../conversion/src/test/resources/models/combinationmodel";
  private static final String CONVERTED_MODELS_DIR = "build/converted/data/models";

  // File name constants
  private static final String COMBINATION_MODEL_JSON = "CombinationModel.json";
  private static final String BASE_DOCUMENT_MODEL_WITH_INCLUDE_JSON =
      "BaseDocumentModelWithInclude.json";

  // JSON field constants
  private static final String FIELD_HEADER = "header";
  private static final String FIELD_MODEL_TYPE = "modelType";
  private static final String MODEL_TYPE_DOCUMENT = "document";

  @Test
  void testInputModelsExist() {
    // Check if the test models exist
    File modelsDir = new File(INPUT_MODELS_DIR);
    assertTrue(
        modelsDir.exists() && modelsDir.isDirectory(),
        "Test models should exist: " + modelsDir.getAbsolutePath());

    // Check if at least one file exists
    File[] files = modelsDir.listFiles();
    assertTrue(files != null && files.length > 0, "Test models directory should contain files");
  }

  @Test
  void testConvertedFilesExist() throws IOException {
    // Check if the converted files exist
    // This test assumes that runWcfCliExec has already been executed

    Path convertedDir = Paths.get(CONVERTED_MODELS_DIR);

    if (!Files.exists(convertedDir)) {
      fail(
          "Converted directory does not exist: "
              + convertedDir.toAbsolutePath()
              + "\n\nPlease execute the task first: gradle :integration-test:runWcfCliExec");
    }

    // Check if the expected files exist
    Path combinationModel = convertedDir.resolve(COMBINATION_MODEL_JSON);
    Path baseDmFile = convertedDir.resolve(BASE_DOCUMENT_MODEL_WITH_INCLUDE_JSON);

    assertTrue(
        Files.exists(combinationModel),
        COMBINATION_MODEL_JSON + " should exist: " + combinationModel.toAbsolutePath());

    assertTrue(
        Files.exists(baseDmFile),
        BASE_DOCUMENT_MODEL_WITH_INCLUDE_JSON + " should exist: " + baseDmFile.toAbsolutePath());

    // Check if the files are not empty
    assertTrue(Files.size(combinationModel) > 0, COMBINATION_MODEL_JSON + " should not be empty");

    assertTrue(
        Files.size(baseDmFile) > 0, BASE_DOCUMENT_MODEL_WITH_INCLUDE_JSON + " should not be empty");
  }

  @Test
  void testCombWithoutCombStepsModelType() {
    // Check if combWithoutCombSteps.json has modelType "document"

    Path combWithoutCombStepsFile = Paths.get(CONVERTED_MODELS_DIR, COMBINATION_MODEL_JSON);

    if (!Files.exists(combWithoutCombStepsFile)) {
      fail(
          COMBINATION_MODEL_JSON
              + " does not exist: "
              + combWithoutCombStepsFile.toAbsolutePath()
              + "\n\nPlease execute the task first: gradle :integration-test:runWcfCliExec");
    }

    // Parse the JSON file
    JsonNode rootNode = objectMapper.readTree(combWithoutCombStepsFile.toFile());

    // Check if the JSON has a header node
    assertNotNull(rootNode, "JSON root node should not be null");
    assertTrue(rootNode.has(FIELD_HEADER), "JSON should have a '" + FIELD_HEADER + "' field");

    JsonNode headerNode = rootNode.get(FIELD_HEADER);

    // Check if modelType field exists
    assertTrue(
        headerNode.has(FIELD_MODEL_TYPE),
        "Header should contain a '" + FIELD_MODEL_TYPE + "' field");

    // Check if modelType is "document"
    String modelType = headerNode.get(FIELD_MODEL_TYPE).asText();
    assertEquals(
        MODEL_TYPE_DOCUMENT,
        modelType,
        FIELD_MODEL_TYPE + " should be '" + MODEL_TYPE_DOCUMENT + "'");
  }
}
