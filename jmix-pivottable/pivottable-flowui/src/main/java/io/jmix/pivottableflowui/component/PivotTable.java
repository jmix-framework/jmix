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
import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Integration of an open-source Javascript Pivot Table written by <a href="https://nicolas.kruchten.com/">Nicolas Kruchten</a>.
 * A pivot table component is a powerful data analysis tool that allows users to summarize, analyze and present
 * datasets in a flexible and interactive manner.
 * @see <a href="https://github.com/nicolaskruchten/pivottable/wiki">Pivot table documentation</a>
 */
public class PivotTable<T> extends JmixPivotTable<T> implements ApplicationContextAware, InitializingBean {

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

    protected void requestPivotData(String dateTimeParseFormat, String dateParseFormat, String timeParseFormat,
                                 Consumer<PivotData> consumer) {
        String jsFunctionParams = dateTimeParseFormat + ", " + dateParseFormat + ", " + timeParseFormat;
        getElement().executeJs("return this._getTableElementData(" + jsFunctionParams + ");")
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

            DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
            typeMessages.put("thousandsSep", Character.toString(formatSymbols.getGroupingSeparator()));
            typeMessages.put("decimalSep", Character.toString(formatSymbols.getDecimalSeparator()));

            localizedStrings.put(type, typeMessages);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        localizedStrings.put("percentFormat.suffix", Character.toString(formatSymbols.getPercent()));

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