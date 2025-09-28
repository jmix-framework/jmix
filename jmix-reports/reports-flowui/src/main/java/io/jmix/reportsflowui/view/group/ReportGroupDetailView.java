package io.jmix.reportsflowui.view.group;

import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.action.view.DetailSaveCloseAction;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportGroupRepository;
import io.jmix.reports.entity.ReportGroup;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

@Route(value = "reports/groups/:id", layout = DefaultMainViewParent.class)
@ViewController("report_ReportGroup.detail")
@ViewDescriptor("report-group-detail-view.xml")
@EditedEntityContainer("groupDc")
@DialogMode(width = "50em")
public class ReportGroupDetailView extends StandardDetailView<ReportGroup> {
    @ViewComponent
    protected DetailSaveCloseAction<Object> saveAction;
    @ViewComponent
    protected TypedTextField<String> codeField;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected ReportGroupRepository groupRepository;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    private EntityStates entityStates;

    protected void markFieldAndPreventSave(TypedTextField<?> field, String messageBundleKey, BeforeSaveEvent event) {
        event.preventSave();
        field.setErrorMessage(messageBundle.getMessage(messageBundleKey));
        field.setInvalid(true);
    }

    protected void checkReportGroupCode(BeforeSaveEvent event) {
        ValidationErrors errors = new ValidationErrors();
        String groupCode = codeField.getTypedValue();
        ReportGroup editedGroup = getEditedEntity();
        Optional<UUID> groupId;

        if (groupCode == null) {
            markFieldAndPreventSave(codeField, "reportGroupDetailView.codeField.isEmpty.text", event);
            return;
        }

        try {
            if (entityStates.isNew(editedGroup)) {
                if (groupRepository.existsGroupByCode(groupCode)) {
                    markFieldAndPreventSave(codeField, "reportGroupDetailView.codeField.alreadyExists.text", event);
                    return;
                }
            }

            groupId = groupRepository.loadGroupIdByCode(groupCode);
            if (groupId.isPresent()) {
                if (!editedGroup.getId().equals(groupId.get())) {
                    if (groupRepository.existsGroupByCode(groupCode)) {
                        markFieldAndPreventSave(codeField, "reportGroupDetailView.codeField.alreadyExists.text", event);
                    }
                }
            }
        } catch (AccessDeniedException ade) {
            event.preventSave();
            errors.add(messageBundle.getMessage("reportGroupDetailView.notification.notReadAccessRights.text"));
        } finally {
            if (!errors.isEmpty()) {
                viewValidation.showValidationErrors(errors);
            }
        }
    }

    @Subscribe
    protected void onBeforeSave(BeforeSaveEvent event) {
        checkReportGroupCode(event);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (readOnly) {
            saveAction.setEnabled(false);
        }
    }
}
