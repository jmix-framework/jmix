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

package io.jmix.reportsflowui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.reports.client")
public class ReportsClientProperties {

    /**
     * Whether reports will be run in the background from the screens and actions defined by the Reports add-on.
     */
    boolean useBackgroundReportProcessing;

    long backgroundReportProcessingTimeoutMs;

    /**
     * Whether Script fields in report editor should handle TAB key as \t symbol instead of focus navigation.
     *
     * @see io.jmix.reportsui.screen.definition.edit.BandDefinitionEditor
     */
    boolean enableTabSymbolInDataSetEditor;

    public ReportsClientProperties(@DefaultValue("false") boolean useBackgroundReportProcessing,
                                   @DefaultValue("10000") long backgroundReportProcessingTimeoutMs,
                                   @DefaultValue("false") boolean enableTabSymbolInDataSetEditor) {
        this.useBackgroundReportProcessing = useBackgroundReportProcessing;
        this.backgroundReportProcessingTimeoutMs = backgroundReportProcessingTimeoutMs;
        this.enableTabSymbolInDataSetEditor = enableTabSymbolInDataSetEditor;
    }

    /**
     * @see #useBackgroundReportProcessing
     */
    public boolean getUseBackgroundReportProcessing() {
        return useBackgroundReportProcessing;
    }

    public long getBackgroundReportProcessingTimeoutMs() {
        return backgroundReportProcessingTimeoutMs;
    }

    /**
     * @see #enableTabSymbolInDataSetEditor
     */
    public boolean getEnableTabSymbolInDataSetEditor() {
        return enableTabSymbolInDataSetEditor;
    }
}