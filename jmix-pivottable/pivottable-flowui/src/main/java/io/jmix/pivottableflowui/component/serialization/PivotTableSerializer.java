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

package io.jmix.pivottableflowui.component.serialization;

import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.kit.data.DataItem;
import io.jmix.pivottableflowui.kit.component.serialization.AbstractSerializer;
import io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Serializes options and data for the {@link PivotTable} into JSON for subsequent sending it to the client-side.
 */
@Component("pivotTable_PivotTableSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PivotTableSerializer extends JmixPivotTableSerializer
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initMapper();
    }

    protected void initSerializer() {
        // to skip mapper initialization
    }
}
