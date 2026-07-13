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

import static com.mgmtp.a12.rmc.utils.PathUtils.stripExtension;
import static com.mgmtp.a12.rmc.utils.PathUtils.stripWorkspacePrefix;
import static com.mgmtp.a12.rmc.utils.ResourceReader.resolveContent;

import com.mgmtp.a12.dataservices.wcf.WorkspaceConverter;
import com.mgmtp.a12.dataservices.wcf.annotations.WcfConverter;
import com.mgmtp.a12.dataservices.wcf.domain.FileTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WcfConverter(
    order = 10,
    description = "Removes excluded files and models from workspace based on settings.json")
public class ExclusionsConverter implements WorkspaceConverter {
  private static final Path DATA_PATH = Path.of("data");
  private static final Path RESOURCES_PATH = Path.of("resources");
  private static final Path SETTINGS = Path.of("settings.json");
  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public Workspace convert(Workspace workspace) {
    Map<String, FileTuple> files = workspace.getFiles();
    Set<Path> exclusions = readExclusions(files, workspace.getInputDir());

    Set<String> modelExclusions = getModelExclusions(exclusions);
    modelExclusions.forEach(workspace.getModels()::remove);

    for (var entry : List.copyOf(files.entrySet())) {
      Path path = Path.of(entry.getKey());
      if (exclusions.contains(stripExtension(path))) {
        files.remove(entry.getKey());
      }
    }
    return workspace;
  }

  private Set<Path> readExclusions(Map<String, FileTuple> files, String inputDir) {
    return files.entrySet().stream()
        .filter(e -> isSettingsFile(e.getKey()))
        .findFirst()
        .map(
            e -> {
              byte[] content = resolveContent(e.getValue(), e.getKey(), inputDir);
              return content == null ? Set.<Path>of() : parseExclusions(content);
            })
        .orElse(Set.of());
  }

  private Set<Path> parseExclusions(byte[] content) {
    JsonNode exclusions = jsonMapper.readTree(content).path("general").path("exclusions");
    if (!exclusions.isArray()) return Set.of();
    Set<Path> result = new HashSet<>();
    for (JsonNode entry : exclusions) {
      JsonNode excluded = entry.get("excluded");
      if (excluded != null) result.add(stripWorkspacePrefix(Path.of(excluded.asString())));
    }
    return result;
  }

  private Set<String> getModelExclusions(Set<Path> exclusions) {
    return exclusions.stream()
        .filter(e -> !e.startsWith(DATA_PATH) && !e.startsWith(RESOURCES_PATH))
        .map(e -> e.getFileName().toString())
        .collect(Collectors.toSet());
  }

  private boolean isSettingsFile(String p) {
    return Path.of(p).endsWith(SETTINGS);
  }
}
