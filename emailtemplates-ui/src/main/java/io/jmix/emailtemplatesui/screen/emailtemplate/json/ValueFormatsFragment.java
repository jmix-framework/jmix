/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplatesui.screen.emailtemplate.json;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportValueFormat;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@UiController("emltmp_ValueFormatsFragment")
@UiDescriptor(path = "/io/jmix/reportsui/screen/report/edit/tabs/report-edit-value-formats-fragment.xml")
public class ValueFormatsFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    @Qualifier("valuesFormatsTable.createValueFormat")
    protected CreateAction<ReportValueFormat> createAction;
    @Autowired
    @Qualifier("valuesFormatsTable.editValueFormat")
    protected EditAction<ReportValueFormat> editAction;

    @Subscribe
    protected void onAttachEvent(AttachEvent event) {
        createAction.setInitializer(reportValueFormat -> reportValueFormat.setReport(reportDc.getItem()));
        editAction.setOpenMode(OpenMode.DIALOG);
    }
}
