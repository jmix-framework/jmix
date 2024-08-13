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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.SlotUtils;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.pivottableflowui.component.serialization.PivotTableSerializer;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    protected void initComponent() {
        options = createOptions();
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();

        serializer = createSerializer();
        initOptionsChangeListener();
        initLocalization();

        Div div = new Div();
        div.setId("div-id");

        SlotUtils.addToSlot(this, "output", div);
    }

    private void initLocalization() {
        options.setLocaleCode(currentAuthentication.getLocale().getLanguage());
        options.setLocalizedStrings(getLocalizedStrings());
    }

    protected void autowireDependencies() {
        messages = applicationContext.getBean(Messages.class);
        formatStringsRegistry = applicationContext.getBean(FormatStringsRegistry.class);
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
    }

    @Override
    protected JmixPivotTableSerializer createSerializer() {
        return applicationContext.getBean(PivotTableSerializer.class);
    }

    protected Map<String, Object> getLocalizedStrings() {
        Map<String, Object> localizedStrings = new LinkedHashMap<>();

        // Number formatting
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

        // Other
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

    public  Map<String, String> getAggregationsLocaleMap() {
        Map<String, String> localeMap = new LinkedHashMap<>();

        for (AggregationMode mode : AggregationMode.values()) {
            localeMap.put(mode.getId(), messages.getMessage("pivottable.aggregator." + mode.getId()));
        }

        return localeMap;
    }

    public Map<String, String> getRenderersLocaleMap() {
        Map<String, String> localeMap = new LinkedHashMap<>();

        for (Renderer renderer : Renderer.values()) {
            localeMap.put(renderer.getId(), messages.getMessage("pivottable.renderer." + renderer.getId()));
        }

        return localeMap;
    }

    /*private native void addPivotTableMessages(String localeCode, JavaScriptObject pivotLocalization) /*-{
        var formatFloat, formatInt, formatPercent, numberFormat, aggregatorTemplates;
        numberFormat = $wnd.$.pivotUtilities.numberFormat;
        aggregatorTemplates = $wnd.$.pivotUtilities.aggregatorTemplates;
        formatFloat = numberFormat({
            digitsAfterDecimal: pivotLocalization.floatFormat.digitsAfterDecimal,
            scaler: pivotLocalization.floatFormat.scaler,
            thousandsSep: pivotLocalization.floatFormat.thousandsSep,
            decimalSep: pivotLocalization.floatFormat.decimalSep,
            prefix: pivotLocalization.floatFormat.prefix,
            suffix: pivotLocalization.floatFormat.suffix,
            showZero: pivotLocalization.floatFormat.showZero
        });
        formatInt = numberFormat({
            digitsAfterDecimal: pivotLocalization.integerFormat.digitsAfterDecimal,
            scaler: pivotLocalization.integerFormat.scaler,
            thousandsSep: pivotLocalization.integerFormat.thousandsSep,
            decimalSep: pivotLocalization.integerFormat.decimalSep,
            prefix: pivotLocalization.integerFormat.prefix,
            suffix: pivotLocalization.integerFormat.suffix,
            showZero: pivotLocalization.integerFormat.showZero
        });
        formatPercent = numberFormat({
            digitsAfterDecimal: pivotLocalization.percentFormat.digitsAfterDecimal,
            scaler: pivotLocalization.percentFormat.scaler,
            thousandsSep: pivotLocalization.percentFormat.thousandsSep,
            decimalSep: pivotLocalization.percentFormat.decimalSep,
            prefix: pivotLocalization.percentFormat.prefix,
            suffix: pivotLocalization.percentFormat.suffix,
            showZero: pivotLocalization.percentFormat.showZero
        });

        var allAggregators = {};
        allAggregators[pivotLocalization.aggregation.count] = aggregatorTemplates.count(formatInt);
        allAggregators[pivotLocalization.aggregation.countUniqueValues] = aggregatorTemplates.countUnique(formatInt);
        allAggregators[pivotLocalization.aggregation.listUniqueValues] = aggregatorTemplates.listUnique(", ");
        allAggregators[pivotLocalization.aggregation.sum] = aggregatorTemplates.sum(formatFloat);
        allAggregators[pivotLocalization.aggregation.integerSum] = aggregatorTemplates.sum(formatInt);
        allAggregators[pivotLocalization.aggregation.average] = aggregatorTemplates.average(formatFloat);
        allAggregators[pivotLocalization.aggregation.minimum] = aggregatorTemplates.min(formatFloat);
        allAggregators[pivotLocalization.aggregation.maximum] = aggregatorTemplates.max(formatFloat);
        allAggregators[pivotLocalization.aggregation.sumOverSum] = aggregatorTemplates.sumOverSum(formatFloat);
        allAggregators[pivotLocalization.aggregation.upperBound80] = aggregatorTemplates.sumOverSumBound80(true, formatFloat);
        allAggregators[pivotLocalization.aggregation.lowerBound80] = aggregatorTemplates.sumOverSumBound80(false, formatFloat);
        allAggregators[pivotLocalization.aggregation.sumAsFractionOfTotal] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.sum(), "total", formatPercent);
        allAggregators[pivotLocalization.aggregation.sumAsFractionOfRows] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.sum(), "row", formatPercent);
        allAggregators[pivotLocalization.aggregation.sumAsFractionOfColumns] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.sum(), "col", formatPercent);
        allAggregators[pivotLocalization.aggregation.countAsFractionOfTotal] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.count(), "total", formatPercent);
        allAggregators[pivotLocalization.aggregation.countAsFractionOfRows] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.count(), "row", formatPercent);
        allAggregators[pivotLocalization.aggregation.countAsFractionOfColumns] =
            aggregatorTemplates.fractionOf(aggregatorTemplates.count(), "col", formatPercent);

        var allRenderers = {};
        allRenderers[pivotLocalization.renderer.table] = $wnd.$.pivotUtilities.renderers["Table"];
        allRenderers[pivotLocalization.renderer.tableBarchart] = $wnd.$.pivotUtilities.renderers["Table Barchart"];
        allRenderers[pivotLocalization.renderer.heatmap] = $wnd.$.pivotUtilities.renderers["Heatmap"];
        allRenderers[pivotLocalization.renderer.rowHeatmap] = $wnd.$.pivotUtilities.renderers["Row Heatmap"];
        allRenderers[pivotLocalization.renderer.colHeatmap] = $wnd.$.pivotUtilities.renderers["Col Heatmap"];
        allRenderers[pivotLocalization.renderer.lineChart] = $wnd.$.pivotUtilities.c3_renderers["Line Chart"];
        allRenderers[pivotLocalization.renderer.barChart] = $wnd.$.pivotUtilities.c3_renderers["Bar Chart"];
        allRenderers[pivotLocalization.renderer.stackedBarChart] =
            $wnd.$.pivotUtilities.c3_renderers["Stacked Bar Chart"];
        allRenderers[pivotLocalization.renderer.horizontalBarChart] =
            $wnd.$.pivotUtilities.c3_renderers["Horizontal Bar Chart"];
        allRenderers[pivotLocalization.renderer.horizontalStackedBarChart] =
            $wnd.$.pivotUtilities.c3_renderers["Horizontal Stacked Bar Chart"];
        allRenderers[pivotLocalization.renderer.areaChart] = $wnd.$.pivotUtilities.c3_renderers["Area Chart"];
        allRenderers[pivotLocalization.renderer.scatterChart] = $wnd.$.pivotUtilities.c3_renderers["Scatter Chart"];
        allRenderers[pivotLocalization.renderer.treemap] = $wnd.$.pivotUtilities.d3_renderers["Treemap"];
        allRenderers[pivotLocalization.renderer.TSVExport] = $wnd.$.pivotUtilities.export_renderers["TSV Export"];

        $wnd.$.pivotUtilities.locales[localeCode] = {
            localeStrings: {
                renderError: pivotLocalization.renderError,
                computeError: pivotLocalization.computeError,
                uiRenderError: pivotLocalization.uiRenderError,
                selectAll: pivotLocalization.selectAll,
                selectNone: pivotLocalization.selectNone,
                apply: pivotLocalization.apply,
                cancel: pivotLocalization.cancel,
                tooMany: pivotLocalization.tooMany,
                filterResults: pivotLocalization.filterResults,
                totals: pivotLocalization.totals,
                vs: pivotLocalization.vs,
                by: pivotLocalization.by
            },
            aggregators: allAggregators,
            renderers: allRenderers,
            aggregatorsLocaleMapping: pivotLocalization.aggregation,
            renderersLocaleMapping: pivotLocalization.renderer
        };
    }-*/;
}
