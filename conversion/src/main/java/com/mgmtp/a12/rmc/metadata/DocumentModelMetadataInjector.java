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

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

/**
 * Enriches a DocumentModel with metadata structure defined in the DocumentMetadataModel, and also
 * allows stripping this metadata structure.
 */
public interface DocumentModelMetadataInjector {

  /**
   * Create an enriched copy of a document model with document metadata on all expected places.
   *
   * <p>For non-generated document models, a "__meta" group is added to the root of the model. For
   * generated documents, "__meta" is added to every root group. For CDM, "__meta" is added to each
   * group annotated by `cdm.relationship` and also to its children group named "relationship" if it
   * exists.
   *
   * @param documentMetadataModel document model metadata
   * @return copy of the original document model enriched by the metadata
   */
  IDocumentModel getDocumentModelWithMetadata(IDocumentModel documentMetadataModel);

  /**
   * Remove all document metadata and attachment metadata groups by name pattern.
   *
   * @return copy of the original document with all metadata removed
   */
  IDocumentModel getDocumentModelWithoutMetadata();
}
