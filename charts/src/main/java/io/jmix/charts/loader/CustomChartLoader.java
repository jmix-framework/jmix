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

package io.jmix.charts.loader;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.charts.component.CustomChart;
import org.dom4j.Element;

public class CustomChartLoader extends AbstractComponentLoader<CustomChart> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(CustomChart.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);
        loadStyleName(resultComponent, element);
        loadAlign(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadCss(resultComponent, element);

        loadNativeJson(resultComponent, element);
    }

    protected void loadNativeJson(CustomChart customChart, Element element) {
        Element nativeJson = element.element("nativeJson");
        if (nativeJson != null) {
            String nativeJsonString = nativeJson.getTextTrim();
            try {
                JsonParser parser = new JsonParser();
                parser.parse(nativeJsonString);
            } catch (JsonSyntaxException e) {
                throw new GuiDevelopmentException("Unable to parse JSON from XML chart configuration", context);
            }

            customChart.setNativeJson(nativeJsonString);
        }
    }
}