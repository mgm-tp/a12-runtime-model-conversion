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
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NotificationConsumer implements Consumer<RankedNotification> {
  private final List<RankedNotification> notifications = new ArrayList<>();

  public void accept(RankedNotification notification) {
    notifications.add(notification);
  }

  public boolean hasProblemOccurred() {
    return !notifications.isEmpty()
        && notifications.stream().anyMatch(n -> n.getSeverity() == Severity.ERROR);
  }

  public List<RankedNotification> getNotifications() {
    return notifications;
  }

  /**
   * Throws a RuntimeException if any problems have been reported, including the formatted problem
   * details in the exception message.
   */
  public void validate(String message) {
    if (hasProblemOccurred()) {
      var details =
          notifications.stream()
              .map(
                  p ->
                      switch (p) {
                        case IProblem problem ->
                            "%s [%s,L%d,s%d,e%d]"
                                .formatted(
                                    problem.getMessage(),
                                    problem.getSeverity(),
                                    problem.getLine(),
                                    problem.getSourceStart(),
                                    problem.getSourceEnd());
                        default -> "%s [%s]".formatted(p.getMessage(), p.getSeverity());
                      })
              .reduce((a, b) -> a + "\n" + b)
              .orElse("");
      throw new IllegalStateException(message + ": " + details);
    }
  }
}
