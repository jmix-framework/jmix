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

package io.jmix.securityui.screen.rowlevelpolicy;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.impl.jpql.ErrorRec;
import io.jmix.data.impl.jpql.JpqlSyntaxException;
import io.jmix.security.model.RowLevelBiPredicate;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import io.jmix.securityui.model.RowLevelPolicyModel;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.WebBrowserTools;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.autocomplete.JpqlUiSuggestionProvider;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scripting.ScriptCompilationException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@UiController("sec_RowLevelPolicyModel.edit")
@UiDescriptor("row-level-policy-model-edit.xml")
@EditedEntityContainer("rowLevelPolicyModelDc")
public class RowLevelPolicyModelEdit extends StandardEditor<RowLevelPolicyModel> {

    private static final Logger log = LoggerFactory.getLogger(RowLevelPolicyModelEdit.class);

    @Autowired
    private ComboBox<String> entityNameField;

    @Autowired
    private Metadata metadata;

    @Autowired
    private JpqlUiSuggestionProvider jpqlUiSuggestionProvider;

    @Autowired
    private QueryTransformerFactory queryTransformerFactory;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Dialogs dialogs;

    @Autowired
    private WebBrowserTools webBrowserTools;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SourceCodeEditor scriptField;

    @Autowired
    private SourceCodeEditor whereClauseField;

    @Autowired
    private MessageTools messageTools;

    @Autowired
    private SourceCodeEditor joinClauseField;

    @Autowired
    private ComboBox actionField;

    public Map<String, String> getEntityOptionsMap() {
        return metadata.getClasses().stream()
                .collect(Collectors.toMap(
                        this::getEntityCaption,
                        MetaClass::getName,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    @Subscribe(id = "rowLevelPolicyModelDc", target = Target.DATA_CONTAINER)
    private void onRowLevelPolicyModelDcItemPropertyChange(
            InstanceContainer.ItemPropertyChangeEvent<RowLevelPolicyModel> event) {
        if ("type".equals(event.getProperty())) {
            initFieldsAccessForType((RowLevelPolicyType) event.getValue());
        }
    }

    private void initFieldsAccessForType(@Nullable RowLevelPolicyType type) {
        if (type == null) return;
        switch (type) {
            case JPQL:
                scriptField.setVisible(false);
                whereClauseField.setVisible(true);
                joinClauseField.setVisible(true);
                actionField.setEditable(false);
                getEditedEntity().setAction(RowLevelPolicyAction.READ);
                getEditedEntity().setScript(null);
                break;
            case PREDICATE:
                scriptField.setVisible(true);
                whereClauseField.setVisible(false);
                joinClauseField.setVisible(false);
                actionField.setEditable(true);
                getEditedEntity().setWhereClause(null);
                getEditedEntity().setJoinClause(null);
                break;
        }
    }

    private String getEntityCaption(MetaClass metaClass) {
        return String.format("%s (%s)", messageTools.getEntityCaption(metaClass), metaClass.getName());
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        entityNameField.setOptionsMap(getEntityOptionsMap());
        joinClauseField.setSuggester((source, text, cursorPosition) -> getSuggestions(true));
        whereClauseField.setSuggester((source, text, cursorPosition) -> getSuggestions(false));

        initFieldsAccessForType(getEditedEntity().getType());
    }

    private List<Suggestion> getSuggestions(boolean inJoinClause) {
        if (entityNameField.getValue() == null) {
            return Collections.emptyList();
        }
        return jpqlUiSuggestionProvider.getSuggestions(
                inJoinClause ? joinClauseField.getAutoCompleteSupport() : whereClauseField.getAutoCompleteSupport(),
                joinClauseField.getValue(),
                whereClauseField.getValue(),
                entityNameField.getValue(),
                inJoinClause
        );
    }

    @Subscribe("testBtn")
    public void onTestBtnClick(Button.ClickEvent event) {
        String entityName = entityNameField.getValue();
        if (RowLevelPolicyType.JPQL.equals(getEditedEntity().getType())) {
            String whereClause = whereClauseField.getValue();
            if (entityName == null || whereClause == null) {
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

                testPassedNotification();

            } catch (JpqlSyntaxException e) {
                StringBuilder stringBuilder = new StringBuilder();
                for (ErrorRec rec : e.getErrorRecs()) {
                    stringBuilder.append(rec.toString()).append("<br>");
                }
                testFailedNotification(stringBuilder.toString());
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause == null) {
                    rootCause = e;
                }
                String msg = rootCause.toString();
                testFailedNotification(msg);
            }
        } else {
            String script = scriptField.getValue();
            if (entityName == null || script == null) {
                return;
            }
            RowLevelBiPredicate<Object, ApplicationContext> predicate = databaseRowLevelRoleProvider.createPredicateFromScript(script);
            Object entity = metadata.create(entityName);
            try {
                predicate.test(entity, applicationContext);
                testPassedNotification();
            } catch (ScriptCompilationException e) {
                Throwable rootCause = e.getRootCause() != null ? e.getRootCause() : e;
                String message = rootCause.getMessage();
                testFailedNotification(message);
            } catch (Exception e) {
                log.info("Groovy script error: {}", e.getMessage());
                testPassedNotification();
            }
        }
    }

    private void testFailedNotification(String message) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("testFailed"))
                .withMessage(message)
                .withContentMode(ContentMode.HTML)
                .show();
    }

    private void testPassedNotification() {
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withCaption(messageBundle.getMessage("testPassed"))
                .show();
    }

    @Subscribe("docsBtn")
    public void onDocsBtnClick(Button.ClickEvent event) {
        webBrowserTools.showWebPage(
                messageBundle.getMessage("docUrl"),
                ParamsMap.of("target", "_blank")
        );
    }
}