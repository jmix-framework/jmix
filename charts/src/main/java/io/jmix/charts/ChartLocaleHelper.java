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

package io.jmix.charts;


import io.jmix.core.Messages;
import io.jmix.charts.model.date.DayOfWeek;
import io.jmix.charts.model.date.Month;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("ui_ChartLocaleHelper")
public final class ChartLocaleHelper {

    @Autowired
    protected Messages messages;

    public Map<String, Object> getChartLocaleMap(Locale locale) {

        Map<String, Object> chartLocaleMap = new LinkedHashMap<>();

        // day of week
        List<String> dayNames = new LinkedList<>();
        List<String> shortDayNames = new LinkedList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dayNames.add(messages.getMessage("amcharts.dayNames." + day.name(), locale));
            shortDayNames.add(messages.getMessage("amcharts.shortDayNames." + day.name(), locale));
        }
        chartLocaleMap.put("dayNames", dayNames);
        chartLocaleMap.put("shortDayNames", shortDayNames);

        // months
        List<String> monthNames = new LinkedList<>();
        List<String> shortMonthNames = new LinkedList<>();
        for (Month m : Month.values()) {
            monthNames.add(messages.getMessage("amcharts.monthNames." + m.name(), locale));
            shortMonthNames.add(messages.getMessage("amcharts.shortMonthNames." + m.name(), locale));
        }
        chartLocaleMap.put("monthNames", monthNames);
        chartLocaleMap.put("shortMonthNames", shortMonthNames);

        // formatting time
        chartLocaleMap.put("am", messages.getMessage("amcharts.am", locale));
        chartLocaleMap.put("pm", messages.getMessage("amcharts.pm", locale));

        return chartLocaleMap;
    }

    public Map<String, String> getExportLocaleMap(Locale locale) {

        Map<String, String> exportLocaleMap = new LinkedHashMap<>();

        exportLocaleMap.put("fallback.save.text", messages.getMessage("fallback.save.text", locale));
        exportLocaleMap.put("fallback.save.image", messages.getMessage("fallback.save.image", locale));

        exportLocaleMap.put("capturing.delayed.menu.label", messages.getMessage("capturing.delayed.menu.label", locale));
        exportLocaleMap.put("capturing.delayed.menu.title", messages.getMessage("capturing.delayed.menu.title", locale));

        exportLocaleMap.put("menu.label.print", messages.getMessage("menu.label.print", locale));
        exportLocaleMap.put("menu.label.undo", messages.getMessage("menu.label.undo", locale));
        exportLocaleMap.put("menu.label.redo", messages.getMessage("menu.label.redo", locale));
        exportLocaleMap.put("menu.label.cancel", messages.getMessage("menu.label.cancel", locale));

        exportLocaleMap.put("menu.label.save.image", messages.getMessage("menu.label.save.image", locale));
        exportLocaleMap.put("menu.label.save.data", messages.getMessage("menu.label.save.data", locale));

        exportLocaleMap.put("menu.label.draw", messages.getMessage("menu.label.draw", locale));
        exportLocaleMap.put("menu.label.draw.change", messages.getMessage("menu.label.draw.change", locale));
        exportLocaleMap.put("menu.label.draw.add", messages.getMessage("menu.label.draw.add", locale));
        exportLocaleMap.put("menu.label.draw.shapes", messages.getMessage("menu.label.draw.shapes", locale));
        exportLocaleMap.put("menu.label.draw.colors", messages.getMessage("menu.label.draw.colors", locale));
        exportLocaleMap.put("menu.label.draw.widths", messages.getMessage("menu.label.draw.widths", locale));
        exportLocaleMap.put("menu.label.draw.opacities", messages.getMessage("menu.label.draw.opacities", locale));
        exportLocaleMap.put("menu.label.draw.text", messages.getMessage("menu.label.draw.text", locale));

        exportLocaleMap.put("menu.label.draw.modes", messages.getMessage("menu.label.draw.modes", locale));
        exportLocaleMap.put("menu.label.draw.modes.pencil", messages.getMessage("menu.label.draw.modes.pencil", locale));
        exportLocaleMap.put("menu.label.draw.modes.line", messages.getMessage("menu.label.draw.modes.line", locale));
        exportLocaleMap.put("menu.label.draw.modes.arrow", messages.getMessage("menu.label.draw.modes.arrow", locale));
        exportLocaleMap.put("label.saved.from", messages.getMessage("label.saved.from", locale));

        return exportLocaleMap;
    }
}
