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
package com.mgmtp.a12.rmc.kernel;

import com.mgmtp.a12.dataservices.wcf.WorkspaceConverter;
import com.mgmtp.a12.dataservices.wcf.annotations.WcfConverter;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.kernel.md.combination.a12internal.KernelModelsWorkspaceConverter;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.rmc.utils.ModelTupleIdentifier;
import java.util.function.Consumer;

@WcfConverter(
    order = 50,
    description =
        "Converts Combination Models to its Runtime version, changes the model-type of mapping precomputation models, removes remaining additive fragment dms and selection models")
public class KernelModelsConverter implements WorkspaceConverter {
  @Override
  public Workspace convert(Workspace workspace) {
    Consumer<RankedNotification> notificationReceiver =
        notification -> {
          System.err.printf("[%s] %s%n", notification.getSeverity(), notification.getMessage());

          if (notification.getSeverity() == Severity.ERROR) {
            throw new RuntimeException(notification.getMessage());
          }
        };

    Workspace adaptedWorkspace =
        new KernelModelsWorkspaceConverter(notificationReceiver).convert(workspace);

    return removeUsedModelsFromWorkspace(adaptedWorkspace);
  }

  private Workspace removeUsedModelsFromWorkspace(Workspace workspace) {
    // remove all models that are managed by kernel and are no longer needed
    workspace
        .getModels()
        .entrySet()
        .removeIf(
            entry ->
                ModelTupleIdentifier.isSelectionModel(entry.getValue())
                    || ModelTupleIdentifier.isTypeDefinitionModel(entry.getValue()));

    return workspace;
  }
}
