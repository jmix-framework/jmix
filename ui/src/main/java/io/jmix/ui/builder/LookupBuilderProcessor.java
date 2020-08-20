/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.builder;


import io.jmix.core.DataManager;
import io.jmix.core.*;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityOptions;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.Nested;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@Component("ui_LookupBuilderProcessor")
public class LookupBuilderProcessor {

    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected UiProperties properties;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;

    @SuppressWarnings("unchecked")
    public <E extends JmixEntity> Screen buildLookup(LookupBuilder<E> builder) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = getScreenContext(origin).getScreens();

        Screen screen = createScreen(builder, screens);

        if (!(screen instanceof LookupScreen)) {
            throw new IllegalArgumentException(String.format("Screen %s does not implement LookupScreen: %s",
                    screen.getId(), screen.getClass()));
        }

        LookupScreen<E> lookupScreen = (LookupScreen) screen;

        if (builder.getField() != null) {
            HasValue<E> field = builder.getField();

            if (field instanceof io.jmix.ui.component.Component.Focusable) {
                screen.addAfterCloseListener(event -> {
                    // move focus to owner
                    ((io.jmix.ui.component.Component.Focusable) field).focus();
                });
            }
            lookupScreen.setSelectHandler(items ->
                    handleSelectionWithField(builder, field, items)
            );
        }

        CollectionContainer<E> container = null;

        if (builder.getListComponent() != null) {
            ListComponent<E> listComponent = builder.getListComponent();

            if (listComponent instanceof io.jmix.ui.component.Component.Focusable) {
                screen.addAfterCloseListener(event -> {
                    // move focus to owner
                    ((io.jmix.ui.component.Component.Focusable) listComponent).focus();
                });
            }

            if (listComponent.getItems() instanceof ContainerDataUnit) {
                container = ((ContainerDataUnit<E>) listComponent.getItems()).getContainer();
            }
        }

        if (builder.getContainer() != null) {
            container = builder.getContainer();
        }

        if (container != null) {
            CollectionContainer<E> collectionDc = container;

            lookupScreen.setSelectHandler(items ->
                    handleSelectionWithContainer(builder, collectionDc, items)
            );
        }

        if (builder.getSelectHandler() != null) {
            lookupScreen.setSelectHandler(builder.getSelectHandler());
        }

        if (builder.getSelectValidator() != null) {
            lookupScreen.setSelectValidator(builder.getSelectValidator());
        }

        if (builder instanceof LookupClassBuilder) {
            @SuppressWarnings("unchecked")
            Consumer<AfterScreenCloseEvent> closeListener = ((LookupClassBuilder) builder).getCloseListener();
            if (closeListener != null) {
                screen.addAfterCloseListener(new AfterCloseListenerAdapter(closeListener));
            }
        }

        return screen;
    }

    protected <E extends JmixEntity> Screen createScreen(LookupBuilder<E> builder, Screens screens) {
        Screen screen;

        if (builder instanceof LookupClassBuilder) {
            LookupClassBuilder lookupClassBuilder = (LookupClassBuilder) builder;
            @SuppressWarnings("unchecked")
            Class<? extends Screen> screenClass = lookupClassBuilder.getScreenClass();
            if (screenClass == null) {
                throw new IllegalArgumentException("Screen class is not set");
            }

            screen = screens.create(screenClass, builder.getLaunchMode(), builder.getOptions());
        } else {
            String lookupScreenId;
            if (builder.getScreenId() != null) {
                lookupScreenId = builder.getScreenId();
            } else {
                lookupScreenId = windowConfig.getLookupScreen(builder.getEntityClass()).getId();
            }

            if (lookupScreenId == null) {
                throw new IllegalArgumentException("Screen id is not set");
            }

            screen = screens.create(lookupScreenId, builder.getLaunchMode(), builder.getOptions());
        }
        return screen;
    }

    @SuppressWarnings("unchecked")
    protected <E extends JmixEntity> void handleSelectionWithField(@SuppressWarnings("unused") LookupBuilder<E> builder,
                                                                   HasValue<E> field, Collection<E> itemsFromLookup) {
        if (itemsFromLookup.isEmpty()) {
            return;
        }

        Collection<E> selectedItems = transform(itemsFromLookup, builder);

        JmixEntity newValue = selectedItems.iterator().next();

        FetchPlan fetchPlanForField = properties.isReloadUnfetchedAttributesFromLookupScreens() && metadataTools.isPersistent(newValue.getClass()) ?
                getFetchPlanForField(field) :
                null;
        if (fetchPlanForField != null && !entityStates.isLoadedWithFetchPlan(newValue, fetchPlanForField)) {
            newValue = dataManager.load(Id.of(newValue)).fetchPlan(fetchPlanForField).one();
        }

        if (field instanceof EntityComboBox) {
            EntityComboBox entityComboBox = (EntityComboBox) field;
            Options options = entityComboBox.getOptions();
            if (options instanceof EntityOptions) {
                EntityOptions entityOptions = (EntityOptions) options;
                if (entityOptions.containsItem(newValue)) {
                    entityOptions.updateItem(newValue);
                }
            }
        }

        // In case of PickerField set the value as if the user had set it
        if (field instanceof SupportsUserAction) {
            ((SupportsUserAction<E>) field).setValueFromUser((E) newValue);
        } else {
            field.setValue((E) newValue);
        }
    }

    protected <E extends JmixEntity> void handleSelectionWithContainer(LookupBuilder<E> builder,
                                                                       CollectionContainer<E> collectionDc,
                                                                       Collection<E> itemsFromLookup) {
        if (itemsFromLookup.isEmpty()) {
            return;
        }

        Collection<E> selectedItems = transform(itemsFromLookup, builder);

        boolean initializeMasterReference = false;
        JmixEntity masterItem = null;
        MetaProperty inverseMetaProperty = null;

        // update holder reference if needed
        if (collectionDc instanceof Nested) {
            InstanceContainer masterDc = ((Nested) collectionDc).getMaster();

            String property = ((Nested) collectionDc).getProperty();
            masterItem = masterDc.getItem();

            MetaProperty metaProperty = metadata.getClass(masterItem).getProperty(property);
            inverseMetaProperty = metaProperty.getInverse();

            if (inverseMetaProperty != null
                    && !inverseMetaProperty.getRange().getCardinality().isMany()) {

                Class<?> inversePropClass = extendedEntities.getEffectiveClass(inverseMetaProperty.getDomain());
                Class<?> dcClass = extendedEntities.getEffectiveClass(collectionDc.getEntityMetaClass());

                initializeMasterReference = inversePropClass.isAssignableFrom(dcClass);
            }
        }

        DataContext dataContext = UiControllerUtils.getScreenData(builder.getOrigin()).getDataContext();

        List<E> mergedItems = new ArrayList<>(selectedItems.size());
        FetchPlan viewForCollectionContainer = properties.isReloadUnfetchedAttributesFromLookupScreens() &&
                collectionDc.getEntityMetaClass() != null && metadataTools.isPersistent(collectionDc.getEntityMetaClass()) ?
                getFetchPlanForCollectionContainer(collectionDc, initializeMasterReference, inverseMetaProperty) :
                null;
        for (E item : selectedItems) {
            if (!collectionDc.containsItem(EntityValues.getId(item))) {
                if (viewForCollectionContainer != null && !entityStates.isLoadedWithFetchPlan(item, viewForCollectionContainer)) {
                    //noinspection unchecked
                    item = (E) dataManager.load(Id.of(item)).fetchPlan(viewForCollectionContainer).one();
                }
                // track changes in the related instance
                E mergedItem = dataContext.merge(item);
                if (initializeMasterReference) {
                    // change reference, now it will be marked as modified
                    EntityValues.setValue(mergedItem, inverseMetaProperty.getName(), masterItem);
                }
                mergedItems.add(mergedItem);
            }
        }

        collectionDc.getMutableItems().addAll(mergedItems);
    }

    protected <E extends JmixEntity> Collection<E> transform(Collection<E> selectedItems, LookupBuilder<E> builder) {
        if (builder.getTransformation() != null) {
            return builder.getTransformation().apply(selectedItems);
        }
        return selectedItems;
    }

    /**
     * If the value for a component (e.g. {@link EntityPicker}) is selected from lookup screen then there may be cases
     * when in entities in lookup screen some attributes required in the editor are not loaded.
     * <p>
     * The method evaluates the fetch plan that is used for the entity in the given {@code field}
     *
     * @return a view or null if the fetch plan cannot be evaluated
     */
    @Nullable
    protected <E extends JmixEntity> FetchPlan getFetchPlanForField(HasValue<E> field) {
        if (field instanceof HasValueSource) {
            ValueSource valueSource = ((HasValueSource) field).getValueSource();
            if (valueSource instanceof ContainerValueSource) {
                ContainerValueSource containerValueSource = (ContainerValueSource) valueSource;
                InstanceContainer<E> container = containerValueSource.getContainer();
                FetchPlan fetchPlan = container.getFetchPlan();
                if (fetchPlan != null) {
                    MetaPropertyPath metaPropertyPath = containerValueSource.getMetaPropertyPath();
                    FetchPlan curFetchPlan = fetchPlan;
                    for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
                        FetchPlanProperty viewProperty = curFetchPlan.getProperty(metaProperty.getName());
                        if (viewProperty != null) {
                            curFetchPlan = viewProperty.getFetchPlan();
                        }
                        if (curFetchPlan == null) break;
                    }
                    if (curFetchPlan != fetchPlan) {
                        return curFetchPlan;
                    }
                }
            }
        }
        return null;
    }

    /**
     * See {@link #getFetchPlanForField(HasValue)} javadoc.
     *
     * @return a fetch plan or null if the fetch plan cannot be evaluated
     */
    @Nullable
    protected <E extends JmixEntity> FetchPlan getFetchPlanForCollectionContainer(CollectionContainer<E> collectionDc,
                                                                                  boolean initializeMasterReference,
                                                                                  @Nullable MetaProperty inverseMetaProperty) {
        FetchPlan fetchPlan = null;
        if (collectionDc instanceof Nested) {
            InstanceContainer masterDc = ((Nested) collectionDc).getMaster();
            FetchPlan masterFetchPlan = masterDc.getFetchPlan();
            if (masterFetchPlan != null) {
                String property = ((Nested) collectionDc).getProperty();
                FetchPlanProperty viewProperty = masterFetchPlan.getProperty(property);
                if (viewProperty != null) {
                    fetchPlan = viewProperty.getFetchPlan();
                    if (fetchPlan != null && initializeMasterReference && inverseMetaProperty != null) {
                        fetchPlan.addProperty(inverseMetaProperty.getName());
                    }
                }
            }
        } else {
            fetchPlan = collectionDc.getFetchPlan();
        }
        return fetchPlan;
    }
}
