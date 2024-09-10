/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.component;

import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.pivottableflowui.export.model.PivotData;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.model.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Integration of an open-source Javascript Pivot Table written by <a href="https://nicolas.kruchten.com/">Nicolas Kruchten</a>.
 * A pivot table component is a powerful data analysis tool that allows users to summarize, analyze and present
 * datasets in a flexible and interactive manner.
 * @see <a href="https://github.com/nicolaskruchten/pivottable/wiki">Pivot table documentation</a>
 */
public class PivotTable extends JmixPivotTable implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected FormatStringsRegistry formatStringsRegistry;
    protected CurrentAuthentication currentAuthentication;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();

        initLocalization();
    }

    public PivotTable withPropertiesOnly(Map<String, String> properties) {
        setProperties(properties);
        return this;
    }

    public PivotTable withProperties(Map<String, String> properties) {
        options.addProperties(properties);
        return this;
    }

    public PivotTable withProperty(String property, String value) {
        options.addProperty(property, value);
        return this;
    }

    public PivotTable withRows(List<String> rows) {
        options.setRows(rows);
        return this;
    }

    public PivotTable withRows(String... rows) {
        options.addRows(rows);
        return this;
    }

    public PivotTable withCols(List<String> cols) {
        options.setCols(cols);
        return this;
    }

    public PivotTable withCols(String... cols) {
        options.addCols(cols);
        return this;
    }

    public PivotTable withAggregation(Aggregation aggregation) {
        options.setAggregation(aggregation);
        return this;
    }

    public PivotTable withRenderer(Renderer renderer) {
        options.setRenderer(renderer);
        return this;
    }

    public PivotTable withAggregationProperties(List<String> aggregationProperties) {
        options.setAggregationProperties(aggregationProperties);
        return this;
    }

    public PivotTable withAggregationProperties(String... aggregationProperties) {
        options.addAggregationProperties(aggregationProperties);
        return this;
    }

    public PivotTable withAggregations(Aggregations aggregations) {
        options.setAggregations(aggregations);
        return this;
    }

    public PivotTable withRenderers(Renderers renderers) {
        options.setRenderers(renderers);
        return this;
    }

    public PivotTable withHiddenProperties(List<String> hiddenProperties) {
        options.setHiddenProperties(hiddenProperties);
        return this;
    }

    public PivotTable withHiddenProperties(String... hiddenProperties) {
        options.addHiddenProperties(hiddenProperties);
        return this;
    }

    public PivotTable withHiddenFromAggregations(List<String> hiddenFromAggregations) {
        options.setHiddenFromAggregations(hiddenFromAggregations);
        return this;
    }

    public PivotTable withHiddenFromAggregations(String... hiddenFromAggregations) {
        options.addHiddenFromAggregations(hiddenFromAggregations);
        return this;
    }

    public PivotTable withHiddenFromDragDrop(List<String> hiddenFromDragDrop) {
        options.setHiddenFromDragDrop(hiddenFromDragDrop);
        return this;
    }

    public PivotTable withHiddenFromDragDrop(String... hiddenFromDragDrop) {
        options.addHiddenFromDragDrop(hiddenFromDragDrop);
        return this;
    }

    public PivotTable withColOrder(Order colOrder) {
        options.setColOrder(colOrder);
        return this;
    }

    public PivotTable withRowOrder(Order rowOrder) {
        options.setRowOrder(rowOrder);
        return this;
    }

    public PivotTable withMenuLimit(Integer menuLimit) {
        setMenuLimit(menuLimit);
        return this;
    }

    public PivotTable withAutoSortUnusedProperties(Boolean autoSortUnusedProperties) {
        setAutoSortUnusedProperties(autoSortUnusedProperties);
        return this;
    }

    public PivotTable withUnusedPropertiesVertical(UnusedPropertiesVertical unusedPropertiesVertical) {
        setUnusedPropertiesVertical(unusedPropertiesVertical);
        return this;
    }

    public PivotTable withFilterFunction(JsFunction filter) {
        options.setFilterFunction(filter);
        return this;
    }

    public PivotTable withSortersFunction(JsFunction sorters) {
        options.setSortersFunction(sorters);
        return this;
    }

    public PivotTable withRendererOptions(RendererOptions rendererOptions) {
        setRendererOptions(rendererOptions);
        return this;
    }

    public PivotTable withInclusions(Map<String, List<String>> inclusions) {
        setInclusions(inclusions);
        return this;
    }

    public PivotTable withInclusions(String property, List<String> inclusions) {
        setInclusions(property, inclusions);
        return this;
    }

    public PivotTable withInclusions(String property, String... inclusions) {
        addInclusions(property, inclusions);
        return this;
    }

    public PivotTable withExclusions(Map<String, List<String>> exclusions) {
        setExclusions(exclusions);
        return this;
    }

    public PivotTable withExclusions(String property, List<String> exclusions) {
        options.setExclusions(property, exclusions);
        return this;
    }

    public PivotTable withExclusions(String property, String... exclusions) {
        addExclusions(property, exclusions);
        return this;
    }

    public PivotTable withDerivedProperties(DerivedProperties derivedProperties) {
        setDerivedProperties(derivedProperties);
        return this;
    }

    public PivotTable withEmptyDataMessage(String emptyDataMessage) {
        options.setEmptyDataMessage(emptyDataMessage);
        return this;
    }

    public PivotTable withShowUI(Boolean showUI) {
        options.setShowUI(showUI);
        return this;
    }

    public PivotTable withShowRowTotals(Boolean showRowTotals) {
        setShowRowTotals(showRowTotals);
        return this;
    }

    public PivotTable withShowColTotals(Boolean showColTotals) {
        setShowColTotals(showColTotals);
        return this;
    }

    public PivotTable withNativeJson(String nativeJson) {
        setNativeJson(nativeJson);
        return this;
    }

    public void getPivotData(Consumer<PivotData> consumer) {
        getElement().executeJs("return this._getTableElementData();")
                .then((SerializableConsumer<JsonValue>) jsonValue -> {
                    PivotData pivotData = (PivotData) serializer.deserialize((JsonObject) jsonValue, PivotData.class);
                    consumer.accept(pivotData);
                });
    }

    protected void initLocalization() {
        options.setLocaleCode(currentAuthentication.getLocale().getLanguage());
        options.setLocalizedStrings(getLocalizedStrings());
    }

    protected void autowireDependencies() {
        messages = applicationContext.getBean(Messages.class);
        formatStringsRegistry = applicationContext.getBean(FormatStringsRegistry.class);
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
    }

    protected Map<String, Object> getLocalizedStrings() {
        Map<String, Object> localizedStrings = new LinkedHashMap<>();

        String[] formatTypes = {"floatFormat", "integerFormat", "percentFormat"};
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());

        for (String type : formatTypes) {
            Map<String, String> typeMessages = new HashMap<>();
            typeMessages.put("digitsAfterDecimal",
                    messages.getMessage("pivottable." + type + ".digitsAfterDecimal"));
            typeMessages.put("scaler", messages.getMessage("pivottable." + type + ".scaler"));
            typeMessages.put("prefix", messages.getMessage("pivottable." + type + ".prefix"));
            typeMessages.put("suffix", messages.getMessage("pivottable." + type + ".suffix"));
            typeMessages.put("showZero", messages.getMessage("pivottable." + type + ".showZero"));

            if (formatStrings != null) {
                DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();

                typeMessages.put("thousandsSep", Character.toString(formatSymbols.getGroupingSeparator()));
                typeMessages.put("decimalSep", Character.toString(formatSymbols.getDecimalSeparator()));
            } else {
                typeMessages.put("thousandsSep",
                        messages.getMessage("pivottable." + type + ".thousandsSep"));
                typeMessages.put("decimalSep",
                        messages.getMessage("pivottable." + type + ".decimalSep"));
            }

            localizedStrings.put(type, typeMessages);
        }

        if (formatStrings != null) {
            DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
            localizedStrings.put("percentFormat.suffix", Character.toString(formatSymbols.getPercent()));
        }

        localizedStrings.put("renderError", messages.getMessage("pivottable.renderError"));
        localizedStrings.put("computeError", messages.getMessage("pivottable.computeError"));
        localizedStrings.put("uiRenderError", messages.getMessage("pivottable.uiRenderError"));
        localizedStrings.put("selectAll", messages.getMessage("pivottable.selectAll"));
        localizedStrings.put("selectNone", messages.getMessage("pivottable.selectNone"));
        localizedStrings.put("apply", messages.getMessage("pivottable.apply"));
        localizedStrings.put("cancel", messages.getMessage("pivottable.cancel"));
        localizedStrings.put("tooMany", messages.getMessage("pivottable.tooMany"));
        localizedStrings.put("filterResults", messages.getMessage("pivottable.filterResults"));
        localizedStrings.put("totals", messages.getMessage("pivottable.totals"));
        localizedStrings.put("vs", messages.getMessage("pivottable.vs"));
        localizedStrings.put("by", messages.getMessage("pivottable.by"));

        localizedStrings.put("aggregation", getAggregationsLocaleMap());
        localizedStrings.put("renderer", getRenderersLocaleMap());

        return localizedStrings;
    }

    protected Map<String, String> getAggregationsLocaleMap() {
        Map<String, String> localeMap = new LinkedHashMap<>();

        for (AggregationMode mode : AggregationMode.values()) {
            localeMap.put(mode.getId(), messages.getMessage("pivottable.aggregator." + mode.getId()));
        }

        return localeMap;
    }

    protected Map<String, String> getRenderersLocaleMap() {
        Map<String, String> localeMap = new LinkedHashMap<>();

        for (Renderer renderer : Renderer.values()) {
            localeMap.put(renderer.getId(), messages.getMessage("pivottable.renderer." + renderer.getId()));
        }

        return localeMap;
    }
}