/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view.builder;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.core.DevelopmentException;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.Views;
import io.jmix.flowui.data.*;
import io.jmix.flowui.kit.component.SupportsUserAction;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.Nested;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.flowui.view.ViewControllerUtils.getViewData;


@Internal
@Component("flowui_DetailWindowBuilderProcessor")
public class DetailWindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected Metadata metadata;
    protected ExtendedEntities extendedEntities;
    protected List<EditedEntityTransformer> editedEntityTransformers;
    protected FlowuiViewProperties viewProperties;

    public DetailWindowBuilderProcessor(ApplicationContext applicationContext,
                                        Views views,
                                        ViewRegistry viewRegistry,
                                        Metadata metadata,
                                        ExtendedEntities extendedEntities,
                                        FlowuiViewProperties viewProperties,
                                        @Nullable List<EditedEntityTransformer> editedEntityTransformers) {
        super(applicationContext, views, viewRegistry);

        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
        this.viewProperties = viewProperties;
        this.editedEntityTransformers = editedEntityTransformers;
    }

    @SuppressWarnings("unchecked")
    public <E, V extends View<?>> DialogWindow<V> build(DetailWindowBuilder<E, V> builder) {

        CollectionContainer<E> container = findContainer(builder);

        E entity = initEntity(builder, container);

        V view = createView(builder);
        ((DetailView<E>) view).setEntityToEdit(entity);

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        DataContext parentDataContext = setupParentDataContext(builder, view, container);

        setupListDataComponent(builder, ((DetailView<E>) view), dialog, container, parentDataContext);
        setupField(builder, view, dialog, parentDataContext);

        return dialog;
    }

    protected <E, V extends View<?>> void setupListDataComponent(DetailWindowBuilder<E, V> builder,
                                                                 DetailView<E> detailView, DialogWindow<V> dialog,
                                                                 @Nullable CollectionContainer<E> container,
                                                                 @Nullable DataContext parentDataContext) {
        if (container == null) {
            return;
        }

        dialog.addAfterCloseListener(closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                E entityFromDetail = getSavedEntity(detailView, parentDataContext);
                E reloadedEntity = transformForCollectionContainer(entityFromDetail, container);
                E savedEntity = transform(reloadedEntity, builder);
                E mergedEntity = merge(savedEntity, builder.getOrigin(), parentDataContext);

                if (builder.getMode() == DetailViewMode.CREATE) {
                    boolean addsFirst = false;

                    if (!(container instanceof Nested)) {
                        addsFirst = viewProperties.isCreateActionAddsFirst();
                        if (builder.getAddFirst() != null) {
                            addsFirst = builder.getAddFirst();
                        }
                    }

                    if (container instanceof Nested || !addsFirst) {
                        container.getMutableItems().add(mergedEntity);
                    } else {
                        container.getMutableItems().add(0, mergedEntity);
                    }
                } else {
                    container.replaceItem(mergedEntity);
                }
            }

            builder.getListDataComponent().ifPresent(listDataComponent -> {
                if (listDataComponent instanceof Focusable) {
                    ((Focusable<?>) listDataComponent).focus();
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    protected <E, V extends View<?>> void setupField(DetailWindowBuilder<E, V> builder,
                                                     V view, DialogWindow<V> dialog,
                                                     @Nullable DataContext parentDataContext) {
        builder.getField().ifPresent(field -> {
            setupViewDataContext(field, builder.getOrigin(), view, parentDataContext);
            dialog.addAfterCloseListener(createAfterCloseListener(field, builder, (DetailView<E>) view));
        });
    }

    @SuppressWarnings("unchecked")
    protected <E, V extends View<?>> Consumer<AfterCloseEvent<V>> createAfterCloseListener(HasValue<?, E> field,
                                                                                           DetailWindowBuilder<E, V> builder,
                                                                                           DetailView<E> detailView) {
        return closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                E entityFromDetail = detailView.getEditedEntity();
                E reloadedEntity = transformForField(entityFromDetail, field);
                E editedEntity = transform(reloadedEntity, builder);

                if (field instanceof SupportsDataProvider) {
                    SupportsDataProvider<E> supportsDataProvider = ((SupportsDataProvider<E>) field);
                    DataProvider<E, ?> dataProvider = supportsDataProvider.getDataProvider();
                    if (dataProvider instanceof EntityItems) {
                        EntityItems<E> entityItems = (EntityItems<E>) dataProvider;
                        if (entityItems.containsItem(editedEntity)) {
                            entityItems.updateItem(editedEntity);
                        }
                    }
                }

                if (field instanceof SupportsUserAction) {
                    ((SupportsUserAction<E>) field).setValueFromClient(editedEntity);
                } else {
                    field.setValue(editedEntity);
                }
            }

            if (field instanceof Focusable) {
                ((Focusable<?>) field).focus();
            }
        };
    }

    protected <E, V extends View<?>> void setupViewDataContext(HasValue<?, E> field,
                                                               View<?> origin, V view,
                                                               @Nullable DataContext parentDataContext) {
        if (parentDataContext == null && field instanceof SupportsValueSource) {
            ValueSource<?> valueSource = ((SupportsValueSource<?>) field).getValueSource();
            if (valueSource instanceof EntityValueSource) {
                if (isCompositionProperty((EntityValueSource<?, ?>) valueSource)) {
                    DataContext originDataContext = getViewData(origin).getDataContext();
                    DataContext dataContext = getViewData(view).getDataContextOrNull();
                    checkDataContext(view, dataContext);
                    //noinspection ConstantConditions
                    dataContext.setParent(originDataContext);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V extends View<?>> Class<V> inferViewClass(DialogWindowBuilder<V> builder) {
        DetailWindowBuilder<?, V> detailBuilder = ((DetailWindowBuilder<?, V>) builder);
        return (Class<V>) viewRegistry.getDetailViewInfo(detailBuilder.getEntityClass()).getControllerClass();
    }

    @Nullable
    protected <E, V extends View<?>> DataContext setupParentDataContext(DetailWindowBuilder<E, V> builder,
                                                                        V view,
                                                                        @Nullable CollectionContainer<E> container) {
        DataContext dataContext = builder.getParentDataContext().orElseGet(() -> {
            if (container instanceof Nested) {
                InstanceContainer<?> masterContainer = ((Nested) container).getMaster();
                String property = ((Nested) container).getProperty();

                MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
                MetaProperty metaProperty = masterMetaClass.getProperty(property);

                if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                    return getViewData(builder.getOrigin()).getDataContextOrNull();
                }
            }

            return null;
        });

        if (dataContext != null) {
            DataContext childContext = getViewData(view).getDataContextOrNull();
            checkDataContext(view, childContext);
            //noinspection ConstantConditions
            childContext.setParent(dataContext);
        }

        return dataContext;
    }

    protected void checkDataContext(View<?> view, @Nullable DataContext dataContext) {
        if (dataContext == null) {
            throw new DevelopmentException(
                    String.format("No DataContext in view '%s'. Composition editing is impossible.", view.getId()));
        }
    }

    @Nullable
    protected <E> CollectionContainer<E> findContainer(DetailWindowBuilder<E, ?> builder) {
        return builder.getContainer().orElseGet(() ->
                builder.getListDataComponent()
                        .map(listDataComponent -> {
                            DataUnit items = listDataComponent.getItems();
                            //noinspection unchecked
                            return items instanceof ContainerDataUnit
                                    ? ((ContainerDataUnit<E>) items).getContainer()
                                    : null;
                        }).orElse(null));
    }

    protected <E> E initEntity(DetailWindowBuilder<E, ?> builder,
                               @Nullable CollectionContainer<E> container) {
        boolean oneToOneComposition = false;
        EntityValueSource<?, ?> entityValueSource = null;

        HasValue<?, E> field = builder.getField().orElse(null);
        if (field instanceof SupportsValueSource) {
            ValueSource<?> valueSource = ((SupportsValueSource<?>) field).getValueSource();
            if (valueSource instanceof EntityValueSource) {
                entityValueSource = (EntityValueSource<?, ?>) valueSource;
                oneToOneComposition = isCompositionProperty(entityValueSource);
            }
        }

        if (builder.getMode() == DetailViewMode.CREATE
                || (oneToOneComposition && field.getValue() == null)) {
            return initNewEntity(builder, container, entityValueSource, oneToOneComposition);
        } else {
            return initEditedEntity(builder);
        }
    }

    protected <E> E initNewEntity(DetailWindowBuilder<E, ?> builder, @Nullable CollectionContainer<E> container,
                                  @Nullable EntityValueSource<?, ?> entityValueSource, boolean oneToOneComposition) {
        E entity = builder.getNewEntity()
                .orElse(metadata.create(builder.getEntityClass()));

        if (container instanceof Nested) {
            initializeNestedEntity(entity, (Nested) container);
        }

        if (oneToOneComposition && entityValueSource != null) {
            Object ownerEntity = entityValueSource.getItem();
            MetaProperty inverseProp = entityValueSource.getMetaPropertyPath().getMetaProperty().getInverse();
            if (inverseProp != null) {
                EntityValues.setValue(entity, inverseProp.getName(), ownerEntity);
            }
        }

        builder.getInitializer().ifPresent(initializer -> initializer.accept(entity));

        return entity;
    }

    protected <E> void initializeNestedEntity(E entity, Nested container) {
        InstanceContainer<?> masterContainer = container.getMaster();
        String property = container.getProperty();

        MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
        MetaProperty metaProperty = masterMetaClass.getProperty(property);

        MetaProperty inverseProp = metaProperty.getInverse();
        if (inverseProp != null && !inverseProp.getRange().getCardinality().isMany()) {
            Class<?> inversePropClass = extendedEntities.getEffectiveClass(inverseProp.getDomain());
            Class<?> containerEntityClass = extendedEntities
                    .getEffectiveClass(((CollectionContainer<?>) container).getEntityMetaClass());
            if (inversePropClass.isAssignableFrom(containerEntityClass)) {
                EntityValues.setValue(entity, inverseProp.getName(), masterContainer.getItem());
            }
        }
    }

    protected <E> E initEditedEntity(DetailWindowBuilder<E, ?> builder) {
        return builder.getEditedEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Detail View of %s cannot be open with mode EDIT, entity is not set",
                        builder.getEntityClass())));
    }

    protected boolean isCompositionProperty(EntityValueSource<?, ?> entityValueSource) {
        MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();
        return metaPropertyPath.getMetaProperty().getType() == MetaProperty.Type.COMPOSITION;
    }

    protected <E> E transform(E entity, DetailWindowBuilder<E, ?> builder) {
        return builder.getTransformation()
                .map(transformation ->
                        transformation.apply(entity))
                .orElse(entity);
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

    protected <E> E transformForField(E entity, HasValue<?, E> field) {
        E result = entity;
        if (CollectionUtils.isNotEmpty(editedEntityTransformers)) {
            for (EditedEntityTransformer transformer : editedEntityTransformers) {
                result = transformer.transformForField(result, field);
            }
        }
        return result;
    }

    protected <E> E getSavedEntity(DetailView<E> detailView, @Nullable DataContext parentDataContext) {
        E editedEntity = detailView.getEditedEntity();
        if (parentDataContext != null) {
            E trackedEntity = parentDataContext.find(editedEntity);
            if (trackedEntity != null) { // makes sense for NoopDataContext
                return trackedEntity;
            }
        }
        return editedEntity;
    }

    protected <E> E merge(E entity, View<?> origin, @Nullable DataContext parentDataContext) {
        if (parentDataContext == null) {
            DataContext thisDataContext = getViewData(origin).getDataContextOrNull();
            if (thisDataContext != null) {
                return thisDataContext.merge(entity);
            }
        }
        return entity;
    }
}
