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

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceMetadata {

  @JsonMerge private final Map<String, FileNameEntry> documents = new LinkedHashMap<>();
  @JsonMerge private final Map<String, AttachmentEntry> attachments = new LinkedHashMap<>();
  @JsonMerge private final Map<String, FileNameEntry> links = new LinkedHashMap<>();

  public static WorkspaceMetadata empty() {
    return new WorkspaceMetadata();
  }

  public Map<String, FileNameEntry> getDocuments() {
    return documents;
  }

  public Map<String, AttachmentEntry> getAttachments() {
    return attachments;
  }

  public Map<String, FileNameEntry> getLinks() {
    return links;
  }

  // ── value types ───────────────────────────────────────────────────────────

  public interface HasFileName {
    String fileName();
  }

  public record FileNameEntry(@JsonProperty("fileName") String fileName) implements HasFileName {}

  public record AttachmentEntry(
      @JsonProperty("fileName") String fileName,
      @JsonProperty("annotations") List<AttachmentAnnotation> annotations)
      implements HasFileName {}

  public record AttachmentAnnotation(
      @JsonProperty("name") String name, @JsonProperty("value") String value) {}
}
