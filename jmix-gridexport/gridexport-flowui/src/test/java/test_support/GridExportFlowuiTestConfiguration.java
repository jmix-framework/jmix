/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.component.groupgrid.adapter.DefaultGroupDataGridAdapterFactory;
import io.jmix.flowui.component.groupgrid.adapter.GroupDataGridAdapterFactory;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import io.jmix.gridexportflowui.GridExportFlowuiConfiguration;
import io.jmix.testsupport.config.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Boots the add-on's UI layer over an in-memory database, so that views with exportable data grids can be
 * navigated to and their data exported.
 */
@Configuration
@Import({FlowuiConfiguration.class, EclipselinkConfiguration.class, CoreConfiguration.class,
        DataConfiguration.class, GridExportFlowuiConfiguration.class,
        CommonCoreTestConfiguration.class, HsqlMemDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class, LiquibaseTestConfiguration.class,
        FlowuiServletTestBeans.class, CoreSecurityTestConfiguration.class})
@JmixModule
public class GridExportFlowuiTestConfiguration {

    @Bean
    GroupDataGridAdapterFactory groupDataGridAdapterFactory() {
        return new DefaultGroupDataGridAdapterFactory(null);
    }

    @Bean
    TestAllEntitiesLoader testAllEntitiesLoader(DataManager dataManager) {
        return new TestAllEntitiesLoader(dataManager);
    }
}
