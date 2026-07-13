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
package com.mgmtp.a12.rmc.kernelextensions;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import java.util.ArrayList;
import java.util.List;

public class ProblemReporter implements IProblemReporter {
  private final List<IProblem> problems = new ArrayList<>();

  public void reportProblem(IProblem problem) {
    problems.add(problem);
  }

  public boolean hasProblemOccurred() {
    return !problems.isEmpty();
  }

  public List<IProblem> getProblems() {
    return problems;
  }

  /**
   * Throws a RuntimeException if any problems have been reported, including the formatted problem
   * details in the exception message.
   */
  public void validate(String message) {
    if (hasProblemOccurred()) {
      var details =
          problems.stream()
              .map(
                  p ->
                      "%s [%s,L%d,s%d,e%d]"
                          .formatted(
                              p.getMessage(),
                              p.getSeverity(),
                              p.getLine(),
                              p.getSourceStart(),
                              p.getSourceEnd()))
              .reduce((a, b) -> a + "\n" + b)
              .orElse("");
      throw new IllegalStateException(message + ": " + details);
    }
  }
}
