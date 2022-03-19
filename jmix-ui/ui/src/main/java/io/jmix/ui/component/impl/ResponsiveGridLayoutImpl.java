/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.impl;

import com.google.common.base.Preconditions;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.Registration;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.*;
import io.jmix.ui.widget.JmixResponsiveGridLayout;
import org.apache.commons.collections4.MapUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.impl.WrapperUtils.*;

public class ResponsiveGridLayoutImpl extends AbstractComponent<JmixResponsiveGridLayout>
        implements ResponsiveGridLayout {

    protected List<Row> rowOrder = new ArrayList<>();

    protected List<Component> ownComponents = new ArrayList<>();
    protected Registration layoutClickRegistration;

    public ResponsiveGridLayoutImpl() {
        component = createComponent();
    }

    protected JmixResponsiveGridLayout createComponent() {
        return new JmixResponsiveGridLayout();
    }

    @Override
    public Row addRow() {
        return addRow(rowOrder.size());
    }

    @Override
    public Row addRow(int index) {
        checkInitializedState();

        RowImpl row = new RowImpl(component.addRow(index), this);
        rowOrder.add(index, row);
        return row;
    }

    @Override
    public void removeRow(Row row) {
        checkInitializedState();

        if (rowOrder.remove(row)) {
            row.removeAllColumns();

            component.removeRow(((RowImpl) row).getVaadinGridElement());
        } else {
            throw new IllegalArgumentException(
                    "Row can't be removed because it does not belong to the ResponsiveGridLayout");
        }
    }

    @Override
    public void removeAllRows() {
        List<Row> rows = new ArrayList<>(rowOrder);
        for (Row row : rows) {
            removeRow(row);
        }
    }

    @Override
    public List<Row> getRows() {
        return Collections.unmodifiableList(rowOrder);
    }

    @Override
    public ContainerType getContainerType() {
        return fromVaadinContainerType(component.getContainerType());
    }

    @Override
    public void setContainerType(ContainerType containerType) {
        checkInitializedState();

        component.setContainerType(toVaadinContainerType(containerType));
    }

    @Nullable
    @Override
    public Component getOwnComponent(String id) {
        checkNotNullArgument(id);

        return ownComponents.stream()
                .filter(component ->
                        Objects.equals(id, component.getId()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return ComponentsHelper.getComponent(this, id);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return Collections.unmodifiableCollection(ownComponents);
    }

    @Override
    public Stream<Component> getOwnComponentsStream() {
        return ownComponents.stream();
    }

    @Override
    public Collection<Component> getComponents() {
        return ComponentsHelper.getComponents(this);
    }

    protected void addOwnComponent(Component component) {
        ownComponents.add(component);
    }

    protected void removeOwnComponent(Component component) {
        ownComponents.remove(component);
    }

    @Override
    public Subscription addLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        if (layoutClickRegistration == null) {
            LayoutEvents.LayoutClickListener layoutClickListener = event -> {
                Component childComponent = findChildComponent(event.getChildComponent());
                Component clickedComponent = findChildComponent(event.getClickedComponent());
                MouseEventDetails mouseEventDetails = WrapperUtils.toMouseEventDetails(event);

                LayoutClickEvent layoutClickEvent =
                        new LayoutClickEvent(this, childComponent, clickedComponent, mouseEventDetails);


                publish(LayoutClickEvent.class, layoutClickEvent);
            };
            layoutClickRegistration = component.addLayoutClickListener(layoutClickListener);
        }

        getEventHub().subscribe(LayoutClickEvent.class, listener);

        return () -> internalRemoveLayoutClickListener(listener);
    }

    @Nullable
    protected Component findChildComponent(com.vaadin.ui.Component clickedComponent) {
        for (Component component : getComponents()) {
            if (component.unwrapComposition(com.vaadin.ui.Component.class) == clickedComponent) {
                return component;
            }
        }
        return null;
    }

    protected void internalRemoveLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        unsubscribe(LayoutClickEvent.class, listener);

        if (!hasSubscriptions(LayoutClickEvent.class)) {
            layoutClickRegistration.remove();
            layoutClickRegistration = null;
        }
    }

    @Override
    public void attached() {
        super.attached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).attached();
        }
    }

    @Override
    public void detached() {
        super.detached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).detached();
        }
    }

    @Override
    public float getWidth() {
        throw new UnsupportedOperationException("ResponsiveGridLayout doesn't support explicit width value");
    }

    @Override
    public SizeUnit getWidthSizeUnit() {
        throw new UnsupportedOperationException("ResponsiveGridLayout doesn't support explicit width value");
    }

    @Override
    public void setWidth(@Nullable String width) {
        throw new UnsupportedOperationException("ResponsiveGridLayout doesn't support explicit width value");
    }

    @Override
    public boolean isResponsive() {
        return false;
    }

    @Override
    public void setResponsive(boolean responsive) {
        throw new UnsupportedOperationException("ResponsiveGridLayout is responsive by its nature");
    }

    protected void checkInitializedState() {
        if (component.isInitialized()) {
            throw new IllegalStateException("ResponsiveGridLayout doesn't support changing " +
                    "its configuration after it has been initialized");
        }
    }

    protected static class RowImpl extends AbstractGridElementImpl<JmixResponsiveGridLayout.Row>
            implements Row {

        protected List<Column> columnOrder = new ArrayList<>();

        protected Map<Breakpoint, RowColumnsValue> rowColumns = new HashMap<>();
        protected Map<Breakpoint, AlignItems> alignItems = new HashMap<>();
        protected Map<Breakpoint, JustifyContent> justifyContent = new HashMap<>();

        public RowImpl(JmixResponsiveGridLayout.Row row, ResponsiveGridLayoutImpl owner) {
            super(row, owner);
        }

        @Override
        public Column addColumn() {
            return addColumn(columnOrder.size());
        }

        @Override
        public Column addColumn(int index) {
            checkInitializedState();

            ColumnImpl column = new ColumnImpl(getVaadinGridElement().addColumn(index), getOwner());
            columnOrder.add(index, column);
            return column;
        }

        @Override
        public void removeColumn(Column column) {
            checkInitializedState();

            if (columnOrder.remove(column)) {
                ColumnImpl colImpl = (ColumnImpl) column;
                ((ColumnImpl) column).removeContentIfPresent();

                getVaadinGridElement().removeColumn(colImpl.getVaadinGridElement());
            } else {
                throw new IllegalArgumentException("Column can't be removed because it does not belong to the row");
            }
        }

        @Override
        public void removeAllColumns() {
            List<Column> columns = new ArrayList<>(columnOrder);
            for (Column column : columns) {
                removeColumn(column);
            }
        }

        @Override
        public List<Column> getColumns() {
            return Collections.unmodifiableList(columnOrder);
        }

        @Override
        public boolean isGuttersEnabled() {
            return getVaadinGridElement().isGuttersEnabled();
        }

        @Override
        public void setGuttersEnabled(boolean guttersEnabled) {
            checkInitializedState();

            getVaadinGridElement().setGuttersEnabled(guttersEnabled);
        }

        @Override
        public float getHeight() {
            return getVaadinGridElement().getHeight();
        }

        @Override
        public SizeUnit getHeightSizeUnit() {
            return WrapperUtils.toSizeUnit(getVaadinGridElement().getHeightUnits());
        }

        @Override
        public void setHeight(String height) {
            checkInitializedState();

            // do not try to parse string if constant passed
            if (Component.AUTO_SIZE.equals(height)) {
                getVaadinGridElement().setHeight(-1, Sizeable.Unit.PIXELS);
            } else if (Component.FULL_SIZE.equals(height)) {
                getVaadinGridElement().setHeight(100, Sizeable.Unit.PERCENTAGE);
            } else {
                getVaadinGridElement().setHeight(height);
            }
        }

        @Override
        public Map<Breakpoint, RowColumnsValue> getRowColumns() {
            return MapUtils.unmodifiableMap(rowColumns);
        }

        @Override
        public void setRowColumns(Map<Breakpoint, RowColumnsValue> columnsValue) {
            checkInitializedState();

            rowColumns = new HashMap<>(columnsValue);
            updateRowColumnsValue();
        }

        @Override
        public void addRowColumns(Breakpoint breakpoint, RowColumnsValue columnsValue) {
            checkInitializedState();

            rowColumns.put(breakpoint, columnsValue);
            updateRowColumnsValue();
        }

        @Override
        public void removeRowColumns(Breakpoint breakpoint) {
            checkInitializedState();

            rowColumns.remove(breakpoint);
            updateRowColumnsValue();
        }

        protected void updateRowColumnsValue() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.RowColumnsValue> columnsValue =
                    rowColumns.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> toVaadinBreakpoint(entry.getKey()),
                                    entry -> toVaadinRowColumnsValue(entry.getValue())
                            ));

            getVaadinGridElement().setRowColumns(columnsValue);
        }

        @Override
        public Map<Breakpoint, AlignItems> getAlignItems() {
            return MapUtils.unmodifiableMap(alignItems);
        }

        @Override
        public void setAlignItems(Map<Breakpoint, AlignItems> alignItems) {
            checkInitializedState();

            this.alignItems = new HashMap<>(alignItems);
            updateAlignItems();
        }

        @Override
        public void addAlignItems(Breakpoint breakpoint, AlignItems alignItems) {
            checkInitializedState();

            this.alignItems.put(breakpoint, alignItems);
            updateAlignItems();
        }

        @Override
        public void removeAlignItems(Breakpoint breakpoint) {
            checkInitializedState();

            alignItems.remove(breakpoint);
            updateAlignItems();
        }

        protected void updateAlignItems() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.AlignItems> alignItems =
                    this.alignItems.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> toVaadinBreakpoint(entry.getKey()),
                                    entry -> toVaadinAlignItems(entry.getValue())
                            ));

            getVaadinGridElement().setAlignItems(alignItems);
        }

        @Override
        public Map<Breakpoint, JustifyContent> getJustifyContent() {
            return MapUtils.unmodifiableMap(justifyContent);
        }

        @Override
        public void setJustifyContent(Map<Breakpoint, JustifyContent> justifyContent) {
            checkInitializedState();

            this.justifyContent = new HashMap<>(justifyContent);
            updateJustifyContent();
        }

        @Override
        public void addJustifyContent(Breakpoint breakpoint, JustifyContent justifyContent) {
            checkInitializedState();

            this.justifyContent.put(breakpoint, justifyContent);
            updateJustifyContent();
        }

        @Override
        public void removeJustifyContent(Breakpoint breakpoint) {
            checkInitializedState();

            justifyContent.remove(breakpoint);
            updateJustifyContent();
        }

        protected void updateJustifyContent() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.JustifyContent> justifyContent =
                    this.justifyContent.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> toVaadinBreakpoint(entry.getKey()),
                                    entry -> toVaadinJustifyContent(entry.getValue())
                            ));

            getVaadinGridElement().setJustifyContent(justifyContent);
        }
    }

    protected static class ColumnImpl extends AbstractGridElementImpl<JmixResponsiveGridLayout.Column>
            implements Column {

        protected Component component;

        protected Map<Breakpoint, ColumnsValue> columns = new HashMap<>();
        protected Map<Breakpoint, AlignSelf> aligns = new HashMap<>();
        protected Map<Breakpoint, OrderValue> orders = new HashMap<>();
        protected Map<Breakpoint, OffsetValue> offsets = new HashMap<>();

        public ColumnImpl(JmixResponsiveGridLayout.Column column, ResponsiveGridLayoutImpl owner) {
            super(column, owner);
        }

        @Override
        public Component getComponent() {
            return component;
        }

        @Override
        public void setComponent(Component component) {
            checkNotNullArgument(component, "Component cannot be null");
            Preconditions.checkState(component.getParent() == null, "Component already has parent");

            removeContentIfPresent();
            this.component = component;

            this.component.setParent(getOwner());
            getOwner().addOwnComponent(this.component);

            assignFrame(component);

            getVaadinGridElement().setComponent(ComponentsHelper.getComposition(component));
        }

        protected void assignFrame(Component component) {
            Frame frame = getOwner().getFrame();
            if (frame != null) {
                if (component instanceof BelongToFrame
                        && ((BelongToFrame) component).getFrame() == null) {
                    ((BelongToFrame) component).setFrame(frame);
                } else {
                    ((FrameImplementation) frame).registerComponent(component);
                }
            }
        }

        protected void removeContentIfPresent() {
            if (component != null) {
                component.setParent(null);
                getOwner().removeOwnComponent(component);

                component = null;
            }
        }

        @Override
        public Map<Breakpoint, ColumnsValue> getColumns() {
            return MapUtils.unmodifiableMap(columns);
        }

        @Override
        public void setColumns(Map<Breakpoint, ColumnsValue> columns) {
            checkInitializedState();

            this.columns = new HashMap<>(columns);
            updateColumns();
        }

        @Override
        public void addColumns(Breakpoint breakpoint, ColumnsValue columnsValue) {
            checkInitializedState();

            columns.put(breakpoint, columnsValue);
            updateColumns();
        }

        @Override
        public void removeColumns(Breakpoint breakpoint) {
            checkInitializedState();

            columns.remove(breakpoint);
            updateColumns();
        }

        protected void updateColumns() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.ColumnsValue> columns =
                    this.columns.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> toVaadinBreakpoint(entry.getKey()),
                                    entry -> toVaadinColumnsValue(entry.getValue())
                            ));

            getVaadinGridElement().setColumns(columns);
        }

        @Override
        public Map<Breakpoint, AlignSelf> getAlignSelf() {
            return MapUtils.unmodifiableMap(aligns);
        }

        @Override
        public void setAlignSelf(Map<Breakpoint, AlignSelf> aligns) {
            checkInitializedState();

            this.aligns = new HashMap<>(aligns);
            updateAlign();
        }

        @Override
        public void addAlignSelf(Breakpoint breakpoint, AlignSelf alignSelf) {
            checkInitializedState();

            aligns.put(breakpoint, alignSelf);
            updateAlign();
        }

        @Override
        public void removeAlignSelf(Breakpoint breakpoint) {
            checkInitializedState();

            aligns.remove(breakpoint);
            updateAlign();
        }

        protected void updateAlign() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.AlignSelf> aligns =
                    this.aligns.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> toVaadinBreakpoint(entry.getKey()),
                                    entry -> toVaadinResponsiveColumnAlignSelf(entry.getValue())
                            ));

            getVaadinGridElement().setAlignSelf(aligns);
        }

        @Override
        public Map<Breakpoint, OrderValue> getOrder() {
            return MapUtils.unmodifiableMap(orders);
        }

        @Override
        public void setOrder(Map<Breakpoint, OrderValue> orders) {
            checkInitializedState();

            this.orders = new HashMap<>(orders);
            updateOrder();
        }

        @Override
        public void addOrder(Breakpoint breakpoint, OrderValue orderValue) {
            checkInitializedState();

            orders.put(breakpoint, orderValue);
            updateOrder();
        }

        @Override
        public void removeOrder(Breakpoint breakpoint) {
            checkInitializedState();

            orders.remove(breakpoint);
            updateOrder();
        }

        protected void updateOrder() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.OrderValue> orders =
                    this.orders.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> toVaadinBreakpoint(entry.getKey()),
                                    entry -> toVaadinOrderValue(entry.getValue())
                            ));

            getVaadinGridElement().setOrder(orders);
        }

        @Override
        public Map<Breakpoint, OffsetValue> getOffset() {
            return MapUtils.unmodifiableMap(offsets);
        }

        @Override
        public void setOffset(Map<Breakpoint, OffsetValue> offsets) {
            checkInitializedState();

            this.offsets = new HashMap<>(offsets);
            updateOffsetValue();
        }

        @Override
        public void addOffset(Breakpoint breakpoint, OffsetValue offsetValue) {
            checkInitializedState();

            offsets.put(breakpoint, offsetValue);
            updateOffsetValue();
        }

        @Override
        public void removeOffset(Breakpoint breakpoint) {
            checkInitializedState();

            offsets.remove(breakpoint);
            updateOffsetValue();
        }

        protected void updateOffsetValue() {
            Map<JmixResponsiveGridLayout.Breakpoint, JmixResponsiveGridLayout.OffsetValue> offsets =
                    this.offsets.entrySet().stream().collect(Collectors.toMap(
                            entry -> toVaadinBreakpoint(entry.getKey()),
                            entry -> toVaadinOffsetValue(entry.getValue())
                    ));

            getVaadinGridElement().setOffset(offsets);
        }
    }

    protected static abstract class AbstractGridElementImpl<T extends JmixResponsiveGridLayout.AbstractGridElement>
            implements GridElement {

        protected T gridElement;
        protected ResponsiveGridLayoutImpl owner;

        protected String id;

        public AbstractGridElementImpl(T gridElement, ResponsiveGridLayoutImpl owner) {
            checkNotNullArgument(gridElement);
            checkNotNullArgument(owner);

            this.gridElement = gridElement;
            this.owner = owner;
        }

        public T getVaadinGridElement() {
            return gridElement;
        }

        public ResponsiveGridLayoutImpl getOwner() {
            return owner;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            checkInitializedState();

            this.id = id;

            AppUI ui = AppUI.getCurrent();
            if (ui != null && ui.isTestMode()) {
                getVaadinGridElement().setJTestId(id);
            }
        }

        @Override
        public String getStyleName() {
            return getVaadinGridElement().getStyleName();
        }

        @Override
        public void setStyleName(String styleName) {
            checkInitializedState();

            getVaadinGridElement().setStyleName(styleName);
        }

        @Override
        public void addStyleName(String styleName) {
            checkInitializedState();

            getVaadinGridElement().addStyleName(styleName);
        }

        @Override
        public void removeStyleName(String styleName) {
            checkInitializedState();

            getVaadinGridElement().removeStyleName(styleName);
        }

        protected void checkInitializedState() {
            getOwner().checkInitializedState();
        }
    }
}
