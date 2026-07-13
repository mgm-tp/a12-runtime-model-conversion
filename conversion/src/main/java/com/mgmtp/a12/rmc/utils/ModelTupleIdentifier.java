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

import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.model.header.Annotation;
import java.util.Optional;

public final class ModelTupleIdentifier {
  private ModelTupleIdentifier() {}

  public static boolean isDocumentModel(ModelTuple mt) {
    return hasModelType(mt, ModelConstants.MODEL_TYPE_DOCUMENT);
  }

  public static boolean isCombinationModel(ModelTuple mt) {
    return hasModelType(mt, ModelConstants.MODEL_TYPE_COMBINATION);
  }

  public static boolean isMappingModel(ModelTuple mt) {
    return hasModelType(mt, ModelConstants.MODEL_TYPE_MAPPING);
  }

  public static boolean isSelectionModel(ModelTuple mt) {
    return hasModelType(mt, ModelConstants.MODEL_TYPE_SELECTION);
  }

  public static boolean isTypeDefinitionModel(ModelTuple mt) {
    return hasModelType(mt, ModelConstants.MODEL_TYPE_DOCUMENT)
        && getAnnotation(mt, ModelConstants.ANNOTATION_TYPEDEF)
            .map(annotation -> annotation.getValue().equals("true"))
            .orElse(false);
  }

  public static boolean isAdditiveDocumentModel(ModelTuple mt) {
    return isDocumentModel(mt)
        && mt.getHeader().getAnnotations().stream()
            .anyMatch(ann -> ModelConstants.ANNOTATION_ADDITIVE_MODEL.equals(ann.getName()));
  }

  private static boolean hasModelType(ModelTuple mt, String expectedType) {
    return expectedType.equals(mt.getHeader().getModelType());
  }

  private static Optional<Annotation> getAnnotation(ModelTuple mt, String annotationName) {
    return mt.getHeader().getAnnotations().stream()
        .filter(annotation -> annotation.getName().equals(annotationName))
        .findAny();
  }
}
