package io.jmix.flowui.screen.builder;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.DevelopmentException;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.FlowUiScreenProperties;
import io.jmix.flowui.Screens;
import io.jmix.flowui.component.SupportsUserAction;
import io.jmix.flowui.data.*;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.Nested;
import io.jmix.flowui.screen.*;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.flowui.screen.UiControllerUtils.getScreenData;


@Internal
@Component("flowui_EditorWindowBuilderProcessor")
public class EditorWindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected Metadata metadata;
    protected ExtendedEntities extendedEntities;
    protected List<EditedEntityTransformer> editedEntityTransformers;
    protected FlowUiScreenProperties screenProperties;

    public EditorWindowBuilderProcessor(ApplicationContext applicationContext,
                                        Screens screens,
                                        ScreenRegistry screenRegistry,
                                        Metadata metadata,
                                        ExtendedEntities extendedEntities,
                                        FlowUiScreenProperties screenProperties,
                                        @Nullable List<EditedEntityTransformer> editedEntityTransformers) {
        super(applicationContext, screens, screenRegistry);

        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
        this.screenProperties = screenProperties;
        this.editedEntityTransformers = editedEntityTransformers;
    }

    @SuppressWarnings("unchecked")
    public <E, S extends Screen> DialogWindow<S> buildScreen(EditorWindowBuilder<E, S> builder) {

        CollectionContainer<E> container = findContainer(builder);

        E entity = initEntity(builder, container);

        S screen = createScreen(builder);
        ((EditorScreen<E>) screen).setEntityToEdit(entity);

        DialogWindow<S> dialog = createDialog(screen);
        initDialog(builder, dialog);

        DataContext parentDataContext = setupParentDataContext(builder, screen, container);

        setupListDataComponent(builder, ((EditorScreen<E>) screen), dialog, container, parentDataContext);
        setupField(builder, screen, dialog, parentDataContext);

        return dialog;
    }

    protected <E, S extends Screen> void setupListDataComponent(EditorWindowBuilder<E, S> builder,
                                                                EditorScreen<E> editorScreen, DialogWindow<S> dialog,
                                                                @Nullable CollectionContainer<E> container,
                                                                @Nullable DataContext parentDataContext) {
        if (container == null) {
            return;
        }

        dialog.addAfterCloseListener(closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
                E entityFromEditor = getCommittedEntity(editorScreen, parentDataContext);
                E reloadedEntity = transformForCollectionContainer(entityFromEditor, container);
                E committedEntity = transform(reloadedEntity, builder);
                E mergedEntity = merge(committedEntity, builder.getOrigin(), parentDataContext);

                if (builder.getMode() == EditMode.CREATE) {
                    boolean addsFirst = false;

                    if (!(container instanceof Nested)) {
                        addsFirst = screenProperties.isCreateActionAddsFirst();
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
    protected <E, S extends Screen> void setupField(EditorWindowBuilder<E, S> builder,
                                                    S screen, DialogWindow<S> dialog,
                                                    @Nullable DataContext parentDataContext) {
        builder.getField().ifPresent(field -> {
            setupScreenDatContext(field, builder.getOrigin(), screen, parentDataContext);
            dialog.addAfterCloseListener(createAfterCloseListener(field, builder, (EditorScreen<E>) screen));
        });
    }

    @SuppressWarnings("unchecked")
    protected <E, S extends Screen> Consumer<AfterCloseEvent<S>> createAfterCloseListener(HasValue<?, E> field,
                                                                                          EditorWindowBuilder<E, S> builder,
                                                                                          EditorScreen<E> editorScreen) {
        return closeEvent -> {
            if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
                E entityFromEditor = editorScreen.getEditedEntity();
                E reloadedEntity = transformForField(entityFromEditor, field);
                E editedEntity = transform(reloadedEntity, builder);

                if (field instanceof SupportsListOptions) {
                    SupportsListOptions<E> supportsListOptions = ((SupportsListOptions<E>) field);
                    Options<E> options = supportsListOptions.getListOptions();
                    if (options instanceof EntityOptions) {
                        EntityOptions<E> entityOptions = (EntityOptions<E>) options;
                        if (entityOptions.containsItem(editedEntity)) {
                            entityOptions.updateItem(editedEntity);
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

    protected <E, S extends Screen> void setupScreenDatContext(HasValue<?, E> field,
                                                               Screen origin, S screen,
                                                               @Nullable DataContext parentDataContext) {
        if (parentDataContext == null && field instanceof SupportsValueSource) {
            ValueSource<?> valueSource = ((SupportsValueSource<?>) field).getValueSource();
            if (valueSource instanceof EntityValueSource) {
                if (isCompositionProperty((EntityValueSource<?, ?>) valueSource)) {
                    DataContext originDataContext = getScreenData(origin).getDataContext();
                    DataContext screenDataContext = getScreenData(screen).getDataContextOrNull();
                    checkDataContext(screen, screenDataContext);
                    //noinspection ConstantConditions
                    screenDataContext.setParent(originDataContext);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <S extends Screen> Class<S> inferScreenClass(DialogWindowBuilder<S> builder) {
        EditorWindowBuilder<?, S> editorBuilder = ((EditorWindowBuilder<?, S>) builder);
        return (Class<S>) screenRegistry.getEditorScreen(editorBuilder.getEntityClass()).getControllerClass();
    }

    @Nullable
    protected <E, S extends Screen> DataContext setupParentDataContext(EditorWindowBuilder<E, S> builder,
                                                                       S screen,
                                                                       @Nullable CollectionContainer<E> container) {
        DataContext dataContext = builder.getParentDataContext().orElseGet(() -> {
            if (container instanceof Nested) {
                InstanceContainer<?> masterContainer = ((Nested) container).getMaster();
                String property = ((Nested) container).getProperty();

                MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
                MetaProperty metaProperty = masterMetaClass.getProperty(property);

                if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                    return getScreenData(builder.getOrigin()).getDataContextOrNull();
                }
            }

            return null;
        });

        if (dataContext != null) {
            DataContext childContext = getScreenData(screen).getDataContextOrNull();
            checkDataContext(screen, childContext);
            //noinspection ConstantConditions
            childContext.setParent(dataContext);
        }

        return dataContext;
    }

    protected void checkDataContext(Screen screen, @Nullable DataContext dataContext) {
        if (dataContext == null) {
            throw new DevelopmentException(
                    String.format("No DataContext in screen '%s'. Composition editing is impossible.", screen.getId()));
        }
    }

    @Nullable
    protected <E> CollectionContainer<E> findContainer(EditorWindowBuilder<E, ?> builder) {
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

    protected <E> E initEntity(EditorWindowBuilder<E, ?> builder,
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

        if (builder.getMode() == EditMode.CREATE
                || (oneToOneComposition && field.getValue() == null)) {
            return initNewEntity(builder, container, entityValueSource, oneToOneComposition);
        } else {
            return initEditedEntity(builder);
        }
    }

    protected <E> E initNewEntity(EditorWindowBuilder<E, ?> builder, @Nullable CollectionContainer<E> container,
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

    protected <E> E initEditedEntity(EditorWindowBuilder<E, ?> builder) {
        return builder.getEditedEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Editor of %s cannot be open with mode EDIT, entity is not set",
                        builder.getEntityClass())));
    }

    protected boolean isCompositionProperty(EntityValueSource<?, ?> entityValueSource) {
        MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();
        return metaPropertyPath.getMetaProperty().getType() == MetaProperty.Type.COMPOSITION;
    }

    protected <E> E transform(E entity, EditorWindowBuilder<E, ?> builder) {
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

    protected <E> E getCommittedEntity(EditorScreen<E> editorScreen, @Nullable DataContext parentDataContext) {
        E editedEntity = editorScreen.getEditedEntity();
        if (parentDataContext != null) {
            E trackedEntity = parentDataContext.find(editedEntity);
            if (trackedEntity != null) { // makes sense for NoopDataContext
                return trackedEntity;
            }
        }
        return editedEntity;
    }

    protected <E> E merge(E entity, Screen origin, @Nullable DataContext parentDataContext) {
        if (parentDataContext == null) {
            DataContext thisDataContext = getScreenData(origin).getDataContextOrNull();
            if (thisDataContext != null) {
                return thisDataContext.merge(entity);
            }
        }
        return entity;
    }
}
