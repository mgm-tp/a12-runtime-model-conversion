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

import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationException;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.DMWrapper;
import com.mgmtp.a12.kernel.md.combination.a12internal.IUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content.CombinationSteps;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content._combinationsteps.Type;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.rmc.kernelextensions.NotificationConsumer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Base implementation of {@link DocumentModelMetadataInjector} providing common metadata injection
 * and removal logic. Subclasses customize the joining items and post-join metadata additions.
 */
public class AbstractDocumentModelMetadataInjector implements DocumentModelMetadataInjector {

  private final IDocumentModel documentModel;
  private final DocumentModelService documentModelService;
  private final Locale locale;

  public AbstractDocumentModelMetadataInjector(
      IDocumentModel documentModel, DocumentModelService documentModelService, Locale locale) {
    this.documentModel = documentModel;
    this.documentModelService = documentModelService;
    this.locale = locale;
  }

  @Override
  public IDocumentModel getDocumentModelWithMetadata(IDocumentModel documentMetadataModel) {
    DocumentModel originalDocumentModel = getInternalDocumentModelCopy();
    Optional<DocumentModel> documentMetadataModelInternal =
        Optional.ofNullable(documentMetadataModel).map(documentModelService::convertFromExternal);

    List<Pair<Type, DocumentModel>> combinationSteps = new ArrayList<>();
    removeMetadata(originalDocumentModel);
    documentMetadataModelInternal.ifPresent(m -> customizeCombinationSteps(combinationSteps, m));
    DocumentModel enrichedModel =
        doKernelCombination(originalDocumentModel, locale, combinationSteps);

    getDocumentMetadataGroup(documentMetadataModelInternal)
        .ifPresent(documentMetadataGroup -> addMetadata(documentMetadataGroup, enrichedModel));

    return documentModelService.convertToExternal(enrichedModel);
  }

  @Override
  public IDocumentModel getDocumentModelWithoutMetadata() {
    return removeMetadata(getInternalDocumentModelCopy());
  }

  /**
   * Applies specific logic to metadata for a particular type of document. Intended for extending
   * default logic. Used for adding CDM sub-document metadata, for example.
   */
  protected void addMetadata(Group documentMetadataGroup, DocumentModel enrichedModel) {
    // By default, do nothing. Subclasses add extra metadata elements.
  }

  /** Used to extend kernel combination expansion. Subclasses add extra steps such as additions. */
  protected void customizeCombinationSteps(
      List<Pair<Type, DocumentModel>> combinationSteps,
      DocumentModel documentMetadataModelInternal) {
    // By default, do nothing. Subclasses add extra combination steps.
  }

  /** Returns a copy of the document model in Kernel's internal form. */
  protected DocumentModel getInternalDocumentModelCopy() {
    return DocumentModelCopyService.copy(
        documentModelService.convertFromExternal(getDocumentModel()));
  }

  /**
   * Creates a copy of the given Group and adds the parent group ID as a prefix to the ID of all
   * elements within the copied group.
   */
  protected Group copyGroupAndAddParentIdPrefixToAllElements(
      Group originalGroup, Group parentGroup) {
    Group g = DocumentModelCopyService.copy(originalGroup);
    UniqueFieldIdVisitor visitor = new UniqueFieldIdVisitor(parentGroup.getId());
    new DocumentModelWalker().acceptElements(List.of(g), visitor);
    return g;
  }

  /**
   * Shortcut to call DocumentModelWalker and return the visitor in the chain. Useful if the visitor
   * collects some data.
   */
  protected static <T extends DocumentModelVisitor> T getVisitorAfterWalk(
      DocumentModel documentModel, T visitor) {
    new DocumentModelWalker().acceptDocumentModel(documentModel, visitor);
    return visitor;
  }

  protected IDocumentModel getDocumentModel() {
    return documentModel;
  }

  protected Locale getLocale() {
    return locale;
  }

  private IDocumentModel removeMetadata(DocumentModel internalModel) {
    MetadataRemovingVisitor visitor = new MetadataRemovingVisitor();
    getVisitorAfterWalk(internalModel, visitor);
    visitor.getGroupsToRemove().forEach(g -> g.getParent().removeElement(g));
    return documentModelService.convertToExternal(internalModel);
  }

  private DocumentModel doKernelCombination(
      DocumentModel documentModel,
      Locale locale,
      List<Pair<Type, DocumentModel>> combinationSteps) {
    Header originalHeader = documentModel.getHeader();

    NotificationConsumer consumer = new NotificationConsumer();

    List<DocumentModel> usedDMs =
        Stream.concat(Stream.of(documentModel), combinationSteps.stream().map(Pair::getRight))
            .toList();

    List<CombinationSteps> steps =
        combinationSteps.stream()
            .map(
                step ->
                    CombinationSteps._empty()
                        ._with(CombinationSteps._pointer().type(), step.getLeft())
                        ._with(
                            CombinationSteps._pointer().additiveModel().dmId(),
                            step.getRight().getHeader().getId()))
            .toList();

    MM_CombinationModel_1 combinationModel =
        MM_CombinationModel_1._empty()
            ._with(
                MM_CombinationModel_1._pointer().header().id(),
                "CM_" + documentModel.getHeader().getId())
            ._with(
                MM_CombinationModel_1._pointer().content().baseModelId(),
                documentModel.getHeader().getId())
            ._with(MM_CombinationModel_1._pointer().content().combinationSteps(), steps);

    combinationModel = CombinationModelService.fillComputableFields(combinationModel);

    IUnexpandedModelResolver unexpandedModelResolver = createUnexpandedModelResolver(usedDMs);
    Optional<IDocumentModel> expanded =
        CombinationModelService.expand(
            combinationModel,
            unexpandedModelResolver,
            CombinationModelService.CombinationModelExpandParams.builder()
                .notificationReceiver(consumer)
                .locale(locale)
                .build());
    DocumentModel enrichedModel =
        expanded.map(documentModelService::convertFromExternal).orElse(documentModel);

    // as the default logic during expansion is to use the header of the combination model (with
    // some parts from the base dm)
    // we have to reset it to only use the original header of the dm.
    enrichedModel.setHeader(originalHeader);
    enrichedModel
        .getContent()
        .getModelInfo()
        .setName(documentModel.getContent().getModelInfo().getName());

    consumer.validate("Error while joining document models");
    return enrichedModel;
  }

  private IUnexpandedModelResolver createUnexpandedModelResolver(List<DocumentModel> loadedDMs) {
    // only the already resolved models need to be used
    var loadedModels =
        loadedDMs.stream()
            .collect(Collectors.toMap(dm -> dm.getHeader().getId(), Function.identity()));

    return dmId -> {
      if (loadedModels.containsKey(dmId)) {
        return new DMWrapper(documentModelService.convertToExternal(loadedModels.get(dmId)));
      } else {
        throw new CombinationException("Unexpected model ID: " + dmId);
      }
    };
  }

  private static Optional<Group> getDocumentMetadataGroup(
      Optional<DocumentModel> documentMetadataModelInternal) {
    return documentMetadataModelInternal
        .map(DocumentModel::getContent)
        .map(DocumentModelContent::getModelRoot)
        .map(Group::getElements)
        .stream()
        .flatMap(Collection::stream)
        .filter(Group.class::isInstance)
        .filter(g -> MetadataConstants.DOCUMENT_METADATA_GROUP_NAME.equals(g.getName()))
        .map(Group.class::cast)
        .findAny();
  }

  private static class MetadataRemovingVisitor extends DocumentModelVisitor {

    private final Set<Group> groupsToRemove = new HashSet<>();

    @Override
    public VisitProcess visitGroup(Group group) {
      if (MetadataConstants.META_GROUP_NAME_PATTERN.matcher(group.getName()).matches()) {
        groupsToRemove.add(group);
        return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
      }
      return super.visitGroup(group);
    }

    public Set<Group> getGroupsToRemove() {
      return groupsToRemove;
    }
  }

  private static class UniqueFieldIdVisitor extends DocumentModelVisitor {
    private final String metaGroupId;

    UniqueFieldIdVisitor(String metaGroupId) {
      this.metaGroupId = metaGroupId;
    }

    @Override
    public VisitProcess visitElement(Element element) {
      element.setId(metaGroupId + "_" + element.getId());
      return super.visitElement(element);
    }
  }
}
