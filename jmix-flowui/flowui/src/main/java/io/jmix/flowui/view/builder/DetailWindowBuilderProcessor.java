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
import io.jmix.flowui.FlowUiViewProperties;
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

import static io.jmix.flowui.view.UiControllerUtils.getViewData;


@Internal
@Component("flowui_DetailWindowBuilderProcessor")
public class DetailWindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected Metadata metadata;
    protected ExtendedEntities extendedEntities;
    protected List<EditedEntityTransformer> editedEntityTransformers;
    protected FlowUiViewProperties viewProperties;

    public DetailWindowBuilderProcessor(ApplicationContext applicationContext,
                                        Views views,
                                        ViewRegistry viewRegistry,
                                        Metadata metadata,
                                        ExtendedEntities extendedEntities,
                                        FlowUiViewProperties viewProperties,
                                        @Nullable List<EditedEntityTransformer> editedEntityTransformers) {
        super(applicationContext, views, viewRegistry);

        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
        this.viewProperties = viewProperties;
        this.editedEntityTransformers = editedEntityTransformers;
    }

    @SuppressWarnings("unchecked")
    public <E, S extends View<?>> DialogWindow<S> build(DetailWindowBuilder<E, S> builder) {

        CollectionContainer<E> container = findContainer(builder);

        E entity = initEntity(builder, container);

        S view = createView(builder);
        ((DetailView<E>) view).setEntityToEdit(entity);

        DialogWindow<S> dialog = createDialog(view);
        initDialog(builder, dialog);

        DataContext parentDataContext = setupParentDataContext(builder, view, container);

        setupListDataComponent(builder, ((DetailView<E>) view), dialog, container, parentDataContext);
        setupField(builder, view, dialog, parentDataContext);

        return dialog;
    }

    protected <E, S extends View<?>> void setupListDataComponent(DetailWindowBuilder<E, S> builder,
                                                                 DetailView<E> detailView, DialogWindow<S> dialog,
                                                                 @Nullable CollectionContainer<E> container,
                                                                 @Nullable DataContext parentDataContext) {
        if (container == null) {
            return;
        }

        dialog.addAfterCloseListener(closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
                E entityFromDetail = getCommittedEntity(detailView, parentDataContext);
                E reloadedEntity = transformForCollectionContainer(entityFromDetail, container);
                E committedEntity = transform(reloadedEntity, builder);
                E mergedEntity = merge(committedEntity, builder.getOrigin(), parentDataContext);

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
    protected <E, S extends View<?>> void setupField(DetailWindowBuilder<E, S> builder,
                                                     S view, DialogWindow<S> dialog,
                                                     @Nullable DataContext parentDataContext) {
        builder.getField().ifPresent(field -> {
            setupViewDataContext(field, builder.getOrigin(), view, parentDataContext);
            dialog.addAfterCloseListener(createAfterCloseListener(field, builder, (DetailView<E>) view));
        });
    }

    @SuppressWarnings("unchecked")
    protected <E, S extends View<?>> Consumer<AfterCloseEvent<S>> createAfterCloseListener(HasValue<?, E> field,
                                                                                           DetailWindowBuilder<E, S> builder,
                                                                                           DetailView<E> detailView) {
        return closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
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

    protected <E, S extends View<?>> void setupViewDataContext(HasValue<?, E> field,
                                                               View<?> origin, S view,
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
    protected <S extends View<?>> Class<S> inferViewClass(DialogWindowBuilder<S> builder) {
        DetailWindowBuilder<?, S> detailBuilder = ((DetailWindowBuilder<?, S>) builder);
        return (Class<S>) viewRegistry.getDetailViewInfo(detailBuilder.getEntityClass()).getControllerClass();
    }

    @Nullable
    protected <E, S extends View<?>> DataContext setupParentDataContext(DetailWindowBuilder<E, S> builder,
                                                                        S view,
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

    protected <E> E getCommittedEntity(DetailView<E> detailView, @Nullable DataContext parentDataContext) {
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
