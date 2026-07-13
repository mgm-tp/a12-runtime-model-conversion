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

import com.mgmtp.a12.dataservices.wcf.WorkspaceConverter;
import com.mgmtp.a12.dataservices.wcf.WorkspaceFactory;
import com.mgmtp.a12.dataservices.wcf.annotations.WcfConverter;
import com.mgmtp.a12.dataservices.wcf.domain.FileTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.rmc.utils.PathUtils;
import com.mgmtp.a12.rmc.utils.ResourceReader;
import com.mgmtp.a12.rmc.workspacestructure.WorkspaceMetadata.AttachmentAnnotation;
import com.mgmtp.a12.rmc.workspacestructure.WorkspaceMetadata.AttachmentEntry;
import com.mgmtp.a12.rmc.workspacestructure.WorkspaceMetadata.HasFileName;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Organizes raw workspace data (documents, links, attachments, resources) into the UUID-keyed,
 * model-grouped structure that Data Services expects for import.
 *
 * <p>This shall be the last converter in the chain before Data Services import.
 */
@WcfConverter(
    order = 999,
    description = "Transforms workspace data into UUID-keyed structure for Data Services import.")
public class WorkspaceStructureConverter implements WorkspaceConverter {

  private static final Path DOCUMENTS_PATH = Path.of("data", "documents");
  private static final Path LINKS_PATH = Path.of("data", "links");
  private static final Path ATTACHMENTS_PATH = Path.of("data", "attachments");
  private static final Path RESOURCES_PATH = Path.of("resources");
  private static final Path SETTINGS = Path.of("settings.json");
  private static final Path INPUT_USERS_SETTINGS_PATH = Path.of("auth", "users.yaml");
  private static final Path OUTPUT_USERS_SETTINGS_PATH = Path.of("data", "user", "users.yaml");
  private static final Path INPUT_ROLES_SETTINGS_PATH = Path.of("auth", "roles.yaml");
  private static final Path OUTPUT_ROLES_SETTINGS_PATH = Path.of("data", "user", "roles.yaml");
  private static final Path INPUT_WORKSPACEDATA_ITEMS_PATH =
      Path.of("data", "workspacedata_items.json");
  private static final Path OUTPUT_WORKSPACEDATA_ITEMS_PATH =
      Path.of("data", "meta", "workspacedata_items.json");

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Workspace convert(Workspace workspace) {
    Map<String, FileTuple> files = workspace.getFiles();
    WorkspaceMetadata existingMeta = readMetadata(files, workspace.getInputDir());

    for (var entry : List.copyOf(files.entrySet())) {
      byte[] content =
          ResourceReader.resolveContent(entry.getValue(), entry.getKey(), workspace.getInputDir());
      Path path = Path.of(entry.getKey());

      if (path.startsWith(DOCUMENTS_PATH)) {
        processDocument(files, path, content, existingMeta);
      } else if (path.startsWith(LINKS_PATH)) {
        processLink(files, path, content, existingMeta);
      } else if (path.startsWith(ATTACHMENTS_PATH)) {
        processAttachment(files, path, content, existingMeta);
      } else if (path.startsWith(RESOURCES_PATH)) {
        convertFile(files, path.toString(), Path.of("data").resolve(path).toString(), content);
      } else if (path.equals(INPUT_USERS_SETTINGS_PATH)) {
        convertFile(files, path.toString(), OUTPUT_USERS_SETTINGS_PATH.toString(), content);
      } else if (path.equals(INPUT_ROLES_SETTINGS_PATH)) {
        convertFile(files, path.toString(), OUTPUT_ROLES_SETTINGS_PATH.toString(), content);
      } else if (path.endsWith(SETTINGS)) {
        files.remove(path.toString());
      }
    }

    byte[] newMetaContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(existingMeta);
    convertFile(
        files,
        INPUT_WORKSPACEDATA_ITEMS_PATH.toString(),
        OUTPUT_WORKSPACEDATA_ITEMS_PATH.toString(),
        newMetaContent);

    return workspace;
  }

  /*
   *  Keeps only inner document object, assigns or reuses a UUID, writes to data/documents/{docmodelname}/{uuid}.json, updates metadata
   */
  private void processDocument(
      Map<String, FileTuple> files, Path path, byte[] content, WorkspaceMetadata existingMeta) {
    JsonNode root = mapper.readTree(content);
    String dmName = root.get("documentModelName").asString();
    String uuid = getOrGenerateId(path, existingMeta.getDocuments());
    Path newPath = DOCUMENTS_PATH.resolve(dmName).resolve(uuid + ".json");
    byte[] docContent = mapper.writeValueAsBytes(root.get("document"));

    convertFile(files, path.toString(), newPath.toString(), docContent);
  }

  /*
   *  Assigns or reuses a UUID, writes to data/links/{modelname}/{uuid}.json, updates metadata
   */
  private void processLink(
      Map<String, FileTuple> files, Path path, byte[] content, WorkspaceMetadata existingMeta) {
    String linkName = mapper.readTree(content).get("relationshipModel").asString();
    String uuid = getOrGenerateId(path, existingMeta.getLinks());
    Path newPath = LINKS_PATH.resolve(linkName).resolve(uuid + ".json");

    convertFile(files, path.toString(), newPath.toString(), content);
  }

  /*
   *  Assigns or reuses a UUID, writes to data/attachments/{uuid}.{ext}, updates metadata
   */
  private void processAttachment(
      Map<String, FileTuple> files, Path path, byte[] content, WorkspaceMetadata existingMeta) {
    Map<String, AttachmentEntry> attachments = existingMeta.getAttachments();
    List<AttachmentAnnotation> annotations = getAttachmentAnnotations(path, attachments);
    String uuid = getOrGenerateId(path, attachments);
    String ext = PathUtils.getExtension(path);
    Path newPath = ATTACHMENTS_PATH.resolve(ext.isEmpty() ? uuid : uuid + "." + ext);

    convertFile(files, path.toString(), newPath.toString(), content);
  }

  private WorkspaceMetadata readMetadata(Map<String, FileTuple> files, String inputDir) {
    FileTuple meta = files.get(INPUT_WORKSPACEDATA_ITEMS_PATH.toString());
    if (meta == null) {
      return WorkspaceMetadata.empty();
    }
    byte[] content =
        ResourceReader.resolveContent(meta, INPUT_WORKSPACEDATA_ITEMS_PATH.toString(), inputDir);
    if (content == null) {
      return WorkspaceMetadata.empty();
    }

    return mapper.readValue(content, WorkspaceMetadata.class);
  }

  private void convertFile(
      Map<String, FileTuple> files, String oldPath, String newPath, byte[] content) {
    files.remove(oldPath);
    files.put(newPath, WorkspaceFactory.getInstance().createFileTuple(newPath, content));
  }

  private String getOrGenerateId(Path path, Map<String, ? extends HasFileName> existingMeta) {
    for (var entry : existingMeta.entrySet()) {
      Path metaPath = PathUtils.stripWorkspacePrefix(Path.of(entry.getValue().fileName()));
      if (metaPath.equals(path)) {
        return Path.of(entry.getKey()).getFileName().toString();
      }
    }
    return UUID.randomUUID().toString();
  }

  private List<AttachmentAnnotation> getAttachmentAnnotations(
      Path path, Map<String, AttachmentEntry> existingMeta) {
    for (AttachmentEntry entry : existingMeta.values()) {
      if (PathUtils.stripWorkspacePrefix(Path.of(entry.fileName())).equals(path)) {
        return entry.annotations();
      }
    }
    return List.of();
  }
}
