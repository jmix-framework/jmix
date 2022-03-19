/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.component.impl;


import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataProvider;
import io.jmix.pivottable.PivotTableLocaleHelper;
import io.jmix.pivottable.component.PivotTable;
import io.jmix.pivottable.model.*;
import io.jmix.pivottable.serialization.impl.JmixPivotTableSerializer;
import io.jmix.pivottable.widget.JmixPivotTable;
import io.jmix.pivottable.widget.events.CellClickListener;
import io.jmix.pivottable.widget.events.RefreshListener;
import io.jmix.pivottable.widget.serialization.PivotTableSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class PivotTableImpl extends AbstractComponent<JmixPivotTable> implements PivotTable, InitializingBean {

    protected RefreshListener refreshHandler;
    protected CellClickListener cellClickHandler;

    protected Messages messages;
    protected MessageTools messageTools;
    protected CurrentAuthentication currentAuthentication;
    protected PivotTableLocaleHelper pivotTableLocaleHelper;

    public PivotTableImpl() {
        component = createComponent();
    }

    protected JmixPivotTable createComponent() {
        return new JmixPivotTable();
    }

    protected PivotTableSerializer createPivotTableSerializer() {
        return applicationContext.getBean(JmixPivotTableSerializer.class);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setPivotTableLocaleHelper(PivotTableLocaleHelper helper) {
        this.pivotTableLocaleHelper = helper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        component.setPivotTableSerializer(createPivotTableSerializer());

        initLocale();
    }

    protected void initLocale() {
        Locale locale = currentAuthentication.getLocale();
        String localeString = LocaleResolver.localeToString(locale);
        if (!Objects.equals(localeString, component.getLocaleString())) {
            component.setPivotTableMessages(localeString, pivotTableLocaleHelper.getPivotTableLocaleMap(locale));
            component.setLocaleString(localeString);
            component.setEmptyDataMessage(messages.getMessage("pivottable.emptyDataMessage", locale));
        }
    }

    @Override
    public void repaint() {
        component.repaint();
    }

    @Override
    public boolean isEditable() {
        return component.getPivotTable().getEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        if (editable != component.getPivotTable().getEditable()) {
            component.getPivotTable().setEditable(editable);

            component.repaint();
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return component.getPivotTable().getProperties();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        component.getPivotTable().setProperties(properties);
    }

    @Override
    public void addProperties(Map<String, String> properties) {
        component.getPivotTable().addProperties(properties);
    }

    @Override
    public void addProperty(String property, String value) {
        component.getPivotTable().addProperty(property, value);
    }

    @Override
    public List<String> getRows() {
        return component.getPivotTable().getRows();
    }

    @Override
    public void setRows(List<String> rows) {
        component.getPivotTable().setRows(rows);
    }

    @Override
    public void addRows(String... rows) {
        component.getPivotTable().addRows(rows);
    }

    @Override
    public List<String> getColumns() {
        return component.getPivotTable().getCols();
    }

    @Override
    public void setColumns(List<String> cols) {
        component.getPivotTable().setCols(cols);
    }

    @Override
    public void addColumns(String... cols) {
        component.getPivotTable().addCols(cols);
    }

    @Override
    public Aggregation getAggregation() {
        return component.getPivotTable().getAggregation();
    }

    @Override
    public void setAggregation(Aggregation aggregation) {
        component.getPivotTable().setAggregation(aggregation);
    }

    @Override
    public Renderer getRenderer() {
        return component.getPivotTable().getRenderer();
    }

    @Override
    public void setRenderer(Renderer renderer) {
        component.getPivotTable().setRenderer(renderer);
    }

    @Override
    public List<String> getAggregationProperties() {
        return component.getPivotTable().getAggregationProperties();
    }

    @Override
    public void setAggregationProperties(List<String> aggregationProperties) {
        component.getPivotTable().setAggregationProperties(aggregationProperties);
    }

    @Override
    public void addAggregationProperties(String... aggregationProperties) {
        component.getPivotTable().addAggregationProperties(aggregationProperties);
    }

    @Override
    public Aggregations getAggregations() {
        return component.getPivotTable().getAggregations();
    }

    @Override
    public void setAggregations(Aggregations aggregations) {
        component.getPivotTable().setAggregations(aggregations);
    }

    @Override
    public Renderers getRenderers() {
        return component.getPivotTable().getRenderers();
    }

    @Override
    public void setRenderers(Renderers renderers) {
        component.getPivotTable().setRenderers(renderers);
    }

    @Override
    public List<String> getHiddenProperties() {
        return component.getPivotTable().getHiddenProperties();
    }

    @Override
    public void setHiddenProperties(List<String> hiddenProperties) {
        component.getPivotTable().setHiddenProperties(hiddenProperties);
    }

    @Override
    public void addHiddenProperties(String... hiddenProperties) {
        component.getPivotTable().addHiddenProperties(hiddenProperties);
    }

    @Override
    public List<String> getHiddenFromAggregations() {
        return component.getPivotTable().getHiddenFromAggregations();
    }

    @Override
    public void setHiddenFromAggregations(List<String> hiddenFromAggregations) {
        component.getPivotTable().setHiddenFromAggregations(hiddenFromAggregations);
    }

    @Override
    public void addHiddenFromAggregations(String... hiddenFromAggregations) {
        component.getPivotTable().addHiddenFromAggregations(hiddenFromAggregations);
    }

    @Override
    public List<String> getHiddenFromDragDrop() {
        return component.getPivotTable().getHiddenFromDragDrop();
    }

    @Override
    public void setHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        component.getPivotTable().setHiddenFromDragDrop(hiddenFromDragDrop);
    }

    @Override
    public void addHiddenFromDragDrop(String... hiddenFromDragDrop) {
        component.getPivotTable().addHiddenFromDragDrop(hiddenFromDragDrop);
    }

    @Override
    public ColumnOrder getColumnOrder() {
        return component.getPivotTable().getColumnOrder();
    }

    @Override
    public void setColumnOrder(ColumnOrder columnOrder) {
        component.getPivotTable().setColumnOrder(columnOrder);
    }

    @Override
    public RowOrder getRowOrder() {
        return component.getPivotTable().getRowOrder();
    }

    @Override
    public void setRowOrder(RowOrder rowOrder) {
        component.getPivotTable().setRowOrder(rowOrder);
    }

    @Override
    public Integer getMenuLimit() {
        return component.getPivotTable().getMenuLimit();
    }

    @Override
    public void setMenuLimit(Integer menuLimit) {
        component.getPivotTable().setMenuLimit(menuLimit);
    }

    @Override
    public Boolean getAutoSortUnusedProperties() {
        return component.getPivotTable().getAutoSortUnusedProperties();
    }

    @Override
    public void setAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        component.getPivotTable().setAutoSortUnusedProperties(autoSortUnusedProperties);
    }

    @Override
    public UnusedPropertiesVertical getUnusedPropertiesVertical() {
        return component.getPivotTable().getUnusedPropertiesVertical();
    }

    @Override
    public void setUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        component.getPivotTable().setUnusedPropertiesVertical(unusedPropertiesVertical);
    }

    @Override
    public DataProvider getDataProvider() {
        return component.getPivotTable().getDataProvider();
    }

    @Override
    public void setDataProvider(DataProvider dataProvider) {
        component.getPivotTable().setDataProvider(dataProvider);
        dataProvider.addChangeListener(e -> {
            if (!isEditable()) {
                repaint();
            }
        });
    }

    @Override
    public void addData(DataItem... dataItems) {
        component.getPivotTable().addData(dataItems);
    }

    @Override
    public JsFunction getFilterFunction() {
        return component.getPivotTable().getFilterFunction();
    }

    @Override
    public void setFilterFunction(JsFunction filter) {
        component.getPivotTable().setFilterFunction(filter);
    }

    @Override
    public JsFunction getSortersFunction() {
        return component.getPivotTable().getSortersFunction();
    }

    @Override
    public void setSortersFunction(JsFunction sorters) {
        component.getPivotTable().setSortersFunction(sorters);
    }

    @Override
    public RendererOptions getRendererOptions() {
        return component.getPivotTable().getRendererOptions();
    }

    @Override
    public void setRendererOptions(RendererOptions rendererOptions) {
        component.getPivotTable().setRendererOptions(rendererOptions);
    }

    @Override
    public Map<String, List<String>> getInclusions() {
        return component.getPivotTable().getInclusions();
    }

    @Override
    public void setInclusions(Map<String, List<String>> inclusions) {
        component.getPivotTable().setInclusions(inclusions);
    }

    @Override
    public void setInclusions(String property, List<String> inclusions) {
        component.getPivotTable().setInclusions(property, inclusions);
    }

    @Override
    public void addInclusions(String property, String... inclusions) {
        component.getPivotTable().addInclusions(property, inclusions);
    }

    @Override
    public Map<String, List<String>> getExclusions() {
        return component.getPivotTable().getExclusions();
    }

    @Override
    public void setExclusions(Map<String, List<String>> exclusions) {
        component.getPivotTable().setExclusions(exclusions);
    }

    @Override
    public void setExclusions(String property, List<String> exclusions) {
        component.getPivotTable().setExclusions(property, exclusions);
    }

    @Override
    public void addExclusions(String property, String... exclusions) {
        component.getPivotTable().addExclusions(property, exclusions);
    }

    @Override
    public DerivedProperties getDerivedProperties() {
        return component.getPivotTable().getDerivedProperties();
    }

    @Override
    public void setDerivedProperties(DerivedProperties derivedProperties) {
        component.getPivotTable().setDerivedProperties(derivedProperties);
    }

    @Override
    public String getNativeJson() {
        return component.getJson();
    }

    @Override
    public void setNativeJson(String json) {
        component.setJson(json);
    }

    @Override
    public String getEmptyDataMessage() {
        return component.getEmptyDataMessage();
    }

    @Override
    public void setEmptyDataMessage(String emptyDataMessage) {
        component.setEmptyDataMessage(emptyDataMessage);
    }

    @Override
    public Subscription addRefreshListener(Consumer<RefreshEvent> refreshListener) {
        if (refreshHandler == null) {
            refreshHandler = this::onRefresh;
            component.addRefreshListener(refreshHandler);
        }
        return getEventHub().subscribe(RefreshEvent.class, refreshListener);
    }

    protected void onRefresh(io.jmix.pivottable.widget.events.RefreshEvent e) {
        RefreshEvent event = new RefreshEvent(PivotTableImpl.this,
                e.getRows(), e.getCols(), e.getRenderer(),
                e.getAggregation(), e.getAggregationProperties(),
                e.getInclusions(), e.getExclusions(),
                e.getColumnOrder(), e.getRowOrder());

        publish(RefreshEvent.class, event);
    }

    @Override
    public Subscription addCellClickListener(Consumer<CellClickEvent> listener) {
        if (cellClickHandler == null) {
            cellClickHandler = this::onCellClick;
            component.addCellClickListener(cellClickHandler);
        }
        return getEventHub().subscribe(CellClickEvent.class, listener);
    }

    protected void onCellClick(io.jmix.pivottable.widget.events.CellClickEvent e) {
        publish(CellClickEvent.class,
                new CellClickEvent(PivotTableImpl.this, e.getValue(), e.getFilters(), e.getUsedDataItemsRetriever()));
    }

    @Override
    public void setShowUI(Boolean showUI) {
        component.setShowUI(showUI);
    }

    @Override
    public Boolean isShowUI() {
        return component.isShowUI();
    }

    @Override
    public void setShowRowTotals(Boolean showRowTotals) {
        component.setShowRowTotals(showRowTotals);
    }

    @Override
    public Boolean isRowTotalsShown() {
        return component.isShowRowTotals();
    }

    @Override
    public void setShowColTotals(Boolean showColTotals) {
        component.setShowColTotals(showColTotals);
    }

    @Override
    public Boolean isColTotalsShown() {
        return component.isShowColTotals();
    }
}