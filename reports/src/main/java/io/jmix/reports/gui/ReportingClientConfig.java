/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.gui;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultLong;

@Source(type = SourceType.DATABASE)
public interface ReportingClientConfig extends Config {

    @Property("reporting.useBackgroundReportProcessing")
    @DefaultBoolean(false)
    boolean getUseBackgroundReportProcessing();

    void setUseBackgroundReportProcessing(boolean useBackgroundReportProcessing);

    @Property("reporting.backgroundReportProcessingTimeoutMs")
    @DefaultLong(10000)
    long getBackgroundReportProcessingTimeoutMs();

    void setBackgroundReportProcessingTimeoutMs(long backgroundReportProcessingTimeoutMs);

    /**
     * @return true if Script fields in report editor should handle TAB key as \t symbol instead of focus navigation
     * @see io.jmix.reports.gui.definition.edit.BandDefinitionEditor
     */
    @Property("reporting.enableTabSymbolInDataSetEditor")
    @DefaultBoolean(false)
    boolean getEnableTabSymbolInDataSetEditor();

    void setEnableTabSymbolInDataSetEditor(boolean enableTabSymbolInDataSetEditor);

}