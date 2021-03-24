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

package io.jmix.ui.widget;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.SizeWithUnit;
import com.vaadin.shared.EventId;
import com.vaadin.shared.Registration;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import io.jmix.ui.widget.client.responsivegridlayout.JmixResponsiveGridLayoutServerRpc;
import io.jmix.ui.widget.client.responsivegridlayout.JmixResponsiveGridLayoutState;
import io.jmix.ui.widget.responsivegridlayout.ResponsiveGridLayoutSerializationHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

@WebJarResource("bootstrap:css/bootstrap-grid.min.css")
public class JmixResponsiveGridLayout extends AbstractComponent
        implements HasComponents, HasComponents.ComponentAttachDetachNotifier, LayoutEvents.LayoutClickNotifier {

    private static final Logger log = LoggerFactory.getLogger(JmixResponsiveGridLayout.class);

    protected ContainerType containerType;
    protected List<Row> rowOrder = new ArrayList<>();

    protected KeyMapper<Column> columnIdMap = new KeyMapper<>();
    protected Map<String, Component> slots = new HashMap<>();

    protected boolean initialized = false;

    public JmixResponsiveGridLayout() {
        registerRpc(createRpc());
        initComponent();
    }

    protected JmixResponsiveGridLayoutServerRpc createRpc() {
        return (mouseDetails, clickedConnector) -> {
            Component layout = JmixResponsiveGridLayout.this;
            // Because JmixResponsiveGridLayout doesn't extend ComponentContainer,
            // we can't use LayoutEvents.LayoutClickEvent.createEvent and need to
            // find a proper 'childComponent' manually
            Component clickedComponent = (Component) clickedConnector;
            Component childComponent = clickedComponent;
            while (childComponent != null
                    && childComponent.getParent() != layout) {
                childComponent = childComponent.getParent();
            }

            LayoutClickEvent event = new LayoutClickEvent(layout, mouseDetails,
                    clickedComponent, childComponent);
            fireEvent(event);
        };
    }

    protected void initComponent() {
        containerType = ContainerType.FLUID;
    }

    protected String getColumnId(Column column) {
        return columnIdMap.key(column);
    }

    protected void removeColumnId(Column column) {
        columnIdMap.remove(column);
    }

    public Row addRow() {
        return addRow(rowOrder.size());
    }

    public Row addRow(int index) {
        checkInitializedState();

        Row row = new Row(this);
        rowOrder.add(index, row);

        return row;
    }

    public void removeRow(Row row) {
        checkInitializedState();

        if (rowOrder.remove(row)) {
            row.removeAllColumns();
        } else {
            throw new IllegalArgumentException(
                    "Row can't be removed because it does not belong to the ResponsiveGridLayout");
        }
    }

    public void removeAllRows() {
        List<Row> rows = new ArrayList<>(rowOrder);
        for (Row row : rows) {
            removeRow(row);
        }
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rowOrder);
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        checkInitializedState();

        if (!Objects.equals(this.containerType, containerType)) {
            this.containerType = containerType;
        }
    }

    public enum ContainerType {
        FIXED,
        FLUID
    }

    public enum Breakpoint {
        XS,
        SM,
        MD,
        LG,
        XL
    }

    @Override
    public Iterator<Component> iterator() {
        return Collections.unmodifiableCollection(slots.values()).iterator();
    }

    @Override
    public Registration addComponentAttachListener(ComponentAttachListener listener) {
        return addListener(ComponentAttachEvent.class, listener, ComponentAttachListener.attachMethod);
    }

    @Override
    public void removeComponentAttachListener(ComponentAttachListener listener) {
        removeListener(ComponentAttachEvent.class, listener, ComponentAttachListener.attachMethod);
    }

    @Override
    public Registration addComponentDetachListener(ComponentDetachListener listener) {
        return addListener(ComponentDetachEvent.class, listener, ComponentDetachListener.detachMethod);
    }

    @Override
    public void removeComponentDetachListener(ComponentDetachListener listener) {
        removeListener(ComponentDetachEvent.class, listener, ComponentDetachListener.detachMethod);
    }

    protected void addComponent(Component component, String columnId) {
        Component old = slots.get(columnId);
        if (old != null) {
            removeComponent(old);
        }

        slots.put(columnId, component);
        getState().childLocations.put(component, columnId);

        // Make sure we're not adding the component inside it's own content
        if (isOrHasAncestor(component)) {
            throw new IllegalArgumentException("Component cannot be added inside it's own content");
        }

        if (component.getParent() != null) {
            // If the component already has a parent, try to remove it
            AbstractSingleComponentContainer.removeFromParent(component);
        }

        component.setParent(this);

        fireComponentAttachEvent(component);
        markAsDirty();
    }

    protected void removeComponent(@Nullable Component component) {
        if (component == null) {
            return;
        }

        slots.values().remove(component);
        getState().childLocations.remove(component);

        if (equals(component.getParent())) {
            component.setParent(null);

            fireComponentDetachEvent(component);
            markAsDirty();
        }
    }

    protected void removeComponent(String location) {
        removeComponent(slots.get(location));
    }

    protected void fireComponentAttachEvent(Component component) {
        fireEvent(new ComponentAttachEvent(this, component));
    }

    protected void fireComponentDetachEvent(Component component) {
        fireEvent(new ComponentDetachEvent(this, component));
    }

    @Override
    public Registration addLayoutClickListener(LayoutEvents.LayoutClickListener listener) {
        return addListener(EventId.LAYOUT_CLICK_EVENT_IDENTIFIER,
                LayoutClickEvent.class, listener,
                LayoutEvents.LayoutClickListener.clickMethod);
    }

    @Override
    @Deprecated
    public void removeLayoutClickListener(LayoutEvents.LayoutClickListener listener) {
        removeListener(EventId.LAYOUT_CLICK_EVENT_IDENTIFIER,
                LayoutClickEvent.class, listener);
    }

    @Override
    protected JmixResponsiveGridLayoutState getState() {
        return (JmixResponsiveGridLayoutState) super.getState();
    }

    @Override
    protected JmixResponsiveGridLayoutState getState(boolean markAsDirty) {
        return (JmixResponsiveGridLayoutState) super.getState(markAsDirty);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (initial) {
            String configurationJsonString = generateJson();
            log.trace("ResponsiveGridLayout configuration JSON:\n{}", configurationJsonString);

            getState().configuration = configurationJsonString;

            initialized = true;
        }
    }

    protected String generateJson() {
        return ResponsiveGridLayoutSerializationHelper.toJson(this);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        throw new UnsupportedOperationException("JmixResponsiveGridLayout doesn't support explicit width value");
    }

    @Override
    public boolean isResponsive() {
        return false;
    }

    @Override
    public void setResponsive(boolean responsive) {
        throw new UnsupportedOperationException("JmixResponsiveGridLayout is responsive by its nature");
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected void checkInitializedState() {
        if (isInitialized()) {
            throw new IllegalStateException("JmixResponsiveGridLayout doesn't support changing " +
                    "its configuration after it has been initialized");
        }
    }

    /* Row */

    public static class Row extends AbstractGridElement {

        protected List<Column> columnOrder = new ArrayList<>();
        protected boolean guttersEnabled;

        protected float height;
        protected Unit heightUnit;

        protected Map<Breakpoint, RowColumnsValue> rowColumns = new HashMap<>();
        protected Map<Breakpoint, AlignItems> alignItems = new HashMap<>();
        protected Map<Breakpoint, JustifyContent> justifyContent = new HashMap<>();

        public Row(JmixResponsiveGridLayout owner) {
            super(owner);
            initRow();
        }

        protected void initRow() {
            guttersEnabled = true;
            setHeightUndefined();
        }

        public Column addColumn() {
            return addColumn(columnOrder.size());
        }

        public Column addColumn(int index) {
            checkInitializedState();

            Column column = new Column(getOwner());
            columnOrder.add(index, column);

            String columnId = getOwner().getColumnId(column);
            column.setColumnId(columnId);

            return column;
        }

        public void removeColumn(Column column) {
            checkInitializedState();

            if (columnOrder.remove(column)) {
                getOwner().removeComponent(column.getComponent());
                getOwner().removeColumnId(column);
            } else {
                throw new IllegalArgumentException("Column can't be removed because it does not belong to the row");
            }
        }

        public void removeAllColumns() {
            List<Column> columns = new ArrayList<>(columnOrder);
            for (Column column : columns) {
                removeColumn(column);
            }
        }

        public List<Column> getColumns() {
            return Collections.unmodifiableList(columnOrder);
        }

        public boolean isGuttersEnabled() {
            return guttersEnabled;
        }

        public void setGuttersEnabled(boolean guttersEnabled) {
            checkInitializedState();

            if (this.guttersEnabled != guttersEnabled) {
                this.guttersEnabled = guttersEnabled;
            }
        }

        public float getHeight() {
            return height;
        }

        public Unit getHeightUnits() {
            return heightUnit;
        }

        public void setHeight(String height) {
            checkInitializedState();

            SizeWithUnit size = SizeWithUnit.parseStringSize(height);
            if (size != null) {
                setHeight(size.getSize(), size.getUnit());
            } else {
                setHeight(-1, Unit.PIXELS);
            }
        }

        public void setHeight(float height, Unit unit) {
            checkInitializedState();

            this.height = height;
            heightUnit = unit;
        }

        public void setHeightUndefined() {
            setHeight(-1, Unit.PIXELS);
        }

        public Map<Breakpoint, RowColumnsValue> getRowColumns() {
            return MapUtils.unmodifiableMap(rowColumns);
        }

        public void setRowColumns(RowColumnsValue columnsValue) {
            setRowColumns(Breakpoint.XS, columnsValue);
        }

        public void setRowColumns(Breakpoint breakpoint, RowColumnsValue columnsValue) {
            setRowColumns(Collections.singletonMap(breakpoint, columnsValue));
        }

        public void setRowColumns(Map<Breakpoint, RowColumnsValue> columnsValue) {
            checkInitializedState();

            rowColumns = new HashMap<>(MapUtils.emptyIfNull(columnsValue));
        }

        public void addRowColumns(Breakpoint breakpoint, RowColumnsValue columnsValue) {
            checkInitializedState();

            rowColumns.put(breakpoint, columnsValue);
        }

        public void removeRowColumns(Breakpoint breakpoint) {
            checkInitializedState();

            rowColumns.remove(breakpoint);
        }

        public Map<Breakpoint, AlignItems> getAlignItems() {
            return MapUtils.unmodifiableMap(alignItems);
        }

        public void setAlignItems(AlignItems alignItems) {
            setAlignItems(Breakpoint.XS, alignItems);
        }

        public void setAlignItems(Breakpoint breakpoint, AlignItems alignItems) {
            setAlignItems(Collections.singletonMap(breakpoint, alignItems));
        }

        public void setAlignItems(Map<Breakpoint, AlignItems> alignItems) {
            checkInitializedState();

            this.alignItems = new HashMap<>(MapUtils.emptyIfNull(alignItems));
        }

        public void addAlignItems(Breakpoint breakpoint, AlignItems alignItems) {
            checkInitializedState();

            this.alignItems.put(breakpoint, alignItems);
        }

        public void removeAlignItems(Breakpoint breakpoint) {
            checkInitializedState();

            alignItems.remove(breakpoint);
        }

        public Map<Breakpoint, JustifyContent> getJustifyContent() {
            return MapUtils.unmodifiableMap(justifyContent);
        }

        public void setJustifyContent(JustifyContent justifyContent) {
            setJustifyContent(Breakpoint.XS, justifyContent);
        }

        public void setJustifyContent(Breakpoint breakpoint, JustifyContent justifyContent) {
            setJustifyContent(Collections.singletonMap(breakpoint, justifyContent));
        }

        public void setJustifyContent(Map<Breakpoint, JustifyContent> justifyContent) {
            checkInitializedState();

            this.justifyContent = new HashMap<>(MapUtils.emptyIfNull(justifyContent));
        }

        public void addJustifyContent(Breakpoint breakpoint, JustifyContent justifyContent) {
            checkInitializedState();

            this.justifyContent.put(breakpoint, justifyContent);
        }

        public void removeJustifyContent(Breakpoint breakpoint) {
            checkInitializedState();

            justifyContent.remove(breakpoint);
        }
    }

    public enum AlignItems {
        START,
        CENTER,
        END,
        BASELINE,
        STRETCH
    }

    public enum JustifyContent {
        START,
        CENTER,
        END,
        AROUND,
        BETWEEN
    }

    public static class RowColumnsValue {

        protected int cols;

        public RowColumnsValue(int cols) {
            Preconditions.checkArgument(cols > 0,
                    "Columns value must be greater than 0");
            this.cols = cols;
        }

        public static RowColumnsValue columns(int cols) {
            return new RowColumnsValue(cols);
        }

        public int getColumns() {
            return cols;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof RowColumnsValue)) {
                return false;
            }

            RowColumnsValue that = (RowColumnsValue) o;
            return cols == that.cols;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cols);
        }
    }

    /* Column */

    public static class Column extends AbstractGridElement {

        protected String columnId;

        protected Component component;

        protected Map<Breakpoint, ColumnsValue> columns = new HashMap<>();
        protected Map<Breakpoint, AlignSelf> aligns = new HashMap<>();
        protected Map<Breakpoint, OrderValue> orders = new HashMap<>();
        protected Map<Breakpoint, OffsetValue> offsets = new HashMap<>();

        public Column(JmixResponsiveGridLayout owner) {
            super(owner);
        }

        public String getColumnId() {
            return columnId;
        }

        public void setColumnId(String columnId) {
            checkInitializedState();

            this.columnId = columnId;
        }

        @Nullable
        public Component getComponent() {
            return component;
        }

        public void setComponent(@Nullable Component component) {
            this.component = component;

            if (component != null) {
                getOwner().addComponent(component, columnId);
            } else {
                getOwner().removeComponent(columnId);
            }
        }

        public Map<Breakpoint, ColumnsValue> getColumns() {
            return MapUtils.unmodifiableMap(columns);
        }

        public void setColumns(ColumnsValue columnsValue) {
            setColumns(Breakpoint.XS, columnsValue);
        }

        public void setColumns(Breakpoint breakpoint, ColumnsValue columnsValue) {
            setColumns(Collections.singletonMap(breakpoint, columnsValue));
        }

        public void setColumns(Map<Breakpoint, ColumnsValue> columns) {
            checkInitializedState();

            this.columns = new HashMap<>(MapUtils.emptyIfNull(columns));
        }

        public void addColumns(Breakpoint breakpoint, ColumnsValue columnsValue) {
            checkInitializedState();

            columns.put(breakpoint, columnsValue);
        }

        public void removeColumns(Breakpoint breakpoint) {
            checkInitializedState();

            columns.remove(breakpoint);
        }

        public Map<Breakpoint, AlignSelf> getAlignSelf() {
            return MapUtils.unmodifiableMap(aligns);
        }

        public void setAlignSelf(AlignSelf alignSelf) {
            setAlignSelf(Breakpoint.XS, alignSelf);
        }

        public void setAlignSelf(Breakpoint breakpoint, AlignSelf alignSelf) {
            setAlignSelf(Collections.singletonMap(breakpoint, alignSelf));
        }

        public void setAlignSelf(Map<Breakpoint, AlignSelf> aligns) {
            checkInitializedState();

            this.aligns = new HashMap<>(MapUtils.emptyIfNull(aligns));
        }

        public void addAlignSelf(Breakpoint breakpoint, AlignSelf alignSelf) {
            checkInitializedState();

            aligns.put(breakpoint, alignSelf);
        }

        public void removeAlignSelf(Breakpoint breakpoint) {
            checkInitializedState();

            aligns.remove(breakpoint);
        }

        public Map<Breakpoint, OrderValue> getOrder() {
            return MapUtils.unmodifiableMap(orders);
        }

        public void setOrder(OrderValue orderValue) {
            setOrder(Breakpoint.XS, orderValue);
        }

        public void setOrder(Breakpoint breakpoint, OrderValue orderValue) {
            setOrder(Collections.singletonMap(breakpoint, orderValue));
        }

        public void setOrder(Map<Breakpoint, OrderValue> orders) {
            checkInitializedState();

            this.orders = new HashMap<>(MapUtils.emptyIfNull(orders));
        }

        public void addOrder(Breakpoint breakpoint, OrderValue orderValue) {
            checkInitializedState();

            orders.put(breakpoint, orderValue);
        }

        public void removeOrder(Breakpoint breakpoint) {
            checkInitializedState();

            orders.remove(breakpoint);
        }

        public Map<Breakpoint, OffsetValue> getOffset() {
            return MapUtils.unmodifiableMap(offsets);
        }

        public void setOffset(OffsetValue offsetValue) {
            setOffset(Breakpoint.XS, offsetValue);
        }

        public void setOffset(Breakpoint breakpoint, OffsetValue offsetValue) {
            setOffset(Collections.singletonMap(breakpoint, offsetValue));
        }

        public void setOffset(Map<Breakpoint, OffsetValue> offsets) {
            checkInitializedState();

            this.offsets = new HashMap<>(MapUtils.emptyIfNull(offsets));
        }

        public void addOffset(Breakpoint breakpoint, OffsetValue offsetValue) {
            checkInitializedState();

            offsets.put(breakpoint, offsetValue);
        }

        public void removeOffset(Breakpoint breakpoint) {
            checkInitializedState();

            offsets.remove(breakpoint);
        }
    }

    public enum AlignSelf {
        AUTO,
        START,
        CENTER,
        END,
        BASELINE,
        STRETCH
    }

    public static class ColumnsValue {

        public static ColumnsValue DEFAULT = new ColumnsValue(null);
        public static ColumnsValue AUTO = new ColumnsValue(true);

        protected Integer cols;
        protected boolean auto;

        public ColumnsValue(@Nullable Integer cols) {
            this(cols, false);
        }

        public ColumnsValue(boolean auto) {
            this(null, auto);
        }

        protected ColumnsValue(@Nullable Integer cols, boolean auto) {
            Preconditions.checkArgument(cols == null || cols > 0,
                    "Columns value must be either 'null' for auto-layout or be greater than 0");

            this.cols = cols;
            this.auto = auto;
        }

        public static ColumnsValue columns(@Nullable Integer cols) {
            return new ColumnsValue(cols);
        }

        @Nullable
        public Integer getColumns() {
            return cols;
        }

        public boolean isAuto() {
            return auto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof ColumnsValue)) {
                return false;
            }

            ColumnsValue that = (ColumnsValue) o;
            return auto == that.auto &&
                    Objects.equals(cols, that.cols);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cols, auto);
        }
    }

    public static class OrderValue {

        public static OrderValue FIRST = new OrderValue("first");
        public static OrderValue LAST = new OrderValue("last");

        protected Integer order;
        protected String value;

        public OrderValue(int order) {
            this(order, null);
        }

        public OrderValue(String value) {
            this(null, value);
        }

        protected OrderValue(@Nullable Integer order, @Nullable String value) {
            Preconditions.checkArgument(order != null && value == null
                            || order == null && value != null,
                    "Either numeric value or constant value can be set at a time");
            Preconditions.checkArgument(order == null || order >= 0,
                    "Order value must be greater or equal to 0");

            this.order = order;
            this.value = value;
        }

        public static OrderValue columns(int order) {
            return new OrderValue(order);
        }

        @Nullable
        public Integer getOrder() {
            return order;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof OrderValue)) {
                return false;
            }

            OrderValue that = (OrderValue) o;
            return Objects.equals(order, that.order) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(order, value);
        }
    }

    public static class OffsetValue {

        protected int cols;

        public OffsetValue(int cols) {
            Preconditions.checkArgument(cols >= 0,
                    "Columns value must be greater or equal to 0");
            this.cols = cols;
        }

        public static OffsetValue columns(int cols) {
            return new OffsetValue(cols);
        }

        public int getColumns() {
            return cols;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof OffsetValue)) {
                return false;
            }

            OffsetValue that = (OffsetValue) o;
            return cols == that.cols;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cols);
        }
    }

    public static abstract class AbstractGridElement {

        protected JmixResponsiveGridLayout owner;
        protected List<String> styles;
        protected String jTestId;

        public AbstractGridElement(JmixResponsiveGridLayout owner) {
            this.owner = owner;
        }

        public JmixResponsiveGridLayout getOwner() {
            return owner;
        }

        public String getJTestId() {
            return jTestId;
        }

        public void setJTestId(String jTestId) {
            checkInitializedState();

            this.jTestId = jTestId;
        }

        public String getStyleName() {
            return CollectionUtils.isNotEmpty(styles)
                    ? String.join(" ", styles)
                    : "";
        }

        public void setStyleName(String style) {
            checkInitializedState();

            if (Strings.isNullOrEmpty(style)) {
                styles = null;
                return;
            }

            initStyles();
            styles.clear();
            addStylesInternal(style);
        }

        public void addStyleName(String style) {
            checkInitializedState();

            if (Strings.isNullOrEmpty(style)) {
                return;
            }

            if (styles != null && styles.contains(style)) {
                return;
            }

            initStyles();
            addStylesInternal(style);
        }

        public void removeStyleName(String style) {
            checkInitializedState();

            String[] stylesToRemove = style.split(" ");
            for (String styleToRemove : stylesToRemove) {
                styles.remove(styleToRemove);
            }
        }

        protected void initStyles() {
            if (styles == null) {
                styles = new ArrayList<>();
            }
        }

        protected void addStylesInternal(String styleStr) {
            String[] stylesToAdd = styleStr.split(" ");
            for (String style : stylesToAdd) {
                if (!style.isEmpty() && !styles.contains(style)) {
                    styles.add(style);
                }
            }
        }

        protected void checkInitializedState() {
            getOwner().checkInitializedState();
        }
    }
}
