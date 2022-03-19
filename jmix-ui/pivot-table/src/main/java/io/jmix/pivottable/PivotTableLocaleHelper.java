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

package io.jmix.pivottable;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.pivottable.model.AggregationMode;
import io.jmix.pivottable.model.Renderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component("ui_PivotTableLocaleHelper")
public final class PivotTableLocaleHelper {

    @Autowired
    protected Messages messages;

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;


    public Map<String, Object> getPivotTableLocaleMap(Locale locale) {
        Map<String, Object> localeMap = new LinkedHashMap<>();

        // Number formatting
        String[] formatTypes = {"floatFormat", "integerFormat", "percentFormat"};
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(locale);

        for (String type : formatTypes) {
            Map<String, String> typeMessages = new HashMap<>();
            typeMessages.put("digitsAfterDecimal",
                    messages.getMessage("pivottable." + type + ".digitsAfterDecimal", locale));
            typeMessages.put("scaler", messages.getMessage("pivottable." + type + ".scaler", locale));
            typeMessages.put("prefix", messages.getMessage("pivottable." + type + ".prefix", locale));
            typeMessages.put("suffix", messages.getMessage("pivottable." + type + ".suffix", locale));
            typeMessages.put("showZero", messages.getMessage("pivottable." + type + ".showZero", locale));

            if (formatStrings != null) {
                DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();

                typeMessages.put("thousandsSep", Character.toString(formatSymbols.getGroupingSeparator()));
                typeMessages.put("decimalSep", Character.toString(formatSymbols.getDecimalSeparator()));
            } else {
                typeMessages.put("thousandsSep",
                        messages.getMessage("pivottable." + type + ".thousandsSep", locale));
                typeMessages.put("decimalSep",
                        messages.getMessage("pivottable." + type + ".decimalSep", locale));
            }

            localeMap.put(type, typeMessages);
        }

        if (formatStrings != null) {
            DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
            localeMap.put("percentFormat.suffix", Character.toString(formatSymbols.getPercent()));
        }

        // Other
        localeMap.put("renderError", messages.getMessage("pivottable.renderError", locale));
        localeMap.put("computeError", messages.getMessage("pivottable.computeError", locale));
        localeMap.put("uiRenderError", messages.getMessage("pivottable.uiRenderError", locale));
        localeMap.put("selectAll", messages.getMessage("pivottable.selectAll", locale));
        localeMap.put("selectNone", messages.getMessage("pivottable.selectNone", locale));
        localeMap.put("apply", messages.getMessage("pivottable.apply", locale));
        localeMap.put("cancel", messages.getMessage("pivottable.cancel", locale));
        localeMap.put("tooMany", messages.getMessage("pivottable.tooMany", locale));
        localeMap.put("filterResults", messages.getMessage("pivottable.filterResults", locale));
        localeMap.put("totals", messages.getMessage("pivottable.totals", locale));
        localeMap.put("vs", messages.getMessage("pivottable.vs", locale));
        localeMap.put("by", messages.getMessage("pivottable.by", locale));

        localeMap.put("aggregation", getAggregationsLocaleMap(locale));
        localeMap.put("renderer", getRenderersLocaleMap(locale));

        return localeMap;
    }

    public  Map<String, String> getAggregationsLocaleMap(Locale locale) {
        Map<String, String> localeMap = new LinkedHashMap<>();

        for (AggregationMode mode : AggregationMode.values()) {
            localeMap.put(mode.getId(), messages.getMessage("pivottable.aggregator." + mode.getId(), locale));
        }

        return localeMap;
    }

    public Map<String, String> getRenderersLocaleMap(Locale locale) {
        Map<String, String> localeMap = new LinkedHashMap<>();

        for (Renderer renderer : Renderer.values()) {
            localeMap.put(renderer.getId(), messages.getMessage("pivottable.renderer." + renderer.getId(), locale));
        }

        return localeMap;
    }
}
