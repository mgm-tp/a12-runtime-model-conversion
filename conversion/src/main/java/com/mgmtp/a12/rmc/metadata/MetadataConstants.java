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

import java.util.regex.Pattern;

/**
 * Constants for metadata injection, inlined from DS dataservices-core-metadata and related modules.
 */
public final class MetadataConstants {

  private MetadataConstants() {}

  // --- From CddConstants ---

  /** Annotation key marking a query root in the CDM. */
  public static final String CDM_QUERY_ROOT_ANNOTATION = "cdm.queryRoot";

  /** Annotation key marking a relationship in the CDM. */
  public static final String CDM_RELATIONSHIP_ANNOTATION = "cdm.relationship";

  /** Logical group name used for relationship-related elements. */
  public static final String RELATIONSHIP_GROUP_NAME = "relationship";

  // --- From DocumentMetadataConstants ---

  /** Group name used to hold document-level metadata. */
  public static final String DOCUMENT_METADATA_GROUP_NAME = "__meta";

  /** Separator used in document metadata paths. */
  public static final String DOCUMENT_METADATA_PATH_SEPARATOR = "/";

  /** Absolute path to the document metadata group starting at the model root. */
  public static final String DOCUMENT_METADATA_GROUP_PATH =
      DOCUMENT_METADATA_PATH_SEPARATOR + DOCUMENT_METADATA_GROUP_NAME;

  // --- From DocumentModelMetadataInjectorFactory ---

  /** Model identifier used for document metadata. */
  public static final String DOCUMENT_META_DATA_MODEL_NAME = "document-meta-data";

  /**
   * Pattern that matches all metadata groups that may be stripped from a model. It matches the
   * document metadata group `__meta` and attachment metadata groups `__attachment_meta_<field>`.
   */
  public static final Pattern META_GROUP_NAME_PATTERN =
      Pattern.compile("^(?:__meta|__attachment_meta_.*)$");

  // --- New constants ---

  /** DM id suffix of a DM that was generated */
  public static final String GENERATED_DM_SUFFIX = "__generated";
}
