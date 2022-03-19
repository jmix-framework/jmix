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

package io.jmix.pivottable.widget;

import com.google.gson.*;
import com.vaadin.server.KeyMapper;
import com.vaadin.ui.AbstractComponent;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.widget.WebJarResource;
import io.jmix.pivottable.model.*;
import io.jmix.pivottable.widget.client.JmixPivotTableSceneState;
import io.jmix.pivottable.widget.client.JmixPivotTableServerRpc;
import io.jmix.pivottable.widget.events.CellClickEvent;
import io.jmix.pivottable.widget.events.CellClickListener;
import io.jmix.pivottable.widget.events.RefreshEvent;
import io.jmix.pivottable.widget.events.RefreshListener;
import io.jmix.pivottable.widget.serialization.PivotTableSerializationContext;
import io.jmix.pivottable.widget.serialization.PivotTableSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.vaadin.util.ReflectTools.findMethod;

@WebJarResource({
        "jquery:jquery.min.js",
        "jquery-ui:jquery-ui.min.js",
        "jquery-ui-touch-punch:jquery.ui.touch-punch.min.js",
        "pivottable:pivot.min.js",
        "pivottable:tips_data.min.js",
        "pivottable:plugins/d3/d3.min.js",
        "pivottable:plugins/c3/c3.min.js",
        "pivottable:c3_renderers.min.js",
        "pivottable:d3_renderers.min.js",
        "pivottable:export_renderers.min.js",
        "pivottable:pivot.min.css",
        "pivottable:plugins/c3/c3.min.css"
})
public class JmixPivotTable extends AbstractComponent {
    private static final long serialVersionUID = 3250758720037122580L;

    protected static final String DATA_ITEM_KEY = "$k";

    private static final Logger log = LoggerFactory.getLogger(JmixPivotTable.class);

    protected static final Method refreshMethod =
            findMethod(RefreshListener.class, "onRefresh", RefreshEvent.class);

    protected static final Method cellClickMethod =
            findMethod(CellClickListener.class, "onCellClick", CellClickEvent.class);

    protected boolean dirty = false;

    protected PivotTableModel pivotTable;
    protected PivotTableSerializer pivotTableSerializer;

    protected String locale;

    protected KeyMapper<DataItem> dataItemMapper;

    public JmixPivotTable(PivotTableSerializer pivotTableSerializer) {
        pivotTable = new PivotTableModel();

        this.pivotTableSerializer = pivotTableSerializer;

        registerRpc(new JmixPivotTableServerRpcImpl(), JmixPivotTableServerRpc.class);

        addRefreshListener(createRefreshListener());
    }

    public JmixPivotTable() {
        pivotTable = new PivotTableModel();

        registerRpc(new JmixPivotTableServerRpcImpl(), JmixPivotTableServerRpc.class);

        addRefreshListener(createRefreshListener());
    }

    public void setPivotTableSerializer(PivotTableSerializer pivotTableSerializer) {
        this.pivotTableSerializer = pivotTableSerializer;
    }

    protected RefreshListener createRefreshListener() {
        return event -> {
            pivotTable.setRows(event.getRows());
            pivotTable.setCols(event.getCols());

            if (pivotTable.getRenderers() == null) {
                pivotTable.setRenderers(new Renderers());
            }
            pivotTable.getRenderers().setSelectedRenderer(event.getRenderer());

            pivotTable.setInclusions(event.getInclusions());
            pivotTable.setExclusions(event.getExclusions());

            pivotTable.setColumnOrder(event.getColumnOrder());
            pivotTable.setRowOrder(event.getRowOrder());

            Aggregation aggregation = event.getAggregation();
            boolean hasMode = aggregation != null && !Boolean.TRUE.equals(aggregation.getCustom());
            if (pivotTable.getAggregations() == null) {
                pivotTable.setAggregations(new Aggregations());
            }
            pivotTable.getAggregations().setSelectedAggregation(hasMode ? aggregation.getMode() : null);
            if (aggregation != null && Boolean.TRUE.equals(aggregation.getCustom())) {
                // Due to impossibility to set a custom aggregation as the default
                // we need to set a custom aggregation as the first one and clear
                // the default aggregation value, so it will be set as the default by pivot.js
                pivotTable.getAggregations().getAggregations().remove(aggregation);
                pivotTable.getAggregations().getAggregations().add(0, aggregation);
            }

            pivotTable.setAggregationProperties(event.getAggregationProperties());
        };
    }

    @Override
    protected JmixPivotTableSceneState getState() {
        return (JmixPivotTableSceneState) super.getState();
    }

    @Override
    protected JmixPivotTableSceneState getState(boolean markAsDirty) {
        return (JmixPivotTableSceneState) super.getState(markAsDirty);
    }

    public PivotTableModel getPivotTable() {
        return pivotTable;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setJson(String json) {
        if (!StringUtils.equals(getJson(), json)) {
            if (json != null) {
                try {
                    JsonParser parser = new JsonParser();
                    parser.parse(json);
                } catch (JsonSyntaxException e) {
                    throw new IllegalStateException("Unable to parse JSON chart configuration");
                }
            }

            getState().json = json;
            forceStateChange();
        }
    }

    public String getJson() {
        return getState(false).json;
    }

    public void repaint() {
        forceStateChange();
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (initial || isDirty()) {
            if (pivotTable != null) {
                // Full repaint

                String dataJsonString;

                if (isCellClickListenerPresent()) {
                    dataItemMapper = new KeyMapper<>();
                    dataJsonString = pivotTableSerializer.serializeData(pivotTable, this::serializeDataItemKey);
                } else {
                    dataJsonString = pivotTableSerializer.serializeData(pivotTable);
                }

                log.trace("pivotTable data JSON:\n{}", dataJsonString);
                getState().data = dataJsonString;

                String optionsJsonString;
                if (isCellClickListenerPresent()) {
                    optionsJsonString = pivotTableSerializer.serialize(pivotTable, this::afterPivotModelSerialized);
                } else {
                    optionsJsonString = pivotTableSerializer.serialize(pivotTable);
                }

                log.trace("pivotTable options JSON:\n{}", optionsJsonString);
                getState().options = optionsJsonString;
            }

            dirty = false;
        }
    }

    protected boolean isCellClickListenerPresent() {
        return hasListeners(CellClickEvent.class);
    }

    protected void serializeDataItemKey(PivotTableSerializationContext context) {
        JsonObject jsonObject = context.getJsonObject();
        String dataItemKey = dataItemMapper.key(context.getDataItem());
        JsonElement serializedKey = context.getSerializationContext().serialize(dataItemKey);
        jsonObject.add(DATA_ITEM_KEY, serializedKey);
    }

    protected void afterPivotModelSerialized(PivotTableSerializationContext context) {
        JsonObject jsonObject = context.getJsonObject();
        JsonArray hiddenProperties = jsonObject.getAsJsonArray("hiddenProperties");
        if (hiddenProperties == null) {
            hiddenProperties = new JsonArray();
        }
        hiddenProperties.add(DATA_ITEM_KEY);
        jsonObject.add("hiddenProperties", hiddenProperties);
    }

    protected void forceStateChange() {
        this.dirty = true;
        getState(true);
    }

    public void addRefreshListener(RefreshListener listener) {
        addListener(JmixPivotTableSceneState.REFRESH_EVENT, RefreshEvent.class, listener, refreshMethod);
    }

    public void addCellClickListener(CellClickListener listener) {
        addListener(JmixPivotTableSceneState.CELL_CLICK_EVENT, CellClickEvent.class, listener, cellClickMethod);
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Use 'getLocaleString' instead");
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("Use 'setLocaleString' instead");
    }

    public String getLocaleString() {
        return locale;
    }

    public void setLocaleString(String locale) {
        this.locale = locale;
        pivotTable.setLocaleCode(locale);
    }

    protected JsonObject convertMapToJsonObject(Map<String, String> localeMap) {
        JsonObject jsonLocaleMap = new JsonObject();
        for (Map.Entry<String, String> localeEntry : localeMap.entrySet()) {
            jsonLocaleMap.addProperty(localeEntry.getKey(), localeEntry.getValue());
        }

        return jsonLocaleMap;
    }

    public void setPivotTableMessages(String localeCode, Map<String, Object> localeMap) {
        if (getState(false).localeMap == null) {
            getState().localeMap = new HashMap<>();
        }

        JsonObject jsonLocaleMap = new JsonObject();
        for (Map.Entry<String, Object> localeEntry : localeMap.entrySet()) {
            JsonElement element;
            if (localeEntry.getValue() instanceof Map) {
                //noinspection unchecked
                element = convertMapToJsonObject((Map<String, String>) localeEntry.getValue());
            } else {
                element = new JsonPrimitive((String) localeEntry.getValue());
            }
            jsonLocaleMap.add(localeEntry.getKey(), element);
        }

        getState().localeMap.put(localeCode, pivotTableSerializer.toJson(localeMap));
    }

    public String getEmptyDataMessage() {
        return getState(false).emptyDataMessage;
    }

    public void setEmptyDataMessage(String emptyDataMessage) {
        getState().emptyDataMessage = emptyDataMessage;
    }

    public void setShowUI(Boolean showUI) {
        pivotTable.setShowUI(showUI);
    }

    public Boolean isShowUI() {
        return pivotTable.isShowUI();
    }

    public void setShowRowTotals(Boolean rowTotals) {
        pivotTable.setShowRowTotals(rowTotals);
    }

    public Boolean isShowRowTotals() {
        return pivotTable.isShowRowTotals();
    }

    public void setShowColTotals(Boolean colTotals) {
        pivotTable.setShowColTotals(colTotals);
    }

    public Boolean isShowColTotals() {
        return pivotTable.isShowColTotals();
    }

    protected class JmixPivotTableServerRpcImpl implements JmixPivotTableServerRpc {

        private static final long serialVersionUID = 4789102026045383363L;

        @Override
        public void onCellClick(Double value, Map<String, String> filters, List<String> dataItemKeys) {
            Supplier<List<DataItem>> usedDataItemsRetriever = () ->
                    dataItemKeys.stream()
                            .map(s -> dataItemMapper.get(s))
                            .collect(Collectors.toList());
            fireEvent(new CellClickEvent(JmixPivotTable.this, value, filters, usedDataItemsRetriever));
        }

        @Override
        public void onRefresh(List<String> rows, List<String> cols, String rendererId,
                              String aggregationId, List<String> aggregationProperties,
                              Map<String, List<String>> inclusions, Map<String, List<String>> exclusions,
                              String colOrderId, String rowOrderId) {
            if (!pivotTable.getEditable()) {
                return;
            }

            Renderer renderer = Renderer.fromId(rendererId);
            Aggregation aggregation = findAggregation(aggregationId);

            ColumnOrder columnOrder = ColumnOrder.fromId(colOrderId);
            RowOrder rowOrder = RowOrder.fromId(rowOrderId);

            fireEvent(new RefreshEvent(JmixPivotTable.this,
                    rows, cols, renderer,
                    aggregation, aggregationProperties,
                    inclusions, exclusions, columnOrder, rowOrder));
        }

        @Nullable
        private Aggregation findAggregation(String aggregationId) {
            if (pivotTable.getAggregations() != null
                    && pivotTable.getAggregations().getAggregations() != null) {
                for (Aggregation aggregation : pivotTable.getAggregations().getAggregations()) {
                    if (aggregation.getId().equals(aggregationId)) {
                        return aggregation;
                    }
                }
            } else {
                AggregationMode mode = AggregationMode.fromId(aggregationId);
                return new Aggregation().setMode(mode);
            }
            return null;
        }
    }
}
