/*
 * Copyright 2023 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.bulkeditor.view.builder;

import com.vaadin.flow.component.Focusable;
import io.jmix.bulkeditor.view.BulkEditView;
import io.jmix.bulkeditor.view.BulkEditViewContext;
import io.jmix.core.DevelopmentException;
import io.jmix.core.EntitySet;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.EditedEntityTransformer;
import io.jmix.flowui.view.builder.WindowBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.flowui.view.ViewControllerUtils.getViewData;

/**
 * A bean that creates an instance of {@link BulkEditorBuilder}.
 */
@Component("bulked_BulkEditors")
public class BulkEditors {

    protected final DialogWindows dialogWindows;
    protected List<EditedEntityTransformer> editedEntityTransformers;

    public BulkEditors(DialogWindows dialogWindows, List<EditedEntityTransformer> editedEntityTransformers) {
        this.dialogWindows = dialogWindows;
        this.editedEntityTransformers = editedEntityTransformers;
    }

    public <E> BulkEditorBuilder<E> builder(MetaClass metaClass, Collection<E> entities, View<?> origin) {
        checkNotNullArgument(metaClass);
        checkNotNullArgument(entities);
        checkNotNullArgument(origin);

        return new BulkEditorBuilder<>(metaClass, entities, origin, this::buildEditor);
    }

    protected <E> DialogWindow<BulkEditView<E>> buildEditor(BulkEditorBuilder<E> builder) {
        if (CollectionUtils.isEmpty(builder.getEntities())) {
            throw new IllegalStateException(String.format("BulkEditor of %s cannot be open with no entities set",
                    builder.getMetaClass()));
        }

        //noinspection rawtypes
        WindowBuilder windowBuilder = dialogWindows.view(builder.getOrigin(), BulkEditView.class);
        //noinspection unchecked
        DialogWindow<BulkEditView<E>> dialogWindow = ((WindowBuilder<BulkEditView<E>>) windowBuilder)
                .withAfterCloseListener(createAfterCloseHandler(builder))
                .build();

        BulkEditView<E> bulkEditorWindow = dialogWindow.getView();
        setupParentDataContext(builder, bulkEditorWindow);

        BulkEditViewContext<E> context = createBulkEditorContext(builder);
        bulkEditorWindow.setBulkEditorContext(context);

        return dialogWindow;
    }

    protected <E, V extends BulkEditView<E>> Consumer<DialogWindow.AfterCloseEvent<V>> createAfterCloseHandler(
            BulkEditorBuilder<E> builder) {
        return afterCloseEvent -> {
            ListDataComponent<E> listDataComponent = builder.getListDataComponent();
            if (afterCloseEvent.closedWith(StandardOutcome.SAVE)
                    && listDataComponent != null
                    && listDataComponent.getItems() instanceof ContainerDataUnit<?> containerDataUnit) {
                replaceItems(containerDataUnit.getContainer(), afterCloseEvent.getView(), builder);
            }

            if (builder.getListDataComponent() instanceof Focusable<?> focusable) {
                focusable.focus();
            }
        };
    }

    protected <E> void replaceItems(CollectionContainer<E> collectionContainer, BulkEditView<?> view,
                                    BulkEditorBuilder<?> builder) {
        EntitySet savedItems = view.getSavedItems();
        if (savedItems == null) {
            return;
        }

        Collection<E> saved = savedItems.getAll(collectionContainer.getEntityMetaClass().getJavaClass());
        saved.stream()
                .map(item -> transformForCollectionContainer(item, collectionContainer))
                .map(item -> merge(item, builder.getOrigin(), view, collectionContainer))
                .forEach(collectionContainer::replaceItem);
    }

    protected <E> E transformForCollectionContainer(E entity, CollectionContainer<E> container) {
        E result = entity;
        if (CollectionUtils.isNotEmpty(editedEntityTransformers)) {
            for (EditedEntityTransformer transformer : editedEntityTransformers) {
                result = transformer.transformForCollectionContainer(result, container);
            }
        }

        return result;
    }

    protected <E> E merge(E entity, View<?> origin, View<?> target,
                          @Nullable CollectionContainer<E> container) {
        DataContext parentDataContext = getViewData(target).getDataContext().getParent();
        if (parentDataContext == null && isContainerLinkedWithDataContext(container)) {
            DataContext thisDataContext = getViewData(origin).getDataContextOrNull();
            if (thisDataContext != null) {
                return thisDataContext.merge(entity);
            }
        }

        return entity;
    }

    protected <E> boolean isContainerLinkedWithDataContext(@Nullable InstanceContainer<E> container) {
        if (container instanceof HasLoader standaloneContainer) {
            DataLoader loader = standaloneContainer.getLoader();
            return loader != null && loader.getDataContext() != null;
        }
        if (container instanceof Nested nestedContainer) {
            InstanceContainer<?> masterContainer = nestedContainer.getMaster();
            return isContainerLinkedWithDataContext(masterContainer);
        }
        return false;
    }

    protected <E> BulkEditViewContext<E> createBulkEditorContext(BulkEditorBuilder<E> builder) {
        BulkEditViewContext<E> context = new BulkEditViewContext<>(builder.getMetaClass(), builder.getEntities());
        context.setExclude(builder.getExclude());
        context.setIncludeProperties(builder.getIncludeProperties());
        context.setFieldValidators(builder.getFieldValidators());
        context.setModelValidators(builder.getModelValidators());
        context.setUseConfirmDialog(builder.isUseConfirmDialog());
        context.setFieldSorter(builder.getFieldSorter());

        return context;
    }

    protected <E> void setupParentDataContext(BulkEditorBuilder<E> builder, BulkEditView<E> view) {
        DataContext parentDataContext = builder.getParentDataContext();
        if (parentDataContext == null && builder.getListDataComponent() != null
                && builder.getListDataComponent().getItems() instanceof ContainerDataUnit<?> containerDataUnit
                && containerDataUnit.getContainer() instanceof Nested nestedContainer) {
            InstanceContainer<?> masterContainer = nestedContainer.getMaster();
            String property = nestedContainer.getProperty();

            MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(property);

            if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                parentDataContext = getViewData(builder.getOrigin()).getDataContextOrNull();
            }
        }

        if (parentDataContext != null) {
            DataContext childContext = getViewData(view).getDataContextOrNull();
            checkDataContext(view, childContext);
            //noinspection ConstantConditions
            childContext.setParent(parentDataContext);
        }
    }

    protected void checkDataContext(View<?> view, @Nullable DataContext dataContext) {
        if (dataContext == null) {
            throw new DevelopmentException(
                    String.format("No DataContext in view '%s'. Composition editing is impossible.", view.getId()));
        }
    }
}