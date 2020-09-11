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
package io.jmix.reports.gui.definition.edit;

import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.TextField;
import io.jmix.core.Stores;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.components.*;
import io.jmix.ui.action.list.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import io.jmix.core.security.EntityOp;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.app.service.ReportWizardService;
import io.jmix.reports.entity.*;
import io.jmix.reports.gui.ReportingClientConfig;
import io.jmix.reports.gui.definition.edit.crosstab.CrossTabTableDecorator;
import io.jmix.reports.gui.definition.edit.scripteditordialog.ScriptEditorDialog;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.autocomplete.AutoCompleteSupport;
import io.jmix.ui.component.autocomplete.JpqlSuggestionFactory;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.gui.OpenType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import javax.inject.Named;
import java.util.*;

public class BandDefinitionEditor extends AbstractFrame implements Suggester {

    @Autowired
    protected Datasource<BandDefinition> bandDefinitionDs;
    @Autowired
    protected CollectionDatasource<DataSet, UUID> dataSetsDs;
    @Autowired
    protected Datasource<Report> reportDs;
    @Autowired
    protected CollectionDatasource<ReportInputParameter, UUID> parametersDs;
    @Autowired
    protected Table<DataSet> dataSets;
    @Named("text")
    protected SourceCodeEditor dataSetScriptField;
    @Autowired
    protected SourceCodeEditor jsonGroovyCodeEditor;
    @Autowired
    protected BoxLayout textBox;
    @Autowired
    protected Label entitiesParamLabel;
    @Autowired
    protected Label entityParamLabel;
    @Autowired
    protected GridLayout commonEntityGrid;
    @Autowired
    protected LookupField jsonSourceTypeField;
    @Autowired
    protected VBoxLayout jsonDataSetTypeVBox;
    @Autowired
    protected Label jsonPathQueryLabel;
    @Autowired
    protected VBoxLayout jsonSourceGroovyCodeVBox;
    @Autowired
    protected VBoxLayout jsonSourceURLVBox;
    @Autowired
    protected VBoxLayout jsonSourceParameterCodeVBox;
    @Autowired
    protected HBoxLayout textParamsBox;
    @Autowired
    protected Label viewNameLabel;
    @Autowired
    protected LookupField orientation;
    @Autowired
    protected LookupField parentBand;
    @Autowired
    protected TextField name;
    @Autowired
    protected LookupField viewNameLookup;
    @Autowired
    protected LookupField entitiesParamLookup;
    @Autowired
    protected LookupField entityParamLookup;
    @Autowired
    protected LookupField dataStore;
    @Autowired
    protected CheckBox processTemplate;
    @Autowired
    protected CheckBox useExistingViewCheckbox;
    @Autowired
    protected Button viewEditButton;
    @Autowired
    protected Label buttonEmptyElement;
    @Autowired
    protected Label checkboxEmptyElement;
    @Autowired
    protected Label spacer;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected ReportWizardService reportWizardService;
    @Autowired
    protected BoxLayout editPane;
    @Autowired
    protected DataSetFactory dataSetFactory;
    @Autowired
    protected CrossTabTableDecorator tabOrientationTableDecorator;
    @Autowired
    protected Configuration configuration;
    @Autowired
    private Security security;
    @Autowired
    protected TextArea jsonPathQueryTextAreaField;
    @Autowired
    protected JpqlSuggestionFactory jpqlSuggestionFactory;
    @Autowired
    protected Stores stores;
    @Autowired
    protected ReportingClientConfig reportingClientConfig;

    protected SourceCodeEditor.Mode dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;

    public interface Companion {
        void initDatasetsTable(Table table);
    }

    public void showJsonScriptEditorDialog() {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) openWindow(
                "scriptEditorDialog",
                OpenType.DIALOG,
                ParamsMap.of(
                        "caption", getScriptEditorDialogCaption(),
                        "scriptValue", jsonGroovyCodeEditor.getValue(),
                        "helpHandler", jsonGroovyCodeEditor.getContextHelpIconClickHandler()
                ));
        editorDialog.addCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                jsonGroovyCodeEditor.setValue(editorDialog.getValue());
            }
        });
    }

    protected String getScriptEditorDialogCaption() {
        ReportGroup group = reportDs.getItem().getGroup();
        String report = reportDs.getItem().getName();

        if (ObjectUtils.isNotEmpty(group) && ObjectUtils.isNotEmpty(report)) {
            return AppBeans.get(Messages.class)
                    .formatMessage(getClass(), "scriptEditorDialog.captionFormat", report, bandDefinitionDs.getItem().getName());
        }
        return null;
    }

    public void showDataSetScriptEditorDialog() {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) openWindow(
                "scriptEditorDialog",
                OpenType.DIALOG,
                ParamsMap.of(
                        "caption", getScriptEditorDialogCaption(),
                        "mode", dataSetScriptFieldMode,
                        "suggester", dataSetScriptField.getSuggester(),
                        "scriptValue", dataSetScriptField.getValue(),
                        "helpHandler", dataSetScriptField.getContextHelpIconClickHandler()
                ));
        editorDialog.addCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                dataSetScriptField.setValue(editorDialog.getValue());
            }
        });
    }

    public void setBandDefinition(BandDefinition bandDefinition) {
        bandDefinitionDs.setItem(bandDefinition);
        name.setEditable((bandDefinition == null || bandDefinition.getParent() != null)
                && isUpdatePermitted());
    }

    public Datasource<BandDefinition> getBandDefinitionDs() {
        return bandDefinitionDs;
    }

    @Override
    public void setEnabled(boolean enabled) {
        //Desktop Component containers doesn't apply disable flags for child components
        for (Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    @Override
    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        int queryPosition = cursorPosition - 1;

        return jpqlSuggestionFactory.requestHint(text, queryPosition, source, cursorPosition);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initDataSetListeners();

        initBandDefinitionsListeners();

        initParametersListeners();

        initCompanion();

        initActions();

        initDataStoreField();

        initSourceCodeOptions();

        initHelpButtons();
    }

    protected void initHelpButtons() {
        jsonGroovyCodeEditor.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("dataSet.text"), getMessage("dataSet.jsonSourceGroovyCodeHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(700f)));
        jsonPathQueryTextAreaField.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("dataSet.text"), getMessage("dataSet.jsonPathQueryHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(700f)));
    }

    protected void initSourceCodeOptions() {
        boolean enableTabSymbolInDataSetEditor = reportingClientConfig.getEnableTabSymbolInDataSetEditor();
        jsonGroovyCodeEditor.setHandleTabKey(enableTabSymbolInDataSetEditor);
        dataSetScriptField.setHandleTabKey(enableTabSymbolInDataSetEditor);
    }

    protected void initJsonDataSetOptions(DataSet dataSet) {
        jsonDataSetTypeVBox.removeAll();
        jsonDataSetTypeVBox.add(jsonSourceTypeField);
        jsonDataSetTypeVBox.add(jsonPathQueryLabel);
        jsonDataSetTypeVBox.add(jsonPathQueryTextAreaField);

        if (dataSet.getJsonSourceType() == null) {
            dataSet.setJsonSourceType(JsonSourceType.GROOVY_SCRIPT);
        }

        switch (dataSet.getJsonSourceType()) {
            case GROOVY_SCRIPT:
                jsonDataSetTypeVBox.add(jsonSourceGroovyCodeVBox);
                jsonDataSetTypeVBox.expand(jsonSourceGroovyCodeVBox);
                break;
            case URL:
                jsonDataSetTypeVBox.add(jsonSourceURLVBox);
                jsonDataSetTypeVBox.expand(jsonSourceURLVBox);
                break;
            case PARAMETER:
                jsonDataSetTypeVBox.add(jsonSourceParameterCodeVBox);
                jsonDataSetTypeVBox.add(spacer);
                jsonDataSetTypeVBox.expand(spacer);
                break;
        }
    }

    protected void initDataStoreField() {
        Map<String, Object> all = new HashMap<>();
        all.put(getMessage("dataSet.dataStoreMain"), Stores.MAIN);
        for (String additional : stores.getAdditional()) {
            all.put(additional, additional);
        }
        dataStore.setOptionsMap(all);
    }

    protected void initCompanion() {
        Companion companion = getCompanion();
        if (companion != null) {
            companion.initDatasetsTable(dataSets);
        }
    }

    protected void initActions() {
        dataSets.addAction(new RemoveAction(dataSets, false) {
            @Override
            public String getDescription() {
                return getMessage("description.removeDataSet");
            }

            @Override
            public String getCaption() {
                return "";
            }
        });

        dataSets.addAction(new AbstractAction("create") {
            @Override
            public String getDescription() {
                return getMessage("description.createDataSet");
            }

            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition selectedBand = bandDefinitionDs.getItem();
                if (selectedBand != null) {
                    DataSet dataset = dataSetFactory.createEmptyDataSet(selectedBand);
                    selectedBand.getDataSets().add(dataset);
                    dataSetsDs.addItem(dataset);
                    dataSetsDs.setItem(dataset);
                    dataSets.setSelected(dataset);
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });

        Action editDataSetViewAction = new EditViewAction(this);
        viewEditButton.setAction(editDataSetViewAction);

        viewNameLookup.setOptionsMap(new HashMap<>());

        entitiesParamLookup.setNewOptionAllowed(true);
        entityParamLookup.setNewOptionAllowed(true);
        viewNameLookup.setNewOptionAllowed(true);
        entitiesParamLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "listEntitiesParamName"));
        entityParamLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "entityParamName"));
        viewNameLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "viewName"));
    }

    protected void initParametersListeners() {
        parametersDs.addCollectionChangeListener(e -> {
            Map<String, Object> paramAliases = new HashMap<>();

            for (ReportInputParameter item : e.getDs().getItems()) {
                paramAliases.put(item.getName(), item.getAlias());
            }
            entitiesParamLookup.setOptionsMap(paramAliases);
            entityParamLookup.setOptionsMap(paramAliases);
        });
    }

    protected void initBandDefinitionsListeners() {
        bandDefinitionDs.addItemChangeListener(e -> {
            updateRequiredIndicators(e.getItem());
            selectFirstDataSet();
        });
        bandDefinitionDs.addItemPropertyChangeListener(e -> {
            if ("name".equals(e.getProperty()) && StringUtils.isBlank((String) e.getValue())) {
                e.getItem().setName("*");
            }
        });
    }

    protected void initDataSetListeners() {
        tabOrientationTableDecorator.decorate(dataSets, bandDefinitionDs);

        dataSetsDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                applyVisibilityRules(e.getItem());

                if (e.getItem().getType() == DataSetType.SINGLE) {
                    refreshViewNames(findParameterByAlias(e.getItem().getEntityParamName()));
                } else if (e.getItem().getType() == DataSetType.MULTI) {
                    refreshViewNames(findParameterByAlias(e.getItem().getListEntitiesParamName()));
                }

                dataSetScriptField.resetEditHistory();
            } else {
                hideAllDataSetEditComponents();
            }
        });

        dataSetsDs.addItemPropertyChangeListener(e -> {
            applyVisibilityRules(e.getItem());
            if ("entityParamName".equals(e.getProperty()) || "listEntitiesParamName".equals(e.getProperty())) {
                ReportInputParameter linkedParameter = findParameterByAlias(String.valueOf(e.getValue()));
                refreshViewNames(linkedParameter);
            }

            if ("processTemplate".equals(e.getProperty()) && e.getItem() != null) {
                applyVisibilityRulesForType(e.getItem());
            }

            @SuppressWarnings("unchecked")
            DatasourceImplementation<DataSet> implementation = (DatasourceImplementation<DataSet>) dataSetsDs;
            implementation.modified(e.getItem());
        });

        dataSetScriptField.resetEditHistory();

        hideAllDataSetEditComponents();
    }

    protected boolean isUpdatePermitted() {
        return security.isEntityOpPermitted(metadata.getClassNN(Report.class), EntityOp.UPDATE);
    }

    protected void updateRequiredIndicators(BandDefinition item) {
        boolean required = !(item == null || reportDs.getItem().getRootBandDefinition().equals(item));
        parentBand.setRequired(required);
        orientation.setRequired(required);
        name.setRequired(item != null);
    }

    @Nullable
    protected ReportInputParameter findParameterByAlias(String alias) {
        for (ReportInputParameter reportInputParameter : parametersDs.getItems()) {
            if (reportInputParameter.getAlias().equals(alias)) {
                return reportInputParameter;
            }
        }
        return null;
    }

    protected void refreshViewNames(@Nullable ReportInputParameter reportInputParameter) {
        if (reportInputParameter != null) {
            if (StringUtils.isNotBlank(reportInputParameter.getEntityMetaClass())) {
                MetaClass parameterMetaClass = metadata.getClass(reportInputParameter.getEntityMetaClass());
                Collection<String> viewNames = metadata.getViewRepository().getViewNames(parameterMetaClass);
                Map<String, Object> views = new HashMap<>();
                for (String viewName : viewNames) {
                    views.put(viewName, viewName);
                }
                views.put(View.LOCAL, View.LOCAL);
                views.put(View.MINIMAL, View.MINIMAL);
                viewNameLookup.setOptionsMap(views);
                return;
            }
        }

        viewNameLookup.setOptionsMap(new HashMap<>());
    }

    protected void applyVisibilityRules(DataSet item) {
        applyVisibilityRulesForType(item);
        if (item.getType() == DataSetType.SINGLE || item.getType() == DataSetType.MULTI) {
            applyVisibilityRulesForEntityType(item);
        }
    }

    protected void applyVisibilityRulesForType(DataSet dataSet) {
        hideAllDataSetEditComponents();

        if (dataSet.getType() != null) {
            switch (dataSet.getType()) {
                case SQL:
                case JPQL:
                    textParamsBox.add(dataStore);
                    textBox.add(processTemplate);
                case GROOVY:
                    editPane.add(textBox);
                    break;
                case SINGLE:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisiblity(true, false);
                    editPane.add(spacer);
                    editPane.expand(spacer);
                    break;
                case MULTI:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisiblity(false, true);
                    editPane.add(spacer);
                    editPane.expand(spacer);
                    break;
                case JSON:
                    initJsonDataSetOptions(dataSet);
                    editPane.add(jsonDataSetTypeVBox);
                    break;
            }

            switch (dataSet.getType()) {
                case SQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.SQL;
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.SQL);
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setContextHelpIconClickHandler(null);
                    break;

                case GROOVY:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Groovy;
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Groovy);
                    dataSetScriptField.setContextHelpIconClickHandler(e ->
                            showMessageDialog(getMessage("dataSet.text"), getMessage("dataSet.textHelp"),
                                    MessageType.CONFIRMATION_HTML
                                            .modal(false)
                                            .width(700f)));
                    break;

                case JPQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(processTemplate.isChecked() ? null : this);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    dataSetScriptField.setContextHelpIconClickHandler(null);
                    break;

                default:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    dataSetScriptField.setContextHelpIconClickHandler(null);
                    break;
            }
        }
    }

    protected void applyVisibilityRulesForEntityType(DataSet item) {
        commonEntityGrid.remove(viewNameLabel);
        commonEntityGrid.remove(viewNameLookup);
        commonEntityGrid.remove(viewEditButton);
        commonEntityGrid.remove(buttonEmptyElement);
        commonEntityGrid.remove(useExistingViewCheckbox);
        commonEntityGrid.remove(checkboxEmptyElement);

        if (Boolean.TRUE.equals(item.getUseExistingView())) {
            commonEntityGrid.add(viewNameLabel);
            commonEntityGrid.add(viewNameLookup);
        } else {
            commonEntityGrid.add(viewEditButton);
            commonEntityGrid.add(buttonEmptyElement);
        }

        commonEntityGrid.add(useExistingViewCheckbox);
        commonEntityGrid.add(checkboxEmptyElement);
    }

    protected void hideAllDataSetEditComponents() {
        // do not use setVisible(false) due to web legacy (Vaadin 6) layout problems #PL-3916
        textParamsBox.remove(dataStore);
        textBox.remove(processTemplate);
        editPane.remove(textBox);
        editPane.remove(commonEntityGrid);
        editPane.remove(jsonDataSetTypeVBox);
        editPane.remove(spacer);
    }

    protected void selectFirstDataSet() {
        dataSetsDs.refresh();
        if (!dataSetsDs.getItemIds().isEmpty()) {
            DataSet item = dataSetsDs.getItem(dataSetsDs.getItemIds().iterator().next());
            dataSets.setSelected(item);
        } else {
            dataSets.setSelected((DataSet) null);
        }
    }

    // For EditViewAction
    @Override
    protected String formatMessage(String key, Object... params) {
        return super.formatMessage(key, params);
    }

    // This is a stub for using set in some DataSet change listener
    protected void setViewEditVisibility(DataSet dataSet) {
        if (isViewEditAllowed(dataSet)) {
            viewEditButton.setVisible(true);
        } else {
            viewEditButton.setVisible(false);
        }
    }

    protected boolean isViewEditAllowed(DataSet dataSet) {
        return true;
    }

    protected void setCommonEntityGridVisiblity(boolean visibleEntityGrid, boolean visibleEntitiesGrid) {
        entityParamLabel.setVisible(visibleEntityGrid);
        entityParamLookup.setVisible(visibleEntityGrid);
        entitiesParamLabel.setVisible(visibleEntitiesGrid);
        entitiesParamLookup.setVisible(visibleEntitiesGrid);
    }
}