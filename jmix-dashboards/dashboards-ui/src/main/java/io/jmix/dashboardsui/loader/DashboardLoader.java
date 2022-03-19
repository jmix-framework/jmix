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

package io.jmix.dashboardsui.loader;

import io.jmix.core.Metadata;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.dashboards.model.parameter.type.*;
import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.component.impl.DashboardImpl;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DashboardLoader extends AbstractComponentLoader<DashboardImpl> {

    protected Metadata metadata;

    @Override
    public void createComponent() {
        resultComponent = factory.create(Dashboard.NAME);
        loadId(resultComponent, element);

        metadata = applicationContext.getBean(Metadata.class);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        assignXmlDescriptor(resultComponent, element);
        loadVisible(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadResponsive(resultComponent, element);

        loadAlign(resultComponent, element);

        loadHeight(resultComponent, element, ComponentsHelper.getComponentHeight(resultComponent));
        loadWidth(resultComponent, element, ComponentsHelper.getComponentWidth(resultComponent));

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        loadReferenceName(resultComponent, element);
        loadJsonPath(resultComponent, element);
        loadParams(resultComponent, element);
        loadTimerDelay(resultComponent, element);
        loadAssistanceBeanName(resultComponent, element);

        resultComponent.init(Collections.emptyMap());
    }

    protected void loadReferenceName(Dashboard resultComponent, Element element) {
        String referenceName = element.attributeValue("code");
        if (isNotBlank(referenceName)) {
            resultComponent.setCode(referenceName);
        }
    }

    protected void loadJsonPath(Dashboard resultComponent, Element element) {
        String jsonPath = element.attributeValue("jsonPath");
        if (isNotBlank(jsonPath)) {
            resultComponent.setJsonPath(jsonPath);
        }
    }

    protected void loadTimerDelay(Dashboard resultComponent, Element element) {
        String timerDelayValue = element.attributeValue("timerDelay");
        if (isNotBlank(timerDelayValue)) {
            resultComponent.setTimerDelay(Integer.parseInt(timerDelayValue));
        }
    }

    protected void loadParams(Dashboard resultComponent, Element element) {
        List<Parameter> parameters = element.content().stream()
                .filter(child -> child instanceof DefaultElement &&
                        "parameter".equals(child.getName()))
                .map(xmlParam -> createParameter((DefaultElement) xmlParam))
                .collect(Collectors.toList());

        resultComponent.setXmlParameters(parameters);
    }

    protected Parameter createParameter(DefaultElement xmlParam) {
        String name = xmlParam.attributeValue("name");
        String value = xmlParam.attributeValue("value");
        String type = xmlParam.attributeValue("type");

        Parameter parameter = metadata.create(Parameter.class);
        parameter.setName(name);
        parameter.setAlias(name);
        parameter.setValue(createParameterValue(type, value));
        return parameter;
    }

    protected ParameterValue createParameterValue(String type, String value) {
        switch (type) {
            case "boolean":
                return new BooleanParameterValue(Boolean.valueOf(value));
            case "date":
                return new DateParameterValue(Date.valueOf(value));
            case "dateTime":
                return new DateTimeParameterValue(Date.valueOf(value));
            case "decimal":
                return new DecimalParameterValue(new BigDecimal(value));
            case "int":
                return new IntegerParameterValue(Integer.valueOf(value));
            case "long":
                return new LongParameterValue(Long.valueOf(value));
            case "time":
                return new TimeParameterValue(Date.valueOf(value));
            case "uuid":
                return new UuidParameterValue(UUID.fromString(value));
            case "string":
            default:
                return new StringParameterValue(value);
        }
    }

    protected void loadAssistanceBeanName(Dashboard resultComponent, Element element) {
        String assistantBeanName = element.attributeValue("assistantBeanName");
        if (isNotBlank(assistantBeanName)) {
            resultComponent.setAssistantBeanName(assistantBeanName);
        }
    }

}
