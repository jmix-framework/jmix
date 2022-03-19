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


import io.jmix.core.*;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Screens;
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
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@Internal
@Component("ui_LookupBuilderProcessor")
public class LookupBuilderProcessor {

    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected UiScreenProperties screenProperties;
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
    @Autowired
    protected FetchPlans fetchPlans;

    @SuppressWarnings("unchecked")
    public <E, S extends Screen> S buildLookup(LookupBuilder<E> builder) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = getScreenContext(origin).getScreens();

        Screen screen = createScreen(builder, screens);

        if (!(screen instanceof LookupScreen)) {
            throw new IllegalArgumentException(String.format("Screen %s does not implement LookupScreen: %s",
                    screen.getId(), screen.getClass()));
        }

        LookupScreen<E> lookupScreen = (LookupScreen) screen;

        if (builder.getField() != null) {
            HasValue field = builder.getField();
            if (field instanceof io.jmix.ui.component.Component.Focusable) {
                screen.addAfterCloseListener(event -> {
                    // move focus to owner
                    ((io.jmix.ui.component.Component.Focusable) field).focus();
                });
            }

            lookupScreen.setSelectHandler(items ->
                    handleSelectionWithField(builder, field, items, builder.isFieldCollectionValue())
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
            Consumer<AfterScreenShowEvent> afterShowListener = ((LookupClassBuilder) builder).getAfterShowListener();
            if (afterShowListener != null) {
                screen.addAfterShowListener(new AfterShowListenerAdapter(afterShowListener));
            }

            Consumer<AfterScreenCloseEvent> afterCloseListener = ((LookupClassBuilder) builder).getAfterCloseListener();
            if (afterCloseListener != null) {
                screen.addAfterCloseListener(new AfterCloseListenerAdapter(afterCloseListener));
            }
        }

        return (S) screen;
    }

    protected <E> Screen createScreen(LookupBuilder<E> builder, Screens screens) {
        Screen screen;

        if (builder instanceof LookupClassBuilder) {
            LookupClassBuilder lookupClassBuilder = (LookupClassBuilder) builder;
            @SuppressWarnings("unchecked")
            Class<? extends Screen> screenClass = lookupClassBuilder.getScreenClass();
            if (screenClass == null) {
                throw new IllegalArgumentException("Screen class is not set");
            }

            screen = screens.create(screenClass, builder.getOpenMode(), builder.getOptions());
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

            screen = screens.create(lookupScreenId, builder.getOpenMode(), builder.getOptions());
        }
        return screen;
    }

    protected <E> void handleSelectionWithField(@SuppressWarnings("unused") LookupBuilder<E> builder,
                                                HasValue field,
                                                Collection<E> itemsFromLookup,
                                                boolean isCollectionValue) {
        if (itemsFromLookup.isEmpty()) {
            return;
        }
        Collection<E> selectedItems = transform(itemsFromLookup, builder);
        Object newValue = isCollectionValue ? selectedItems : selectedItems.iterator().next();

        FetchPlan fetchPlan = getFetchPlanForField(field);
        if (fetchPlan != null) {
            Collection<E> reloadedItems = reloadItemsByFetchPlan(fetchPlan, toCollection(newValue, isCollectionValue));
            if (!reloadedItems.isEmpty()) {
                newValue = fromCollection(reloadedItems, isCollectionValue);
            }
        }

        if (field instanceof OptionsField) {
            updateFieldOptions((OptionsField) field, toCollection(newValue, isCollectionValue));
        }

        if (isCollectionValue) {
            Collection<E> fieldValue = new ArrayList<>(field.getValue() == null
                    ? Collections.emptyList()
                    : (Collection<E>) field.getValue());

            Collection<E> itemsToAppend = (Collection<E>) newValue;
            for (E item : itemsToAppend) {
                if (!fieldValue.contains(item)) {
                    fieldValue.add(item);
                }
            }

            newValue = fieldValue;
        }

        // In case of PickerField set the value as if the user had set it
        if (field instanceof SupportsUserAction) {
            ((SupportsUserAction) field).setValueFromUser(newValue);
        } else {
            field.setValue(newValue);
        }
    }

    protected <E> Collection<E> toCollection(Object value, boolean isCollectionValue) {
        return isCollectionValue ? (Collection<E>) value : Collections.singletonList((E) value);
    }

    protected <E> Object fromCollection(Collection<E> value, boolean isCollectionValue) {
        return isCollectionValue ? value : value.iterator().next();
    }

    protected <E> void handleSelectionWithContainer(LookupBuilder<E> builder,
                                                    CollectionContainer<E> collectionDc,
                                                    Collection<E> itemsFromLookup) {
        if (itemsFromLookup.isEmpty()) {
            return;
        }

        Collection<E> selectedItems = transform(itemsFromLookup, builder);

        boolean initializeMasterReference = false;
        Object masterItem = null;
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
        FetchPlan viewForCollectionContainer = screenProperties.isReloadUnfetchedAttributesFromLookupScreens() &&
                collectionDc.getEntityMetaClass() != null && metadataTools.isJpaEntity(collectionDc.getEntityMetaClass())
                ? getFetchPlanForCollectionContainer(collectionDc, initializeMasterReference, inverseMetaProperty)
                : null;
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

    protected <E> Collection<E> transform(Collection<E> selectedItems, LookupBuilder<E> builder) {
        if (builder.getTransformation() != null) {
            return builder.getTransformation().apply(selectedItems);
        }
        return selectedItems;
    }

    /**
     * The method evaluates the fetch plan that is used for the entity in the given {@code field}
     * <p>
     * If the value for a component (e.g. {@link EntityPicker}) is selected from lookup screen then there may be cases
     * when in entities in lookup screen some attributes required in the editor are not loaded.
     *
     * @return a view or {@code null} if the fetch plan cannot be evaluated
     */
    @Nullable
    protected FetchPlan getFetchPlanForField(HasValue field) {
        if (field instanceof HasValueSource) {
            ValueSource valueSource = ((HasValueSource) field).getValueSource();
            if (valueSource instanceof ContainerValueSource) {
                ContainerValueSource containerValueSource = (ContainerValueSource) valueSource;
                InstanceContainer container = containerValueSource.getContainer();
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
    protected <E> FetchPlan getFetchPlanForCollectionContainer(CollectionContainer<E> collectionDc,
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
                        fetchPlan = fetchPlans.builder(fetchPlan)
                                .add(inverseMetaProperty.getName())
                                .build();
                    }
                }
            }
        } else {
            fetchPlan = collectionDc.getFetchPlan();
        }
        return fetchPlan;
    }

    protected <E> Collection<E> reloadItemsByFetchPlan(FetchPlan fetchPlan, Collection<E> itemsFromLookup) {
        if (itemsFromLookup.isEmpty()) {
            return Collections.emptyList();
        }

        E firstItem = itemsFromLookup.iterator().next();

        boolean reloadByFetchPlan = screenProperties.isReloadUnfetchedAttributesFromLookupScreens()
                && metadataTools.isJpaEntity(firstItem.getClass());

        Collection<E> reloadedItems = new ArrayList<>(itemsFromLookup.size());
        if (reloadByFetchPlan) {
            if (!entityStates.isLoadedWithFetchPlan(firstItem, fetchPlan)) {
                for (E selectedItem : itemsFromLookup) {
                    E reloadedItem = dataManager.load(Id.of(selectedItem)).fetchPlan(fetchPlan).one();
                    reloadedItems.add(reloadedItem);
                }
            }
        }
        return reloadedItems;
    }

    /**
     * Updates entities in options if they contain selected item from lookup screen.
     *
     * @param field options field to update
     * @param items selected entities from lookup screen to update options in field
     * @param <E>   entity type
     */
    @SuppressWarnings("rawtypes")
    public <E> void updateFieldOptions(OptionsField field, Collection<E> items) {
        Options options = field.getOptions();

        if (options instanceof EntityOptions) {
            EntityOptions entityOptions = (EntityOptions) options;
            for (E newItem : items) {
                if (entityOptions.containsItem(newItem)) {
                    entityOptions.updateItem(newItem);
                }
            }
        }
    }
}
