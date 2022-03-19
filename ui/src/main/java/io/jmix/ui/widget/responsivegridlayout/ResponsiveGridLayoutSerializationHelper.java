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

package io.jmix.ui.widget.responsivegridlayout;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaadin.server.Sizeable.Unit;
import io.jmix.ui.widget.JmixResponsiveGridLayout;
import io.jmix.ui.widget.JmixResponsiveGridLayout.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ResponsiveGridLayoutSerializationHelper {

    private static final String CONTAINER_BASE_STYLE_NAME = "container";
    private static final String ROW_BASE_STYLE_NAME = "row";
    private static final String COLUMN_BASE_STYLE_NAME = "col";

    private static final String STYLE_PROPERTY = "style";
    private static final String JTEST_ID_PROPERTY = "jTestId";

    private static final String WORD_SEPARATOR = "-";
    private static final String STYLE_SEPARATOR = " ";

    private ResponsiveGridLayoutSerializationHelper() {
    }

    public static String toJson(JsonObject jsonObject) {
        return createGson().toJson(jsonObject);
    }

    public static String toJson(JmixResponsiveGridLayout grid) {
        return toJson(serialize(grid));
    }

    public static JsonObject serialize(JmixResponsiveGridLayout grid) {
        JsonObject containerObject = new JsonObject();

        containerObject.addProperty(STYLE_PROPERTY, toStyle(grid.getContainerType()));

        List<Row> rows = grid.getRows();
        if (CollectionUtils.isNotEmpty(rows)) {
            JsonArray rowObjects = new JsonArray();

            for (Row row : rows) {
                JsonObject rowObject = serialize(row);
                rowObjects.add(rowObject);
            }

            containerObject.add("rows", rowObjects);
        }

        return containerObject;
    }

    public static JsonObject serialize(Row row) {
        JsonObject rowObject = new JsonObject();

        List<String> styles = new ArrayList<>();
        styles.add(ROW_BASE_STYLE_NAME);

        if (!row.isGuttersEnabled()) {
            styles.add("no-gutters");
        }

        styles.add(rowColumnsValueToStyle(row.getRowColumns()));
        styles.add(rowAlignItemsToStyle(row.getAlignItems()));
        styles.add(rowJustifyContentToStyle(row.getJustifyContent()));
        styles.add(row.getStyleName());

        rowObject.addProperty(STYLE_PROPERTY, joinStyles(STYLE_SEPARATOR, styles));
        if (!Strings.isNullOrEmpty(row.getJTestId())) {
            rowObject.addProperty(JTEST_ID_PROPERTY, row.getJTestId());
        }

        // A negative number implies an unspecified size, so we don't need to add it.
        if (row.getHeight() >= 0) {
            rowObject.addProperty("height", getCssSize(row.getHeight(), row.getHeightUnits()));
        }

        List<Column> columns = row.getColumns();
        if (CollectionUtils.isNotEmpty(columns)) {
            JsonArray colObjects = new JsonArray();

            for (Column column : columns) {
                JsonObject colObject = serialize(column);
                colObjects.add(colObject);
            }

            rowObject.add("cols", colObjects);
        }

        return rowObject;
    }

    public static JsonObject serialize(Column column) {
        JsonObject colObject = new JsonObject();

        List<String> styles = new ArrayList<>();
        styles.add(columnsValueToStyle(column.getColumns()));
        styles.add(columnAlignSelfToStyle(column.getAlignSelf()));
        styles.add(columnOrderToStyle(column.getOrder()));
        styles.add(columnOffsetToStyle(column.getOffset()));
        styles.add(column.getStyleName());

        colObject.addProperty(STYLE_PROPERTY, joinStyles(STYLE_SEPARATOR, styles));
        colObject.addProperty("columnId", column.getColumnId());
        if (!Strings.isNullOrEmpty(column.getJTestId())) {
            colObject.addProperty(JTEST_ID_PROPERTY, column.getJTestId());
        }

        return colObject;
    }

    public static String toStyle(ContainerType type) {
        return type == ContainerType.FLUID
                ? CONTAINER_BASE_STYLE_NAME + "-fluid"
                : CONTAINER_BASE_STYLE_NAME;
    }

    @Nullable
    private static String toStyle(Breakpoint breakpoint) {
        return breakpoint == Breakpoint.XS ? null : breakpoint.name().toLowerCase();
    }

    public static String rowColumnsValueToStyle(Map<Breakpoint, RowColumnsValue> rowColumns) {
        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, RowColumnsValue> entry : rowColumns.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }
        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, RowColumnsValue columnsValue) {
        return joinStyles(WORD_SEPARATOR,
                ROW_BASE_STYLE_NAME + "-cols",
                toStyle(breakpoint),
                toStyle(columnsValue)
        );
    }

    private static String toStyle(RowColumnsValue columnsValue) {
        return String.valueOf(columnsValue.getColumns());
    }

    public static String rowAlignItemsToStyle(Map<Breakpoint, AlignItems> alignItems) {
        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, AlignItems> entry : alignItems.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }

        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, AlignItems alignItems) {
        return joinStyles(WORD_SEPARATOR,
                "align-items",
                toStyle(breakpoint),
                alignItems.name().toLowerCase()
        );
    }

    public static String rowJustifyContentToStyle(Map<Breakpoint, JustifyContent> justifyContent) {
        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, JustifyContent> entry : justifyContent.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }

        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, JustifyContent justifyContent) {
        return joinStyles(WORD_SEPARATOR,
                "justify-content",
                toStyle(breakpoint),
                justifyContent.name().toLowerCase()
        );
    }

    public static String columnsValueToStyle(Map<Breakpoint, ColumnsValue> columns) {
        if (MapUtils.isEmpty(columns)) {
            return COLUMN_BASE_STYLE_NAME;
        }

        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, ColumnsValue> entry : columns.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }

        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, ColumnsValue columnsValue) {
        return joinStyles(WORD_SEPARATOR,
                COLUMN_BASE_STYLE_NAME,
                toStyle(breakpoint),
                toStyle(columnsValue)
        );
    }

    @Nullable
    private static String toStyle(ColumnsValue columnsValue) {
        if (columnsValue.isAuto()) {
            return "auto";
        }

        Integer columns = columnsValue.getColumns();
        return columns == null ? null : String.valueOf(columns);
    }

    public static String columnAlignSelfToStyle(Map<Breakpoint, AlignSelf> align) {
        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, AlignSelf> entry : align.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }

        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, AlignSelf alignSelf) {
        return joinStyles(WORD_SEPARATOR,
                "align-self",
                toStyle(breakpoint),
                alignSelf.name().toLowerCase()
        );
    }

    public static String columnOrderToStyle(Map<Breakpoint, OrderValue> orders) {
        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, OrderValue> entry : orders.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }

        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, OrderValue order) {
        return joinStyles(WORD_SEPARATOR,
                "order",
                toStyle(breakpoint),
                toStyle(order)
        );
    }

    private static String toStyle(OrderValue orderValue) {
        return orderValue.getValue() != null
                ? orderValue.getValue()
                : String.valueOf(orderValue.getOrder());
    }

    public static String columnOffsetToStyle(Map<Breakpoint, OffsetValue> offsets) {
        List<String> styles = new ArrayList<>();
        for (Map.Entry<Breakpoint, OffsetValue> entry : offsets.entrySet()) {
            styles.add(toStyle(entry.getKey(), entry.getValue()));
        }

        return joinStyles(STYLE_SEPARATOR, styles);
    }

    public static String toStyle(Breakpoint breakpoint, OffsetValue offset) {
        return joinStyles(WORD_SEPARATOR,
                "offset",
                toStyle(breakpoint),
                toStyle(offset)
        );
    }

    private static String toStyle(OffsetValue offset) {
        return String.valueOf(offset.getColumns());
    }

    private static String getCssSize(float size, Unit unit) {
        return size + unit.getSymbol();
    }

    private static Gson createGson() {
        return new GsonBuilder().create();
    }

    private static String joinStyles(String separator, Iterable<String> iterable) {
        return joinStyles(separator, StreamSupport.stream(iterable.spliterator(), false));
    }

    private static String joinStyles(String separator, String... styles) {
        return joinStyles(separator, Stream.of(styles));
    }

    private static String joinStyles(String separator, Stream<String> styles) {
        return styles
                .filter(s -> !Strings.isNullOrEmpty(s))
                .collect(Collectors.joining(separator));
    }
}
