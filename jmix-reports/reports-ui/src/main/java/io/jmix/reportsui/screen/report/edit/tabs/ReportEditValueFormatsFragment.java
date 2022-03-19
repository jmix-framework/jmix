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

package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportValueFormat;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_ReportEditValueFormats.fragment")
@UiDescriptor("report-edit-value-formats-fragment.xml")
public class ReportEditValueFormatsFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Install(to = "valuesFormatsTable.createValueFormat", subject = "initializer")
    protected void valuesFormatsTableCreateInitializer(ReportValueFormat reportValueFormat) {
        reportValueFormat.setReport(reportDc.getItem());
    }
}
