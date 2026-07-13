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

import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.SelectionModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;

public class ModelConstants {
  private ModelConstants() {}

  // normal DMs, additive models, decoration models
  public static final String MODEL_TYPE_DOCUMENT = DocumentModel.MODEL_TYPE_DOCUMENT;
  public static final String MODEL_TYPE_COMBINATION = CombinationModelService.MODEL_TYPE;
  public static final String MODEL_TYPE_SELECTION = SelectionModelService.MODEL_TYPE;
  // TODO: provide via kernel
  public static final String MODEL_TYPE_MAPPING = "mapping";
  public static final String RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE =
      "runtime_mapping_precompfragment_document";
  public static final String ANNOTATION_ADDITIVE_MODEL = "additive-document";

  public static final String ANNOTATION_TYPEDEF = "tdonly";
}
