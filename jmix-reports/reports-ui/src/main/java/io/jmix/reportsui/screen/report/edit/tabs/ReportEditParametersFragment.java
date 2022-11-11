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

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Dialogs;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UiController("report_ReportEditParameters.fragment")
@UiDescriptor("report-edit-parameters-fragment.xml")
public class ReportEditParametersFragment extends ScreenFragment {
    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    protected CollectionPropertyContainer<ReportInputParameter> parametersDc;

    @Autowired
    protected Table<ReportInputParameter> inputParametersTable;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected MetadataTools metadataTools;

    @Install(to = "inputParametersTable.name", subject = "valueProvider")
    protected String inputParametersTableNameValueProvider(ReportInputParameter parameter) {
        return metadataTools.getInstanceName(parameter);
    }

    @Install(to = "inputParametersTable.up", subject = "enabledRule")
    protected boolean inputParametersTableUpEnabledRule() {
        if (inputParametersTable != null) {
            ReportInputParameter item = inputParametersTable.getSingleSelected();
            if (item != null && parametersDc.getItem() == item) {
                return item.getPosition() > 0 && isUpdatePermitted();
            }
        }

        return false;
    }

    @Subscribe("inputParametersTable.up")
    protected void onInputParametersTableUp(Action.ActionPerformedEvent event) {
        replaceParameters(true);
    }

    @Install(to = "inputParametersTable.down", subject = "enabledRule")
    protected boolean inputParametersTableDownEnabledRule() {
        if (inputParametersTable != null) {
            ReportInputParameter item = inputParametersTable.getSingleSelected();
            if (item != null && parametersDc.getItem() == item) {
                return item.getPosition() < parametersDc.getItems().size() - 1 && isUpdatePermitted();
            }
        }

        return false;
    }

    @Subscribe("inputParametersTable.down")
    protected void onInputParametersTableDown(Action.ActionPerformedEvent event) {
        replaceParameters(false);
    }

    @Install(to = "inputParametersTable.createParameter", subject = "initializer")
    protected void inputParametersTableCreateInitializer(ReportInputParameter reportInputParameter) {
        reportInputParameter.setReport(reportDc.getItem());
        reportInputParameter.setPosition(parametersDc.getItems().size());
    }

    @Install(to = "inputParametersTable.createParameter", subject = "afterCommitHandler")
    protected void inputParametersTableCreateAfterCommitHandler(ReportInputParameter reportInputParameter) {

    }

    @Install(to = "inputParametersTable.removeParameter", subject = "afterActionPerformedHandler")
    protected void inputParametersTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ReportInputParameter> afterActionPerformedEvent) {
        orderParameters();
    }

    @Install(to = "validationScriptCodeEditor", subject = "contextHelpIconClickHandler")
    protected void validationScriptCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("parameters.validationScript"))
                .withMessage(messageBundle.getMessage("parameters.crossFieldValidationScriptHelp"))
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth("600px")
                .show();
    }

    protected void orderParameters() {
        Report report = reportDc.getItem();
        if (report.getInputParameters() == null) {
            report.setInputParameters(new ArrayList<>());
        }

        for (int i = 0; i < report.getInputParameters().size(); i++) {
            report.getInputParameters().get(i).setPosition(i);
        }
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected void replaceParameters(boolean up) {
        List<ReportInputParameter> items = parametersDc.getMutableItems();
        ReportInputParameter currentItem = parametersDc.getItem();
        ReportInputParameter itemToSwap = IterableUtils.find(items,
                e -> e.getPosition().equals(currentItem.getPosition() - (up ? 1 : -1)));
        int currentPosition = currentItem.getPosition();

        currentItem.setPosition(itemToSwap.getPosition());
        itemToSwap.setPosition(currentPosition);

        Collections.swap(items, itemToSwap.getPosition(), currentItem.getPosition());
    }
}
