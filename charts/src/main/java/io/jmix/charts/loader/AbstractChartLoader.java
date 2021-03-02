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
import io.jmix.ui.data.impl.ContainerDataProvider;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.charts.component.Chart;
import io.jmix.charts.model.settings.Responsive;
import io.jmix.charts.model.settings.Rule;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class AbstractChartLoader<T extends Chart> extends ChartModelLoader<T> {

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

        loadDataContainer(resultComponent, element);
    }

    protected void loadDataContainer(Chart chart, Element element) {
        String dataContainerId = element.attributeValue("dataContainer");

        if (StringUtils.isNotEmpty(dataContainerId)) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);

            CollectionContainer dataContainer;

            InstanceContainer container = screenData.getContainer(dataContainerId);
            if (container instanceof CollectionContainer) {
                dataContainer = (CollectionContainer) container;
            } else {
                throw new GuiDevelopmentException("Not a CollectionContainer: " + dataContainerId, context);
            }

            chart.setDataProvider(new ContainerDataProvider(dataContainer));
        }
    }

    protected void loadConfiguration(T chart, Element element) {
        loadBaseProperties(chart, element);

        Element responsiveElement = element.element("responsive");
        if (responsiveElement != null) {
            Responsive responsive = new Responsive();
            loadResponsive(responsive, responsiveElement);
            chart.setResponsive(responsive);
        }

        Element nativeJson = element.element("nativeJson");
        if (nativeJson != null) {
            String nativeJsonString = nativeJson.getTextTrim();
            try {
                JsonParser parser = new JsonParser();
                parser.parse(nativeJsonString);
            } catch (JsonSyntaxException e) {
                throw new GuiDevelopmentException("Unable to parse JSON from XML chart configuration", context);
            }

            resultComponent.setNativeJson(nativeJsonString);
        }
    }

    protected void loadResponsive(Responsive responsive, Element responsiveElement) {
        loadRules(responsive, responsiveElement);

        String responsiveValue = responsiveElement.attributeValue("enabled");
        if (StringUtils.isNotEmpty(responsiveValue)) {
            responsive.setEnabled(Boolean.parseBoolean(responsiveValue));
        }
    }

    protected void loadRules(Responsive responsive, Element responsiveElement) {
        Element rulesElement = responsiveElement.element("rules");
        if (rulesElement != null) {
            for (Element ruleElement : rulesElement.elements("rule")) {
                Rule rule = new Rule();

                String maxHeight = ruleElement.attributeValue("maxHeight");
                if (StringUtils.isNotEmpty(maxHeight)) {
                    rule.setMaxHeight(Integer.parseInt(maxHeight));
                }

                String minHeight = ruleElement.attributeValue("minHeight");
                if (StringUtils.isNotEmpty(minHeight)) {
                    rule.setMinHeight(Integer.parseInt(minHeight));
                }

                String maxWidth = ruleElement.attributeValue("maxWidth");
                if (StringUtils.isNotEmpty(maxWidth)) {
                    rule.setMaxWidth(Integer.parseInt(maxWidth));
                }

                String minWidth = ruleElement.attributeValue("minWidth");
                if (StringUtils.isNotEmpty(minWidth)) {
                    rule.setMinWidth(Integer.parseInt(minWidth));
                }

                rule.setRawOverridesJson(ruleElement.getTextTrim());
                responsive.addRule(rule);
            }
        }
    }
}