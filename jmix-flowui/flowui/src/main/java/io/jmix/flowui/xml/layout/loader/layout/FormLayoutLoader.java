/*
 * Copyright (c) 2008-2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.layout;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import io.jmix.flowui.xml.layout.loader.AbstractContainerLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class FormLayoutLoader extends AbstractContainerLoader<FormLayout> {

    protected List<ResponsiveStep> pendingLoadResponsiveSteps = new ArrayList<>();

    protected DataLoaderSupport dataLoaderSupport;

    public DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    @Override
    protected FormLayout createComponent() {
        return factory.create(FormLayout.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createResponsiveSteps();

        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadResponsiveSteps();
        loadSubComponents();
    }

    protected void createResponsiveSteps() {
        for (Element subElement : element.elements("responsiveStep")) {
            String minWidth = loadString(subElement, "minWidth")
                    .orElse(null);
            Integer columns = loadInteger(subElement, "columns")
                    .orElse(1);
            LabelsPosition labelsPosition = loadEnum(subElement, LabelsPosition.class, "labelsPosition")
                    .orElse(null);

            pendingLoadResponsiveSteps.add(new ResponsiveStep(minWidth, columns, labelsPosition));
        }
    }

    protected void loadResponsiveSteps() {
        resultComponent.setResponsiveSteps(pendingLoadResponsiveSteps);

        pendingLoadResponsiveSteps.clear();
    }

    @Override
    protected boolean isChildElementIgnored(Element subElement) {
        return "responsiveStep".equalsIgnoreCase(subElement.getName());
    }

    public static class FormItemLoader extends AbstractContainerLoader<FormLayout.FormItem> {

        @Override
        protected FormLayout.FormItem createComponent() {
            return factory.create(FormLayout.FormItem.class);
        }

        @Override
        public void initComponent() {
            super.initComponent();

            createSubComponents(resultComponent, element);
        }

        @Override
        public void loadComponent() {
            loadVisible(resultComponent, element);
            loadColspan();
            loadSubComponents();
        }

        protected void loadColspan() {
            Integer colspan = loadInteger(element, "colspan").orElse(1);
            FormLayout form = (FormLayout) resultComponent.getParent().orElse(null);
            assert form != null;
            form.setColspan(resultComponent, colspan);
        }
    }
}
