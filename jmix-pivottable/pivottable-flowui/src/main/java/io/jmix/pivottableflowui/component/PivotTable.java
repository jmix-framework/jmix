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
import io.jmix.pivottableflowui.component.serialization.PivotTableSerializer;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PivotTable extends JmixPivotTable implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
//    protected Function<DataItem, String> dataItemKeyMapper;

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
        serializer = createSerializer();
        initOptionsChangeListener();
        Div div = new Div();
        div.setId("div-id");

        SlotUtils.addToSlot(this, "output", div);
    }

    @Override
    protected JmixPivotTableSerializer createSerializer() {
        return applicationContext.getBean(PivotTableSerializer.class);
    }
}
