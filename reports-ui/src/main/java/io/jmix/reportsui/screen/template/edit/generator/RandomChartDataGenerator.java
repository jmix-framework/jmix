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

package io.jmix.reportsui.screen.template.edit.generator;

import io.jmix.core.Messages;
import io.jmix.reports.entity.charts.*;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

@Component("report_RandomChartDataGenerator")
public class RandomChartDataGenerator {
    public static final List<String> COLORS = Collections.unmodifiableList(Arrays.asList("red", "green", "blue", "yellow", "orange", "black", "magenta"));

    @Autowired
    protected Messages messages;

    @Nullable
    public List<Map<String, Object>> generateRandomChartData(@Nullable AbstractChartDescription abstractChartDescription) {
        List<Map<String, Object>> data = null;
        if (abstractChartDescription == null) {
            return null;
        }

        if (ChartType.SERIAL == abstractChartDescription.getType()) {
            SerialChartDescription chartDescription = (SerialChartDescription) abstractChartDescription;
            String categoryField = chartDescription.getCategoryField();

            data = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                HashMap<String, Object> map = new HashMap<>();
                data.add(map);

                map.put(categoryField, messages.getMessage(getClass(), "caption.category") + i);

                for (ChartSeries chartSeries : chartDescription.getSeries()) {
                    String valueField = chartSeries.getValueField();
                    String colorField = chartSeries.getColorField();
                    map.put(valueField, Math.abs(RandomUtils.nextInt(0, 100)));
                    map.put(colorField, COLORS.get(RandomUtils.nextInt(0, 6)));
                }
            }
        } else if (ChartType.PIE == abstractChartDescription.getType()) {
            PieChartDescription chartDescription = (PieChartDescription) abstractChartDescription;
            String titleField = chartDescription.getTitleField();
            String valueField = chartDescription.getValueField();
            String colorField = chartDescription.getColorField();

            data = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                HashMap<String, Object> map = new HashMap<>();
                data.add(map);

                map.put(titleField, messages.getMessage(getClass(), "caption.category") + i);
                map.put(valueField, Math.abs(RandomUtils.nextInt(0, 100)));
                map.put(colorField, COLORS.get(RandomUtils.nextInt(0, 6)));
            }
        }

        return data;
    }
}
