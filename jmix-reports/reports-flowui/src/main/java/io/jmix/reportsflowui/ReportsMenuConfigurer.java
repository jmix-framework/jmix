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

package io.jmix.reportsflowui;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Conditionally makes the {@code report_ReportTableView} menu item visible at application startup.
 * <p>
 * The item is hidden in the module's {@code menu.xml} by default
 * ({@code visible="false"}) and is shown only when
 * {@code jmix.reports.client.show-report-table-view-in-menu=true} is set
 * in the application properties.
 */
@Internal
@Component("report_ReportsMenuConfigurer")
public class ReportsMenuConfigurer {

    static final String REPORT_TABLE_VIEW_ID = "report_ReportTableView";

    @Autowired
    protected MenuConfig menuConfig;

    @Autowired
    protected ReportsClientProperties properties;

    /**
     * Reveals the {@code report_ReportTableView} menu item when the corresponding property is enabled.
     * Runs once after the application context is fully started.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        if (!properties.isShowReportTableViewInMenu()) {
            return;
        }

        menuConfig.getRootItems().stream()
                .map(root -> menuConfig.findItem(REPORT_TABLE_VIEW_ID, root))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(item -> item.setVisible(true));
    }
}
