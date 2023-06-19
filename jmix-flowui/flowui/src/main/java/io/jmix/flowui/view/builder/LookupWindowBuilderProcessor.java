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
import io.jmix.core.*;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.Views;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.*;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.kit.component.SupportsUserAction;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.Nested;
import io.jmix.flowui.view.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.*;

@Internal
@Component("flowui_LookupWindowBuilderProcessor")
public class LookupWindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected FetchPlans fetchPlans;
    protected EntityStates entityStates;
    protected ExtendedEntities extendedEntities;
    protected UiViewProperties viewProperties;

    public LookupWindowBuilderProcessor(ApplicationContext applicationContext,
                                        Views views,
                                        ViewRegistry viewRegistry,
                                        Metadata metadata,
                                        MetadataTools metadataTools,
                                        DataManager dataManager,
                                        FetchPlans fetchPlans,
                                        EntityStates entityStates,
                                        ExtendedEntities extendedEntities,
                                        UiViewProperties viewProperties) {
        super(applicationContext, views, viewRegistry);

        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.entityStates = entityStates;
        this.extendedEntities = extendedEntities;
        this.viewProperties = viewProperties;
    }

    public <E, V extends View<?>> DialogWindow<V> build(LookupWindowBuilder<E, V> builder) {
        V view = createView(builder);

        if (!(view instanceof LookupView)) {
            throw new IllegalArgumentException(String.format("View '%s' does not implement %s. View class: %s",
                    view.getId(), LookupView.class.getSimpleName(), view.getClass()));
        }
        //noinspection unchecked
        LookupView<E> lookupView = (LookupView<E>) view;


        CollectionContainer<E> container = findContainer(builder);
        if (container != null) {
            lookupView.setSelectionHandler(items ->
                    handleSelectionWithContainer(builder, container, items)
            );

            if (view instanceof MultiSelectLookupView) {
                ((MultiSelectLookupView) view).setLookupComponentMultiSelect(true);
            }
        }

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        builder.getField().ifPresent(field -> {
            if (field instanceof Focusable) {
                dialog.addAfterCloseListener(closeEvent ->
                        ((Focusable<?>) field).focus());
            }

            lookupView.setSelectionHandler(items ->
                    handleSelectionWithField(builder, field, items));

            if (view instanceof MultiSelectLookupView) {
                ((MultiSelectLookupView) view).setLookupComponentMultiSelect(builder.isFieldCollectionValue());
            }
        });

        builder.getSelectHandler().ifPresent(lookupView::setSelectionHandler);
        builder.getSelectValidator().ifPresent(lookupView::setSelectionValidator);

        builder.getListDataComponent().ifPresent(listDataComponent -> {
            if (listDataComponent instanceof Focusable) {
                dialog.addAfterCloseListener(closeEvent ->
                        ((Focusable<?>) listDataComponent).focus());
            }
        });

        return dialog;
    }

    @Nullable
    protected <E> CollectionContainer<E> findContainer(LookupWindowBuilder<E, ?> builder) {
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

    @SuppressWarnings("unchecked")
    @Override
    protected <V extends View<?>> Class<V> inferViewClass(DialogWindowBuilder<V> builder) {
        LookupWindowBuilder<?, V> lookupBuilder = ((LookupWindowBuilder<?, V>) builder);
        return (Class<V>) viewRegistry.getLookupViewInfo(lookupBuilder.getEntityClass()).getControllerClass();
    }

    protected <E> void handleSelectionWithContainer(LookupWindowBuilder<E, ?> builder,
                                                    CollectionContainer<E> collectionDc,
                                                    Collection<E> itemsFromLookup) {
        if (itemsFromLookup.isEmpty()) {
            return;
        }

        Collection<E> selectedItems = transform(builder, itemsFromLookup);

        boolean initializeMasterReference = false;
        Object masterItem = null;
        MetaProperty inverseMetaProperty = null;

        // update holder reference if needed
        if (collectionDc instanceof Nested) {
            InstanceContainer<?> masterDc = ((Nested) collectionDc).getMaster();

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

        DataContext dataContext = ViewControllerUtils.getViewData(builder.getOrigin()).getDataContext();

        List<E> mergedItems = new ArrayList<>(selectedItems.size());
        FetchPlan viewForCollectionContainer = viewProperties.isReloadUnfetchedAttributesFromLookupViews()
                && metadataTools.isJpaEntity(collectionDc.getEntityMetaClass())
                ? getFetchPlanForCollectionContainer(collectionDc, initializeMasterReference, inverseMetaProperty)
                : null;

        for (E item : selectedItems) {
            if (!collectionDc.containsItem(Objects.requireNonNull(EntityValues.getId(item)))) {
                if (viewForCollectionContainer != null && !entityStates.isLoadedWithFetchPlan(item, viewForCollectionContainer)) {
                    item = dataManager.load(Id.of(item)).fetchPlan(viewForCollectionContainer).one();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <E, V extends View<?>> void handleSelectionWithField(LookupWindowBuilder<E, V> builder,
                                                                   HasValue field,
                                                                   Collection<E> itemsFromLookup) {
        if (itemsFromLookup.isEmpty()) {
            return;
        }

        boolean isCollectionValue = builder.isFieldCollectionValue();

        Collection<E> selectedItems = transform(builder, itemsFromLookup);
        Object newValue = isCollectionValue ? selectedItems : selectedItems.iterator().next();

        FetchPlan fetchPlan = getFetchPlanForField(field);
        if (fetchPlan != null) {
            Collection<E> reloadedItems = reloadItemsByFetchPlan(fetchPlan, toCollection(newValue, isCollectionValue));
            if (!reloadedItems.isEmpty()) {
                newValue = fromCollection(reloadedItems, isCollectionValue);
            }
        }

        if (field instanceof SupportsDataProvider) {
            updateFieldOptions((SupportsDataProvider<E>) field, toCollection(newValue, isCollectionValue));
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

        // In case of ValuePicker set the value as if the user had set it
        if (field instanceof SupportsUserAction) {
            ((SupportsUserAction) field).setValueFromClient(newValue);
        } else {
            field.setValue(newValue);
        }
    }

    protected <E> Collection<E> transform(LookupWindowBuilder<E, ?> builder, Collection<E> selectedItems) {
        if (builder.getTransformation().isPresent()) {
            return builder.getTransformation().get().apply(selectedItems);
        }
        return selectedItems;
    }

    /**
     * The method evaluates the fetch plan that is used for the entity in the given {@code field}
     * <p>
     * If the value for a component (e.g. {@link EntityPicker}) is selected from lookup view then there may be cases
     * when in entities in lookup view some attributes required in the detail are not loaded.
     *
     * @return a view or {@code null} if the fetch plan cannot be evaluated
     */
    @Nullable
    protected FetchPlan getFetchPlanForField(HasValue<?, ?> field) {
        if (field instanceof SupportsValueSource) {
            ValueSource<?> valueSource = ((SupportsValueSource<?>) field).getValueSource();
            if (valueSource instanceof ContainerValueSource) {
                ContainerValueSource<?, ?> containerValueSource = (ContainerValueSource<?, ?>) valueSource;
                InstanceContainer<?> container = containerValueSource.getContainer();
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
            InstanceContainer<?> masterDc = ((Nested) collectionDc).getMaster();
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

        boolean reloadByFetchPlan = viewProperties.isReloadUnfetchedAttributesFromLookupViews()
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

    @SuppressWarnings("unchecked")
    protected <E> Collection<E> toCollection(Object value, boolean isCollectionValue) {
        return isCollectionValue ? (Collection<E>) value : Collections.singletonList((E) value);
    }

    protected <E> Object fromCollection(Collection<E> value, boolean isCollectionValue) {
        return isCollectionValue ? value : value.iterator().next();
    }

    /**
     * Updates entities in options if they contain selected item from lookup view.
     *
     * @param field options field to update
     * @param items selected entities from lookup view to update options in field
     * @param <E>   entity type
     */
    public <E> void updateFieldOptions(SupportsDataProvider<E> field, Collection<E> items) {
        DataProvider<E, ?> dataProvider = field.getDataProvider();

        if (dataProvider instanceof EntityItems) {
            EntityItems<E> entityItems = (EntityItems<E>) dataProvider;
            for (E newItem : items) {
                if (entityItems.containsItem(newItem)) {
                    entityItems.updateItem(newItem);
                }
            }
        }
    }
}
