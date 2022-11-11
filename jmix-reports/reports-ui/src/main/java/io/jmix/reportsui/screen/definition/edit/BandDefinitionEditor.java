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
package io.jmix.reportsui.screen.definition.edit;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.*;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reportsui.action.list.EditFetchPlanAction;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.definition.edit.crosstab.CrossTabTableDecorator;
import io.jmix.reportsui.screen.definition.edit.scripteditordialog.ScriptEditorDialog;
import io.jmix.reportsui.screen.report.wizard.ReportsWizard;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Actions;
import io.jmix.ui.Dialogs;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.autocomplete.AutoCompleteSupport;
import io.jmix.ui.component.autocomplete.JpqlUiSuggestionProvider;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.component.data.options.MapOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;

@UiController("report_BandDefinitionEditor.fragment")
@UiDescriptor("band-definition-edit-fragment.xml")
public class BandDefinitionEditor extends ScreenFragment implements Suggester {

    @Autowired
    protected CollectionContainer<BandDefinition> bandsDc;
    @Autowired
    protected CollectionContainer<DataSet> dataSetsDc;
    @Autowired
    protected InstanceContainer<Report> reportDc;
    @Autowired
    protected CollectionContainer<ReportInputParameter> parametersDc;
    @Autowired
    protected Table<DataSet> dataSetsTable;
    @Autowired
    protected SourceCodeEditor dataSetScriptField;
    @Autowired
    protected SourceCodeEditor jsonGroovyCodeEditor;
    @Autowired
    protected BoxLayout dataSetScriptBox;
    @Autowired
    protected Label<String> entitiesParamLabel;
    @Autowired
    protected Label<String> entityParamLabel;
    @Autowired
    protected GridLayout commonEntityGrid;
    @Autowired
    protected ComboBox<JsonSourceType> jsonSourceTypeField;
    @Autowired
    protected VBoxLayout jsonDataSetTypeVBox;
    @Autowired
    protected Label<String> jsonPathQueryLabel;
    @Autowired
    protected VBoxLayout jsonSourceGroovyCodeVBox;
    @Autowired
    protected VBoxLayout jsonSourceURLVBox;
    @Autowired
    protected VBoxLayout jsonSourceParameterCodeVBox;
    @Autowired
    protected HBoxLayout textParamsBox;
    @Autowired
    protected Label<String> fetchPlanNameLabel;
    @Autowired
    protected ComboBox<Orientation> orientationField;
    @Autowired
    protected ComboBox<BandDefinition> parentBandField;
    @Autowired
    protected TextField<String> nameField;
    @Autowired
    protected ComboBox<String> fetchPlanNameField;
    @Autowired
    protected ComboBox<String> entitiesParamField;
    @Autowired
    protected ComboBox<String> entityParamField;
    @Autowired
    protected ComboBox dataStoreField;
    @Autowired
    protected CheckBox isProcessTemplateField;
    @Autowired
    protected CheckBox isUseExistingFetchPlanField;
    @Autowired
    protected Button fetchPlanEditButton;
    @Autowired
    protected Label<String> buttonEmptyElement;
    @Autowired
    protected Label<String> checkboxEmptyElement;
    @Autowired
    protected Label<String> spacerLabel;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ReportsWizard reportWizardService;
    @Autowired
    protected BoxLayout editPane;
    @Autowired
    protected DataSetFactory dataSetFactory;
    @Autowired
    protected CrossTabTableDecorator tabOrientationTableDecorator;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected TextArea<String> jsonPathQueryTextAreaField;
    @Autowired
    protected JpqlUiSuggestionProvider jpqlUiSuggestionProvider;
    @Autowired
    protected Stores stores;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected Actions actions;

    @Autowired
    protected DataContext dataContext;

    protected SourceCodeEditor.Mode dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;

    protected EditFetchPlanAction editFetchPlanAction;

    @Subscribe("jsonSourceGroovyCodeLinkBtn")
    protected void showJsonScriptEditorDialog(Button.ClickEvent event) {
        ScriptEditorDialog editorDialog = screenBuilders.screen(this)
                .withScreenClass(ScriptEditorDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "scriptValue", jsonGroovyCodeEditor.getValue(),
                        "helpHandler", jsonGroovyCodeEditor.getContextHelpIconClickHandler()
                ))).build();
        editorDialog.setCaption(getScriptEditorDialogCaption());
        editorDialog.addAfterCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(((StandardCloseAction) actionId.getCloseAction()).getActionId())) {
                jsonGroovyCodeEditor.setValue(editorDialog.getValue());
            }
        });

        editorDialog.show();
    }

    protected String getScriptEditorDialogCaption() {
        ReportGroup group = reportDc.getItem().getGroup();
        String report = reportDc.getItem().getName();

        if (ObjectUtils.isNotEmpty(group) && ObjectUtils.isNotEmpty(report)) {
            return messageBundle.formatMessage("scriptEditorDialog.captionFormat", report, bandsDc.getItem().getName());
        }
        return StringUtils.EMPTY;
    }

    @Subscribe("dataSetTextLinkBtn")
    protected void showDataSetScriptEditorDialog(Button.ClickEvent event) {
        ScriptEditorDialog editorDialog = screenBuilders.screen(this)
                .withScreenClass(ScriptEditorDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "mode", dataSetScriptFieldMode,
                        "suggester", dataSetScriptField.getSuggester(),
                        "scriptValue", dataSetScriptField.getValue(),
                        "helpHandler", dataSetScriptField.getContextHelpIconClickHandler()
                ))).build();
        editorDialog.setCaption(getScriptEditorDialogCaption());
        editorDialog.addAfterCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(((StandardCloseAction) actionId.getCloseAction()).getActionId())) {
                dataSetScriptField.setValue(editorDialog.getValue());
            }
        });

        editorDialog.show();
    }

    public InstanceContainer<BandDefinition> getBandDefinitionDc() {
        return bandsDc;
    }

    public void setEnabled(boolean enabled) {
        //Desktop Component containers doesn't apply disable flags for child components
        for (Component component : getFragment().getComponents()) {
            component.setEnabled(enabled);
        }
    }

    @Override
    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        int queryPosition = cursorPosition - 1;

        return jpqlUiSuggestionProvider.getSuggestions(text, queryPosition, source);
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        initDataSetListeners();

        initActions();

        initDataStoreField();

        initSourceCodeOptions();
    }

    @Install(to = "jsonGroovyCodeEditor", subject = "contextHelpIconClickHandler")
    protected void jsonGroovyCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("dataSet.text"))
                .withMessage(messageBundle.getMessage("dataSet.jsonSourceGroovyCodeHelp"))
                .withModal(false)
                .withWidth("700px")
                .withContentMode(ContentMode.HTML)
                .show();
    }

    @Install(to = "jsonPathQueryTextAreaField", subject = "contextHelpIconClickHandler")
    protected void jsonPathQueryTextAreaFieldContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("dataSet.text"))
                .withMessage(messageBundle.getMessage("dataSet.jsonPathQueryHelp"))
                .withModal(false)
                .withWidth("700px")
                .withContentMode(ContentMode.HTML)
                .show();
    }

    protected void initSourceCodeOptions() {
        boolean enableTabSymbolInDataSetEditor = reportsClientProperties.getEnableTabSymbolInDataSetEditor();
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
                jsonDataSetTypeVBox.add(spacerLabel);
                jsonDataSetTypeVBox.expand(spacerLabel);
                break;
        }
    }

    protected void initDataStoreField() {
        Map<String, Object> all = new HashMap<>();
        all.put(messageBundle.getMessage("dataSet.dataStoreMain"), Stores.MAIN);
        for (String additional : stores.getAdditional()) {
            all.put(additional, additional);
        }
        dataStoreField.setOptionsMap(all);
    }

    @Subscribe("dataSetsTable.create")
    protected void onDataSetsCreate(Action.ActionPerformedEvent event) {
        BandDefinition selectedBand = bandsDc.getItem();

        DataSet dataset = dataSetFactory.createEmptyDataSet(selectedBand);
        selectedBand.getDataSets().add(dataset);
        dataSetsDc.getMutableItems().add(dataset);
        dataSetsDc.setItem(dataset);
        dataSetsTable.setSelected(dataset);
    }

    @Install(to = "dataSetsTable.create", subject = "enabledRule")
    protected boolean dataSetsCreateEnabledRule() {
        return isUpdatePermitted();
    }

    protected void initActions() {
        editFetchPlanAction = (EditFetchPlanAction) actions.create(EditFetchPlanAction.ID);
        editFetchPlanAction.setDataSetsTable(dataSetsTable);
        editFetchPlanAction.setBandsDc(bandsDc);
        fetchPlanEditButton.setAction(editFetchPlanAction);

        fetchPlanNameField.setOptionsMap(new HashMap<>());

        entitiesParamField.setEnterPressHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDc, "listEntitiesParamName"));
        entityParamField.setEnterPressHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDc, "entityParamName"));
        fetchPlanNameField.setEnterPressHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDc, "fetchPlanName"));
    }

    @Subscribe(id = "parametersDc", target = Target.DATA_CONTAINER)
    protected void onParametersDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportInputParameter> event) {
        Map<String, String> paramAliases = new HashMap<>();

        for (ReportInputParameter item : event.getSource().getItems()) {
            paramAliases.put(item.getName(), item.getAlias());
        }
        BiMap<String, String> biMap = ImmutableBiMap.copyOf(paramAliases);

        entitiesParamField.setOptions(new MapOptions<>(biMap));
        entitiesParamField.setOptionCaptionProvider(o -> biMap.inverse().getOrDefault(o, o));

        entityParamField.setOptions(new MapOptions<>(biMap));
        entityParamField.setOptionCaptionProvider(o -> biMap.inverse().getOrDefault(o, o));
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<BandDefinition> event) {
        if ("name".equals(event.getProperty()) && StringUtils.isBlank((String) event.getValue())) {
            event.getItem().setName("*");
        }
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemChange(InstanceContainer.ItemChangeEvent<BandDefinition> event) {
        BandDefinition item = event.getItem();
        nameField.setEditable((item == null || item.getParent() != null) && isUpdatePermitted());
        updateRequiredIndicators(item);
        selectFirstDataSet();
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcItemChange(InstanceContainer.ItemChangeEvent<DataSet> event) {
        DataSet dataSet = event.getItem();

        if (dataSet != null) {
            applyVisibilityRules(event.getItem());

            if (dataSet.getType() == DataSetType.SINGLE) {
                refreshFetchPlanNames(findParameterByAlias(dataSet.getEntityParamName()));
            } else if (dataSet.getType() == DataSetType.MULTI) {
                refreshFetchPlanNames(findParameterByAlias(dataSet.getListEntitiesParamName()));
            }

            dataSetScriptField.resetEditHistory();
        } else {
            hideAllDataSetEditComponents();
        }
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<DataSet> event) {
        applyVisibilityRules(event.getItem());
        if ("entityParamName".equals(event.getProperty()) || "listEntitiesParamName".equals(event.getProperty())) {
            ReportInputParameter linkedParameter = findParameterByAlias(String.valueOf(event.getValue()));
            refreshFetchPlanNames(linkedParameter);
        }

        if ("processTemplate".equals(event.getProperty())) {
            applyVisibilityRulesForType(event.getItem());
        }
    }

    protected void initDataSetListeners() {
        tabOrientationTableDecorator.decorate(dataSetsTable, dataSetsDc, bandsDc);
        dataSetScriptField.resetEditHistory();
        hideAllDataSetEditComponents();
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected void updateRequiredIndicators(@Nullable BandDefinition item) {
        boolean required = !(item == null || reportDc.getItem().getRootBandDefinition().equals(item));
        parentBandField.setRequired(required);
        parentBandField.setNullOptionVisible(!required);
        orientationField.setRequired(required);
        nameField.setRequired(item != null);
    }

    @Nullable
    protected ReportInputParameter findParameterByAlias(String alias) {
        for (ReportInputParameter reportInputParameter : parametersDc.getItems()) {
            if (reportInputParameter.getAlias().equals(alias)) {
                return reportInputParameter;
            }
        }
        return null;
    }

    protected void refreshFetchPlanNames(@Nullable ReportInputParameter reportInputParameter) {
        if (reportInputParameter != null) {
            if (StringUtils.isNotBlank(reportInputParameter.getEntityMetaClass())) {
                MetaClass parameterMetaClass = metadata.getClass(reportInputParameter.getEntityMetaClass());
                Collection<String> fetchPlanNames = fetchPlanRepository.getFetchPlanNames(parameterMetaClass);
                Map<String, String> fetchPlans = new HashMap<>();
                for (String fetchPlanName : fetchPlanNames) {
                    fetchPlans.put(fetchPlanName, fetchPlanName);
                }
                fetchPlans.put(FetchPlan.LOCAL, FetchPlan.LOCAL);
                fetchPlans.put(FetchPlan.INSTANCE_NAME, FetchPlan.INSTANCE_NAME);
                fetchPlans.put(FetchPlan.BASE, FetchPlan.BASE);
                fetchPlanNameField.setOptionsMap(fetchPlans);
                fetchPlanNameField.setValue(FetchPlan.BASE);
                return;
            }
        }

        fetchPlanNameField.setOptionsMap(new HashMap<>());
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
                    textParamsBox.add(dataStoreField);
                    dataSetScriptBox.add(isProcessTemplateField);
                    editPane.add(dataSetScriptBox);
                    break;
                case GROOVY:
                    editPane.add(dataSetScriptBox);
                    break;
                case SINGLE:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisibility(true, false);
                    editPane.add(spacerLabel);
                    editPane.expand(spacerLabel);
                    break;
                case MULTI:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisibility(false, true);
                    editPane.add(spacerLabel);
                    editPane.expand(spacerLabel);
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
                            dialogs.createMessageDialog()
                                    .withCaption(messageBundle.getMessage("dataSet.text"))
                                    .withMessage(messageBundle.getMessage("dataSet.textHelp"))
                                    .withModal(false)
                                    .withWidth("700px")
                                    .withContentMode(ContentMode.HTML)
                                    .show());
                    break;

                case JPQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(isProcessTemplateField.isChecked() ? null : this);
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
        commonEntityGrid.remove(fetchPlanNameLabel);
        commonEntityGrid.remove(fetchPlanNameField);
        commonEntityGrid.remove(fetchPlanEditButton);
        commonEntityGrid.remove(buttonEmptyElement);
        commonEntityGrid.remove(isUseExistingFetchPlanField);
        commonEntityGrid.remove(checkboxEmptyElement);

        if (Boolean.TRUE.equals(item.getUseExistingFetchPLan())) {
            commonEntityGrid.add(fetchPlanNameLabel);
            commonEntityGrid.add(fetchPlanNameField);
        } else {
            commonEntityGrid.add(fetchPlanEditButton);
            commonEntityGrid.add(buttonEmptyElement);
        }

        commonEntityGrid.add(isUseExistingFetchPlanField);
        commonEntityGrid.add(checkboxEmptyElement);
    }

    protected void hideAllDataSetEditComponents() {
        // do not use setVisible(false) due to web legacy (Vaadin 6) layout problems #PL-3916
        textParamsBox.remove(dataStoreField);
        dataSetScriptBox.remove(isProcessTemplateField);
        editPane.remove(dataSetScriptBox);
        editPane.remove(commonEntityGrid);
        editPane.remove(jsonDataSetTypeVBox);
        editPane.remove(spacerLabel);
    }

    protected void selectFirstDataSet() {
        if (!dataSetsDc.getItems().isEmpty()) {
            DataSet item = dataSetsDc.getItems().iterator().next();
            dataSetsTable.setSelected(item);
        } else {
            dataSetsTable.setSelected((DataSet) null);
        }
    }

    // This is a stub for using set in some DataSet change listener
    protected void setFetchPlanEditVisibility(DataSet dataSet) {
        if (isFetchPlanEditAllowed(dataSet)) {
            fetchPlanEditButton.setVisible(true);
        } else {
            fetchPlanEditButton.setVisible(false);
        }
    }

    protected boolean isFetchPlanEditAllowed(DataSet dataSet) {
        return true;
    }

    protected void setCommonEntityGridVisibility(boolean visibleEntityGrid, boolean visibleEntitiesGrid) {
        entityParamLabel.setVisible(visibleEntityGrid);
        entityParamField.setVisible(visibleEntityGrid);
        entitiesParamLabel.setVisible(visibleEntitiesGrid);
        entitiesParamField.setVisible(visibleEntitiesGrid);
    }
}
