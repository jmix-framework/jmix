/*
 * Copyright 2022 Haulmont.
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

package io.jmix.autoconfigure.datatoolsflowui;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import io.jmix.core.CoreConfiguration;
import io.jmix.datatools.DatatoolsConfiguration;
import io.jmix.datatoolsflowui.DatatoolsFlowuiConfiguration;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramViewSupport;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramStorage;
import io.jmix.datatoolsflowui.datamodel.impl.DataModelDiagramViewSupportImpl;
import io.jmix.datatoolsflowui.datamodel.impl.InMemoryDataModelDiagramStorage;
import io.jmix.flowui.FlowuiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, FlowuiConfiguration.class, DatatoolsConfiguration.class,
        DatatoolsFlowuiConfiguration.class})
public class DatatoolsFlowuiAutoConfiguration {

    @VaadinSessionScope
    @Bean("datatl_DataModelDiagramViewSupport")
    @ConditionalOnMissingBean
    public DataModelDiagramViewSupport dataModelDiagramViewSupport() {
        return new DataModelDiagramViewSupportImpl();
    }

    @VaadinSessionScope
    @Bean("datatl_DataModelDiagramStorage")
    @ConditionalOnMissingBean
    public DataModelDiagramStorage dataModelDiagramStorage() {
        return new InMemoryDataModelDiagramStorage();
    }
}
