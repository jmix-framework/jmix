/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.component.gridlayout;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Represents a customizable grid layout component for displaying data in a grid-based format.
 *
 * @param <T> the type of the items displayed in this layout
 */
@Tag("jmix-grid-layout")
@JsModule("./src/grid-layout/jmix-grid-layout.js")
public class JmixGridLayout<T> extends Component implements HasSize, HasItemComponents<T>,
        HasListDataView<T, GridLayoutListDataView<T>>, HasDataView<T, Void, GridLayoutDataView<T>> {

    protected final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(DataProvider.ofItems());
    protected List<T> items;
    protected Registration dataProviderListenerRegistration;

    protected ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;
    protected ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>(itemLabelGenerator);

    protected int lastNotifiedDataSize = -1;
    protected volatile int lastFetchedDataSize = -1;
    protected SerializableConsumer<UI> sizeRequest;

    /**
     * Returns the gap of the grid layout in the {@link JmixGridLayout}. Default value is {@code var(--lumo-space-s)}.
     *
     * @return the gap of the grid layout
     */
    public String getGap() {
        return getElement().getProperty("gridGap", "var(--lumo-space-s)");
    }


    /**
     * Sets the gap of the grid layout in the {@link JmixGridLayout}.
     *
     * @param gridGap the gap to be set for the grid layout, represented as a CSS length value
     *                (e.g., {@code "10px"}, {@code "1rem"})
     */
    public void setGap(String gridGap) {
        getElement().setProperty("gridGap", gridGap);
    }

    /**
     * Returns the minimum width of the columns in the {@link JmixGridLayout}. Default value is {@code 19rem}
     *
     * @return the minimum width for the columns as a CSS length value (e.g., {@code "19rem"})
     */
    public String getColumnMinWidth() {
        return getElement().getProperty("columnMinWidth", "19rem");
    }

    /**
     * Sets the minimum width for the grid columns in the {@link JmixGridLayout}.
     *
     * @param columnMinWidth the minimum width to be set for the grid columns,
     *                       represented as a CSS length value (e.g., {@code "50px"}, {@code "10rem"})
     */
    public void setColumnMinWidth(String columnMinWidth) {
        getElement().setProperty("columnMinWidth", columnMinWidth);
    }

    /**
     * Gets the data provider used by this {@link JmixGridLayout}.
     * <p>
     * To get information and control over the items in the {@link JmixGridLayout}, use either {@link #getListDataView()}
     * or {@link #getGenericDataView()} instead.
     *
     * @return the data provider used by this {@link JmixGridLayout}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider.get();
    }

    /**
     * Sets a generic data provider for the {@link JmixGridLayout} to use.
     * <p>
     * Use this method when none of the {@code setItems} methods are applicable, e.g., when having a data provider
     * with a filter that cannot be transformed to {@code DataProvider<T, Void>}.
     *
     * @param dataProvider DataProvider instance to use, not {@code null}
     */
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
        rebuild();
        setupDataProviderListener(this.dataProvider.get());
    }

    protected void setupDataProviderListener(DataProvider<T, ?> dataProvider) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(this::handleDataChange);
    }

    protected void handleDataChange(DataChangeEvent<T> event) {
        if (event instanceof DataRefreshEvent<T> dataRefreshEvent) {
            refresh(dataRefreshEvent.getItem());
        } else {
            rebuild();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getDataProvider() != null) {
            setupDataProviderListener(getDataProvider());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
            dataProviderListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer
     * @see #setRenderer
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this {@link JmixGridLayout}. The renderer is applied to each item to create a
     * component which represents the item.
     *
     * @param itemRenderer the item renderer, not {@code null}
     */
    public void setRenderer(ComponentRenderer<? extends Component, T> itemRenderer) {
        Preconditions.checkArgument(itemRenderer != null, "Item renderer cannot be null");
        this.itemRenderer = itemRenderer;
        getItemComponents().forEach(this::refreshContent);
    }

    /**
     * Gets the item label generator used to produce the strings shown
     * in the {@link JmixGridLayout} for each item.
     *
     * @return the item label provider to use, not {@code null}
     */
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    public void setItemLabelGenerator(ItemLabelGenerator<T> itemLabelGenerator) {
        Preconditions.checkArgument(itemLabelGenerator != null, "Item label generator cannot be null");
        this.itemLabelGenerator = itemLabelGenerator;
        setRenderer(new TextRenderer<>(itemLabelGenerator));
    }

    @SuppressWarnings("unchecked")
    protected void rebuild() {
        removeAll();

        final AtomicInteger itemCounter = new AtomicInteger(0);
        items = (List<T>) getDataProvider()
                .fetch(DataViewUtils.getQuery(this))
                .collect(Collectors.toList());
        items.stream()
                .map(this::createItemComponent)
                .forEach(component -> {
                    add(component);
                    itemCounter.incrementAndGet();
                });

        lastFetchedDataSize = itemCounter.get();

        // Ignore new size requests unless the last one has been executed
        // to void multiple beforeClientResponses.
        if (sizeRequest != null) {
            sizeRequest = ui -> {
                fireSizeEvent();
                sizeRequest = null;
            };

            // Size event is fired before client response to avoid multiple size change events
            // during server round trips
            runBeforeClientResponse(sizeRequest);
        }
    }

    protected GridLayoutItem<T> createItemComponent(T item) {
        GridLayoutItem<T> gridLayoutItem = new GridLayoutItem<>(item);
        refreshContent(gridLayoutItem);
        return gridLayoutItem;
    }

    protected void refresh(T item) {
        getItemComponents().stream()
                .filter(gridLayoutItem -> getItemId(gridLayoutItem.getItem()).equals(getItemId(item)))
                .findAny()
                .ifPresent(this::refreshContent);
    }

    protected void refreshContent(GridLayoutItem<T> gridLayoutItem) {
        gridLayoutItem.removeAll();
        gridLayoutItem.add(itemRenderer.createComponent(gridLayoutItem.getItem()));
    }

    protected List<GridLayoutItem<T>> getItemComponents() {
        //noinspection unchecked
        return getChildren()
                .filter(GridLayoutItem.class::isInstance)
                .map(component -> (GridLayoutItem<T>) component)
                .collect(Collectors.toList());
    }

    /**
     * Sets a generic data provider for the {@link JmixGridLayout} to use and returns
     * the base {@link GridLayoutDataView} that provides API to get information on the items.
     * <p>
     * This method should be used only when the data provider type is not either {@link ListDataProvider} or
     * {@link BackEndDataProvider}.
     *
     * @param dataProvider DataProvider instance to use, not {@code null}
     * @return {@link GridLayoutDataView} providing information on the data
     */
    @Override
    public GridLayoutDataView<T> setItems(DataProvider<T, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    /**
     * Sets an in-memory data provider for the {@link JmixGridLayout} to use.
     * <p>
     * <b>Note!</b> Using a {@link ListDataProvider} instead of a {@link InMemoryDataProvider} is recommended to get
     * access to {@link GridLayoutListDataView} API by using {@link HasListDataView#setItems(ListDataProvider)}.
     *
     * @param inMemoryDataProvider data provider to use, not {@code null}
     * @return {@link GridLayoutDataView} providing information on the data
     */
    @Override
    public GridLayoutDataView<T> setItems(InMemoryDataProvider<T> inMemoryDataProvider) {
        // We don't use DataProvider.withConvertedFilter() here because it's
        // implementation doesn't apply the filter converter if Query has a null filter
        DataProvider<T, Void> convertedDataProvider = new DataProviderWrapper<>(
                inMemoryDataProvider
        ) {
            @Override
            protected SerializablePredicate<T> getFilter(Query<T, Void> query) {
                // Ignore the query filter (Void) and apply the predicate only
                return Optional.ofNullable(inMemoryDataProvider.getFilter())
                        .orElse(item -> true);
            }
        };

        return setItems(convertedDataProvider);
    }

    /**
     * Sets a {@link ListDataProvider} for the {@link JmixGridLayout} to use and returns a {@link ListDataView}
     * that provides information and allows operation on the items.
     *
     * @param listDataProvider ListDataProvider providing items to the {@link JmixGridLayout}.
     * @return {@link GridLayoutListDataView} providing access to the items
     */
    @Override
    public GridLayoutListDataView<T> setItems(ListDataProvider<T> listDataProvider) {
        setDataProvider(listDataProvider);
        return getListDataView();
    }

    /**
     * Gets the list data view for the {@link JmixGridLayout}. This data view should only be used when the items are
     * in-memory and set with:
     * <ul>
     *     <li>{@link #setItems(Collection)}</li>
     *     <li>{@link #setItems(Object[])}</li>
     *     <li>{@link #setItems(ListDataProvider)}</li>
     * </ul>
     * If the items are not in-memory, an exception is thrown.
     *
     * @return the list data view that provides access to the data bound to the {@link JmixGridLayout}
     */
    @Override
    public GridLayoutListDataView<T> getListDataView() {
        return new GridLayoutListDataView<>(this::getDataProvider, this,
                (filter, sorting) -> rebuild());
    }

    /**
     * Gets the generic data view for the {@link JmixGridLayout}. This data view should only be used when
     * {@link #getListDataView()} is not applicable for the underlying data provider.
     *
     * @return the generic DataView instance implementing {@link GridLayoutDataView}
     */
    @Override
    public GridLayoutDataView<T> getGenericDataView() {
        return new GridLayoutDataView<>(this::getDataProvider, this);
    }

    protected Object getItemId(T item) {
        return getIdentifierProvider().apply(item);
    }

    protected void fireSizeEvent() {
        final int newSize = lastFetchedDataSize;
        if (lastNotifiedDataSize != newSize) {
            lastNotifiedDataSize = newSize;
            fireEvent(new ItemCountChangeEvent<>(this, newSize, false));
        }
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    @SuppressWarnings("unchecked")
    private IdentifierProvider<T> getIdentifierProvider() {
        IdentifierProvider<T> identifierProviderObject = (IdentifierProvider<T>) ComponentUtil
                .getData(this, IdentifierProvider.class);
        if (identifierProviderObject != null)
            return identifierProviderObject;

        DataProvider<T, ?> dataProvider = getDataProvider();
        if (dataProvider != null)
            return dataProvider::getId;

        return IdentifierProvider.identity();
    }
}
