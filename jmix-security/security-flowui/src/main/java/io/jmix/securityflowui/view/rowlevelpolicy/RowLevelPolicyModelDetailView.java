package io.jmix.securityflowui.view.rowlevelpolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyModel;
import io.jmix.security.model.RowLevelPolicyType;
import io.jmix.security.role.RolePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@ViewController("sec_RowLevelPolicyModel.detail")
@ViewDescriptor("row-level-policy-model-detail-view.xml")
@EditedEntityContainer("rowLevelPolicyModelDc")
@DialogMode(width = "50em")
public class RowLevelPolicyModelDetailView extends StandardDetailView<RowLevelPolicyModel> {

    private static final Logger log = LoggerFactory.getLogger(RowLevelPolicyModelDetailView.class);

    @ViewComponent
    private JmixComboBox<String> entityNameField;
    @ViewComponent
    private Select<RowLevelPolicyAction> actionField;
    @ViewComponent
    private TextArea joinClauseField;
    @ViewComponent
    private TextArea whereClauseField;
    @ViewComponent
    private TextArea scriptField;
    @ViewComponent
    private JmixButton checkSyntaxBtn;
    @ViewComponent
    private Anchor docsLink;
    @ViewComponent
    private HorizontalLayout detailActions;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private Dialogs dialogs;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Metadata metadata;
    @Autowired(required = false)
    private RolePersistence rolePersistence;

    @Subscribe
    public void onInit(InitEvent event) {
        setReloadEdited(false);
        ComponentUtils.setItemsMap(entityNameField, getEntityOptionsMap());
        initJoinClauseFieldHelperText();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initFieldsAccessForType();
        updateCheckSyntaxButtonEnabledState();
    }

    @Subscribe(id = "rowLevelPolicyModelDc", target = Target.DATA_CONTAINER)
    private void onRowLevelPolicyModelDcItemPropertyChange(ItemPropertyChangeEvent<RowLevelPolicyModel> event) {
        String property = event.getProperty();

        if ("type".equals(property)) {
            initFieldsAccessForType();
        }

        if ("entityName".equals(property)
                || "whereClause".equals(property)
                || "script".equals(property)) {
            updateCheckSyntaxButtonEnabledState();
        }
    }

    private void updateCheckSyntaxButtonEnabledState() {
        RowLevelPolicyModel entity = getEditedEntity();
        switch (entity.getType()) {
            case JPQL:
                checkSyntaxBtn.setEnabled(!Strings.isNullOrEmpty(entity.getEntityName())
                        && !Strings.isNullOrEmpty(entity.getWhereClause()));
                break;
            case PREDICATE:
                checkSyntaxBtn.setEnabled(!Strings.isNullOrEmpty(entity.getEntityName())
                        && !Strings.isNullOrEmpty(entity.getScript()));
                break;
            default:
                checkSyntaxBtn.setEnabled(false);
        }
    }

    private void initFieldsAccessForType() {
        RowLevelPolicyType type = getEditedEntity().getType();
        if (type == null) {
            return;
        }

        switch (type) {
            case JPQL:
                actionField.setReadOnly(true);

                joinClauseField.setVisible(true);
                whereClauseField.setVisible(true);
                scriptField.setVisible(false);

                getEditedEntity().setAction(RowLevelPolicyAction.READ);
                getEditedEntity().setScript(null);
                break;
            case PREDICATE:
                actionField.setReadOnly(isReadOnly());

                joinClauseField.setVisible(false);
                whereClauseField.setVisible(false);
                scriptField.setVisible(true);

                getEditedEntity().setWhereClause(null);
                getEditedEntity().setJoinClause(null);
                break;
        }
    }

    @Subscribe("checkSyntaxBtn")
    public void onCheckSyntaxBtnClick(ClickEvent<Button> event) {
        RowLevelPolicyType type = getEditedEntity().getType();
        switch (type) {
            case JPQL:
                checkJpqlSyntax();
                break;
            case PREDICATE:
                checkPredicateSyntax();
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private void checkJpqlSyntax() {
        String entityName = getEditedEntity().getEntityName();
        String whereClause = getEditedEntity().getWhereClause();

        if (Strings.isNullOrEmpty(entityName) || Strings.isNullOrEmpty(whereClause)) {
            return;
        }

        List<String> errors = getRolePersistence().checkRowLevelJpqlPolicySyntax(
                entityName, joinClauseField.getValue(), whereClause);
        if (errors.isEmpty()) {
            showTestPassedNotification();
        } else {
            Div content = new Div();
            for (String rec : errors) {
                content.add(new Paragraph(rec.toString()));
            }
            showTestFailedNotification(content);
        }
    }

    private void checkPredicateSyntax() {
        String entityName = getEditedEntity().getEntityName();
        String script = getEditedEntity().getScript();

        if (Strings.isNullOrEmpty(entityName) || Strings.isNullOrEmpty(script)) {
            return;
        }

        String error = getRolePersistence().checkRowLevelPredicatePolicySyntax(entityName, script);
        if (error == null) {
            showTestPassedNotification();
        } else {
            showTestFailedNotification(new Span(error));
        }
    }

    protected void initJoinClauseFieldHelperText() {
        String joinHelperMessage = messageBundle.getMessage("joinClauseField.helperText");
        joinClauseField.setHelperComponent(new Html(joinHelperMessage));
    }

    private void showTestFailedNotification(Component content) {
        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage("notification.testFailed.header"))
                .withContent(content)
                .open();
    }

    private void showTestPassedNotification() {
        notifications.create(messageBundle.getMessage("notification.testPassed.message"))
                .show();
    }

    public Map<String, String> getEntityOptionsMap() {
        return metadata.getClasses().stream()
                .collect(Collectors.toMap(
                        MetaClass::getName,
                        this::getEntityCaption,
                        this::throwDuplicateException,
                        TreeMap::new));
    }

    protected String getEntityCaption(MetaClass metaClass) {
        return String.format("%s (%s)", messageTools.getEntityCaption(metaClass), metaClass.getName());
    }

    protected String throwDuplicateException(String v1, String v2) {
        throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
    }

    private RolePersistence getRolePersistence() {
        if (rolePersistence == null) {
            throw new IllegalStateException("RolePersistence is not available");
        }
        return rolePersistence;
    }
}