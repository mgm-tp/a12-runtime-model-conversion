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
package com.mgmtp.a12.rmc.utils;

import com.mgmtp.a12.dataservices.wcf.WorkspaceFactory;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.HeaderParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class TestWorkspace {
  private final Workspace workspace;
  private final DefaultHeaderParser headerParser;

  public TestWorkspace(String inputDir, List<TestModel> models)
      throws HeaderParseException, IOException {
    this(inputDir, models, Collections.emptyList());
  }

  public TestWorkspace(String inputDir, List<TestModel> models, List<TestFile> files)
      throws HeaderParseException, IOException {
    this.workspace = WorkspaceFactory.getInstance().createWorkspace();
    this.workspace.setInputDir(inputDir);
    this.headerParser = new DefaultHeaderParser();

    for (var model : models) {
      this.addModel(inputDir, model);
    }

    for (var file : files) {
      this.addFile(inputDir, file);
    }
  }

  public Workspace getWorkspace() {
    return workspace;
  }

  public void addModel(String inputDir, TestModel model) throws HeaderParseException {
    var resourcePath = Path.of(inputDir, model.path()).toString();
    var document = new String(ResourceReader.readResourceFile(resourcePath));
    var header = this.headerParser.parseJson(document);

    this.workspace
        .getModels()
        .put(model.id(), WorkspaceFactory.getInstance().createModelTuple(header, document));
  }

  public void addFile(String inputDir, TestFile file) throws IOException {
    addFileWithContent(file.path(), Files.readAllBytes(Path.of(inputDir, file.path())));
  }

  public void addFileWithContent(String workspacePath, byte[] content) {
    this.workspace
        .getFiles()
        .put(workspacePath, WorkspaceFactory.getInstance().createFileTuple(workspacePath, content));
  }
}
