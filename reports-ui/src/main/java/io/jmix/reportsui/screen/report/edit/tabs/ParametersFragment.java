package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("report_ReportEditParameters.fragment")
@UiDescriptor("parameters.xml")
public class ParametersFragment extends ScreenFragment {

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
    protected Messages messages;

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
        ReportInputParameter parameter = inputParametersTable.getSingleSelected();
        if (parameter != null) {
            List<ReportInputParameter> inputParameters = reportDc.getItem().getInputParameters();
            int index = parameter.getPosition();
            if (index > 0) {
                ReportInputParameter previousParameter = null;
                for (ReportInputParameter _param : inputParameters) {
                    if (_param.getPosition() == index - 1) {
                        previousParameter = _param;
                        break;
                    }
                }
                if (previousParameter != null) {
                    parameter.setPosition(previousParameter.getPosition());
                    previousParameter.setPosition(index);

                    sortParametersByPosition();
                }
            }
        }
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
        ReportInputParameter parameter = inputParametersTable.getSingleSelected();
        if (parameter != null) {
            List<ReportInputParameter> inputParameters = reportDc.getItem().getInputParameters();
            int index = parameter.getPosition();
            if (index < parametersDc.getItems().size() - 1) {
                ReportInputParameter nextParameter = null;
                for (ReportInputParameter _param : inputParameters) {
                    if (_param.getPosition() == index + 1) {
                        nextParameter = _param;
                        break;
                    }
                }
                if (nextParameter != null) {
                    parameter.setPosition(nextParameter.getPosition());
                    nextParameter.setPosition(index);

                    sortParametersByPosition();
                }
            }
        }
    }

    @Install(to = "inputParametersTable.create", subject = "initializer")
    protected void inputParametersTableCreateInitializer(ReportInputParameter reportInputParameter) {
        reportInputParameter.setReport(reportDc.getItem());
        reportInputParameter.setPosition(parametersDc.getItems().size());
    }

    @Install(to = "inputParametersTable.create", subject = "afterCommitHandler")
    protected void inputParametersTableCreateAfterCommitHandler(ReportInputParameter reportInputParameter) {

    }

    @Install(to = "inputParametersTable.remove", subject = "afterActionPerformedHandler")
    protected void inputParametersTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ReportInputParameter> afterActionPerformedEvent) {
        orderParameters();
    }

    @Install(to = "validationScriptCodeEditor", subject = "contextHelpIconClickHandler")
    protected void validationScriptCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage(getClass(), "parameters.validationScript"))
                .withMessage(messages.getMessage(getClass(), "parameters.crossFieldValidationScriptHelp"))
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

    protected void sortParametersByPosition() {
        parametersDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "position"));
    }
}
