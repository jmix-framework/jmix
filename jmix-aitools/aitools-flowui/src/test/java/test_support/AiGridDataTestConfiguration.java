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

import io.jmix.aitoolsflowui.gridexport.AiGridDataExportDialogContributor;
import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import io.jmix.flowui.component.groupgrid.adapter.DefaultGroupDataGridAdapterFactory;
import io.jmix.flowui.component.groupgrid.adapter.GroupDataGridAdapterFactory;
import io.jmix.gridexportflowui.GridExportFlowuiConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import static org.mockito.Mockito.mock;

/**
 * Registers the grid export add-on and the beans that send its rows to the AI, the way the optional
 * auto-configuration does in an application.
 */
@Configuration
@Import(GridExportFlowuiConfiguration.class)
public class AiGridDataTestConfiguration {

    @Bean
    AiGridDataSupport aiGridDataSupport() {
        return new AiGridDataSupport();
    }

    @Bean
    AiGridDataExportDialogContributor aiGridDataExportDialogContributor() {
        return new AiGridDataExportDialogContributor();
    }

    // Provided by the application's starter, as in the grid export tests.
    @Bean
    GroupDataGridAdapterFactory groupDataGridAdapterFactory() {
        return new DefaultGroupDataGridAdapterFactory(null);
    }

    // The loaders of the "all rows" export want one; the tests never export all rows and have no database.
    @Bean
    PlatformTransactionManager transactionManager() {
        return mock(PlatformTransactionManager.class);
    }
}
