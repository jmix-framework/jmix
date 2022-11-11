package io.jmix.securityflowui.view.rowlevelpolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import io.jmix.core.DataManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.impl.jpql.ErrorRec;
import io.jmix.data.impl.jpql.JpqlSyntaxException;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.security.model.RowLevelBiPredicate;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import io.jmix.securityflowui.model.RowLevelPolicyModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scripting.ScriptCompilationException;

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

    @Autowired
    private Dialogs dialogs;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Metadata metadata;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private QueryTransformerFactory queryTransformerFactory;
    @Autowired
    private DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(entityNameField, getEntityOptionsMap());
        docsLink.getStyle().set("margin-inline-start", "auto");
        detailActions.setAlignSelf(Alignment.CENTER, docsLink);
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
                actionField.setReadOnly(false);

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

        String baseQueryString = "select e from " + entityName + " e";
        try {
            QueryTransformer transformer = queryTransformerFactory.transformer(baseQueryString);
            if (StringUtils.isNotBlank(joinClauseField.getValue())) {
                transformer.addJoinAndWhere(joinClauseField.getValue(), whereClause);
            } else {
                transformer.addWhere(whereClause);
            }

            String jpql = transformer.getResult();
            dataManager.load(metadata.getClass(entityName).getJavaClass())
                    .query(jpql)
                    .maxResults(0)
                    .list();

            showTestPassedNotification();
        } catch (JpqlSyntaxException e) {
            Div content = new Div();
            for (ErrorRec rec : e.getErrorRecs()) {
                content.add(new Paragraph(rec.toString()));
            }

            showTestFailedNotification(content);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause == null) {
                rootCause = e;
            }

            showTestFailedNotification(new Span(rootCause.toString()));
        }
    }

    private void checkPredicateSyntax() {
        String entityName = getEditedEntity().getEntityName();
        String script = getEditedEntity().getScript();

        if (Strings.isNullOrEmpty(entityName) || Strings.isNullOrEmpty(script)) {
            return;
        }

        RowLevelBiPredicate<Object, ApplicationContext> predicate = databaseRowLevelRoleProvider.createPredicateFromScript(script);
        Object entity = metadata.create(entityName);
        try {
            predicate.test(entity, getApplicationContext());
            showTestPassedNotification();
        } catch (ScriptCompilationException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause == null) {
                rootCause = e;
            }
            String message = Strings.nullToEmpty(rootCause.getMessage());
            showTestFailedNotification(new Span(message));
        } catch (Exception e) {
            log.info("Groovy script error: {}", e.getMessage());
            showTestPassedNotification();
        }
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
}