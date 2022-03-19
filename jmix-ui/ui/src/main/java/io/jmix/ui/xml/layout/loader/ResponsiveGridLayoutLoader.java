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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ResponsiveGridLayout;
import io.jmix.ui.component.ResponsiveGridLayout.*;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponsiveGridLayoutLoader extends AbstractComponentLoader<ResponsiveGridLayout> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(ResponsiveGridLayout.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadCss(resultComponent, element);

        loadAlign(resultComponent, element);
        loadHeight(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadEnum(element, ContainerType.class, "containerType", resultComponent::setContainerType);

        loadRows(resultComponent, element);
    }

    protected void loadGridElement(GridElement gridElement, Element element) {
        loadString(element, "id", gridElement::setId);
        loadString(element, "stylename", gridElement::setStyleName);
    }

    protected void loadRows(ResponsiveGridLayout component, Element element) {
        List<Element> rowElements = element.elements("row");
        for (Element rowElement : rowElements) {
            Row row = component.addRow();
            loadRow(row, rowElement);
        }
    }

    protected void loadRow(Row row, Element element) {
        loadGridElement(row, element);

        loadBoolean(element, "guttersEnabled", row::setGuttersEnabled);
        loadString(element, "height", row::setHeight);

        loadRowColumns(row, element);
        loadRowAlignItems(row, element);
        loadRowJustifyContent(row, element);

        loadColumns(row, element);
    }

    protected void loadRowColumns(Row row, Element element) {
        Map<Breakpoint, RowColumnsValue> rowColumnsValueMap = new HashMap<>();

        String attributeBaseName = "cols";
        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadInteger(element, getAttributeFullName(attributeBaseName, breakpoint))
                    .ifPresent(cols ->
                            rowColumnsValueMap.put(breakpoint, RowColumnsValue.columns(cols)));
        }

        if (!rowColumnsValueMap.isEmpty()) {
            row.setRowColumns(rowColumnsValueMap);
        }
    }

    protected void loadRowAlignItems(Row row, Element element) {
        Map<Breakpoint, AlignItems> alignItemsMap = new HashMap<>();

        String attributeBaseName = "alignItems";
        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadEnum(element, AlignItems.class,
                    getAttributeFullName(attributeBaseName, breakpoint))
                    .ifPresent(alignItems ->
                            alignItemsMap.put(breakpoint, alignItems));
        }

        if (!alignItemsMap.isEmpty()) {
            row.setAlignItems(alignItemsMap);
        }
    }

    protected void loadRowJustifyContent(Row row, Element element) {
        Map<Breakpoint, JustifyContent> justifyContentMap = new HashMap<>();

        String attributeBaseName = "justifyContent";
        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadEnum(element, JustifyContent.class,
                    getAttributeFullName(attributeBaseName, breakpoint))
                    .ifPresent(justifyContent ->
                            justifyContentMap.put(breakpoint, justifyContent));
        }

        if (!justifyContentMap.isEmpty()) {
            row.setJustifyContent(justifyContentMap);
        }
    }

    protected void loadColumns(Row row, Element element) {
        List<Element> colElements = element.elements("col");
        for (Element colElement : colElements) {
            Column column = row.addColumn();
            loadColumn(column, colElement);
        }
    }

    protected void loadColumn(Column column, Element element) {
        loadGridElement(column, element);

        loadColumnColumnsValue(column, element);
        loadColumnAlignSelf(column, element);
        loadColumnOrderValue(column, element);
        loadColumnOffsetValue(column, element);

        loadComponent(column, element);
    }

    protected void loadComponent(Column column, Element element) {
        List<Element> elements = element.elements();
        if (elements.isEmpty()) {
            throw new GuiDevelopmentException(
                    "ResponsiveGridLayout column element can't be empty", context);
        }
        if (elements.size() > 1) {
            throw new GuiDevelopmentException(
                    "ResponsiveGridLayout column element must contain only one component", context);
        }

        Element contentElement = elements.get(0);

        LayoutLoader loader = getLayoutLoader();
        ComponentLoader<?> childComponentLoader = loader.createComponent(contentElement);
        childComponentLoader.loadComponent();

        Component component = childComponentLoader.getResultComponent();
        column.setComponent(component);
    }

    protected void loadColumnColumnsValue(Column column, Element element) {
        Map<Breakpoint, ColumnsValue> columnsValueMap = new HashMap<>();

        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadString(element, breakpoint.name().toLowerCase())
                    .ifPresent(columnStr ->
                            columnsValueMap.put(breakpoint, createColumnValue(columnStr)));
        }

        if (!columnsValueMap.isEmpty()) {
            column.setColumns(columnsValueMap);
        }
    }

    protected ColumnsValue createColumnValue(String columnStr) {
        if ("DEFAULT".equalsIgnoreCase(columnStr)) {
            return ColumnsValue.DEFAULT;
        }

        if ("AUTO".equalsIgnoreCase(columnStr)) {
            return ColumnsValue.AUTO;
        }

        return ColumnsValue.columns(Integer.parseInt(columnStr));
    }

    protected void loadColumnAlignSelf(Column column, Element element) {
        Map<Breakpoint, AlignSelf> alignsMap = new HashMap<>();

        String attributeBaseName = "alignSelf";
        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadEnum(element, AlignSelf.class,
                    getAttributeFullName(attributeBaseName, breakpoint))
                    .ifPresent(alignSelf ->
                            alignsMap.put(breakpoint, alignSelf));
        }

        if (!alignsMap.isEmpty()) {
            column.setAlignSelf(alignsMap);
        }
    }

    protected void loadColumnOrderValue(Column column, Element element) {
        Map<Breakpoint, OrderValue> orderValueMap = new HashMap<>();

        String attributeBaseName = "order";
        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadString(element, getAttributeFullName(attributeBaseName, breakpoint))
                    .ifPresent(orderStr ->
                            orderValueMap.put(breakpoint, createOrderValue(orderStr)));
        }

        if (!orderValueMap.isEmpty()) {
            column.setOrder(orderValueMap);
        }
    }

    protected OrderValue createOrderValue(String orderStr) {
        if (StringUtils.equalsIgnoreCase(OrderValue.LAST.getValue(), orderStr)) {
            return OrderValue.LAST;
        }

        if (StringUtils.equalsIgnoreCase(OrderValue.FIRST.getValue(), orderStr)) {
            return OrderValue.FIRST;
        }

        return StringUtils.isNumeric(orderStr)
                ? OrderValue.columns(Integer.parseInt(orderStr))
                : new OrderValue(orderStr);
    }

    protected void loadColumnOffsetValue(Column column, Element element) {
        Map<Breakpoint, OffsetValue> offsetValueMap = new HashMap<>();

        String attributeBaseName = "offset";
        for (Breakpoint breakpoint : Breakpoint.values()) {
            loadInteger(element, getAttributeFullName(attributeBaseName, breakpoint))
                    .ifPresent(offset ->
                            offsetValueMap.put(breakpoint, OffsetValue.columns(offset)));
        }

        if (!offsetValueMap.isEmpty()) {
            column.setOffset(offsetValueMap);
        }
    }

    protected String getAttributeFullName(String attributeBaseName, Breakpoint breakpoint) {
        return breakpoint == Breakpoint.XS
                ? attributeBaseName
                : attributeBaseName + StringUtils.capitalize(breakpoint.name().toLowerCase());
    }
}
