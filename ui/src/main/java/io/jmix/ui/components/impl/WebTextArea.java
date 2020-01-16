/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.components.impl;

import io.jmix.ui.components.TextArea;
import io.jmix.ui.widgets.CubaTextArea;
import com.vaadin.shared.ui.ValueChangeMode;
import org.springframework.beans.factory.InitializingBean;

public class WebTextArea<V> extends WebAbstractTextArea<CubaTextArea, V>
        implements TextArea<V>, InitializingBean {

    public WebTextArea() {
        this.component = createComponent();

        attachValueChangeListener(component);
    }

    protected CubaTextArea createComponent() {
        return new CubaTextArea();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(CubaTextArea component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
    }

    @Override
    public CaseConversion getCaseConversion() {
        return CaseConversion.valueOf(component.getCaseConversion().name());
    }

    @Override
    public void setCaseConversion(CaseConversion caseConversion) {
        io.jmix.ui.widgets.CaseConversion widgetCaseConversion =
                io.jmix.ui.widgets.CaseConversion.valueOf(caseConversion.name());
        component.setCaseConversion(widgetCaseConversion);
    }
}
