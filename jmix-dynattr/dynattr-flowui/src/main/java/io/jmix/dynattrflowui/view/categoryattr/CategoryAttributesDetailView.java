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

package io.jmix.dynattrflowui.view.categoryattr;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.suggestion.QuerySuggestions;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.OptionsLoaderType;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattrflowui.impl.model.TargetViewComponent;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationViewFragment;
import io.jmix.flowui.*;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.action.view.LookupSelectAction;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.dynattr.AttributeType.*;
import static io.jmix.dynattr.OptionsLoaderType.*;
import static java.lang.String.format;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_CategoryAttribute.edit")
@ViewDescriptor("category-attributes-detail-view.xml")
@Route(value = "dynat/category/:id/attributes/:id", layout = DefaultMainViewParent.class)
@PrimaryDetailView(CategoryAttribute.class)
@EditedEntityContainer("categoryAttributeDc")
@DialogMode(width = "50em", height = "37.5em") // todo forceDialog = true
public class CategoryAttributesDetailView extends StandardDetailView<CategoryAttribute> {

    protected static final String DATA_TYPE_PROPERTY = "dataType";
    protected static final String DEFAULT_DATE_IS_CURRENT_PROPERTY = "defaultDateIsCurrent";
    protected static final String ENTITY_CLASS_PROPERTY = "entityClass";
    protected static final String JOIN_CLAUSE_PROPERTY = "joinClause";
    protected static final String LOOKUP_PROPERTY = "lookup";
    protected static final String NAME_PROPERTY = "name";
    protected static final String SCREEN_PROPERTY = "screen";
    protected static final String WHERE_CLAUSE_PROPERTY = "whereClause";

    protected static final String MAIN_TAB_NAME = "mainTab";
    protected static final String ONE_COLUMN_WIDTH = "630px";
    protected static final String TWO_COLUMNS_WIDTH = "854px";
    protected static final String MESSAGE_DIALOG_WIDTH = "560px";

    protected static final String CONFIGURATION_NUMBER_FORMAT_PATTERN_PROPERTY = "numberFormatPattern";
    protected static final String CONFIGURATION_OPTIONS_LOADER_TYPE_PROPERTY = "optionsLoaderType";

    protected static final Multimap<AttributeType, String> FIELDS_VISIBLE_FOR_TYPES = ArrayListMultimap.create();
    protected static final Set<AttributeType> SUPPORTED_OPTIONS_TYPES = ImmutableSet.of(STRING, DOUBLE, DECIMAL, INTEGER, ENTITY);

    protected static final String JPQL_WHERE = "where";

    static {
        FIELDS_VISIBLE_FOR_TYPES.put(BOOLEAN, "defaultBooleanField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "defaultStringField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "rowsCountField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "defaultDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "minDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "maxDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "defaultDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "minDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "maxDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "numberFormatPatternField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "defaultIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "minIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "maxIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "defaultDateField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "defaultDateIsCurrentField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "defaultDateWithoutTimeField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "defaultDateIsCurrentField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "enumerationBox");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "enumerationField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "editEnumerationBtn");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "defaultStringField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "entityClassField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "screenField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "defaultEntityIdField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "widthField");
    }

    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Messages messages;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ViewSupport viewSupport;
    @Autowired
    protected Views views;
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;
    //    @Autowired
//    protected JpqlUiSuggestionProvider jpqlUiSuggestionProvider;
    @Autowired
    protected AccessManager accessManager;

    @ViewComponent
    protected JmixCheckbox lookupField;
    @ViewComponent
    protected TypedDateTimePicker<Date> defaultDateField;
    @ViewComponent
    protected TypedDateTimePicker<LocalDate> defaultDateWithoutTimeField;
    @ViewComponent
    protected FormLayout optionalAttributeForm;
    @ViewComponent
    protected JmixComboBox<AttributeType> dataTypeField;
    @ViewComponent
    protected JmixComboBox<String> entityClassField;
    @ViewComponent
    protected JmixComboBox<String> screenField;
    @ViewComponent
    protected JmixComboBox<Boolean> defaultBooleanField;
    @ViewComponent
    protected JmixComboBox<OptionsLoaderType> optionsLoaderTypeField;
    @ViewComponent
    protected EntityPicker<Object> defaultEntityIdField;
    @ViewComponent
    protected CodeEditor optionsLoaderScriptField;
    @ViewComponent
    protected CodeEditor joinClauseField;
    @ViewComponent
    protected CodeEditor whereClauseField;
    @ViewComponent
    protected CodeEditor validationScriptField;
    @ViewComponent
    protected CodeEditor recalculationScriptField;
    @ViewComponent
    protected DataGrid<TargetViewComponent> targetScreensTable;
    @ViewComponent
    protected TabSheet tabSheet;
    @ViewComponent
    protected TypedTextField<String> codeField;
    @ViewComponent
    protected TypedTextField<BigDecimal> defaultDecimalField;
    @ViewComponent
    protected TypedTextField<BigDecimal> minDecimalField;
    @ViewComponent
    protected TypedTextField<BigDecimal> maxDecimalField;
    @ViewComponent
    protected JmixMultiSelectComboBox<CategoryAttribute> dependsOnAttributesField;
    @ViewComponent
    protected CollectionContainer<TargetViewComponent> targetScreensDc;
    @ViewComponent
    protected InstanceContainer<CategoryAttributeConfiguration> configurationDc;

    @ViewComponent("dependsOnAttributesField.clear")
    private ValueClearAction dependsOnAttributesFieldClear;
    @ViewComponent("dependsOnAttributesField.select")
    private LookupSelectAction dependsOnAttributesFieldSelect;
    @ViewComponent
    private JmixButton editEnumerationBtn;

    protected AttributeLocalizationViewFragment localizationFragment;

    protected List<TargetViewComponent> targetScreens = new ArrayList<>();


    @Subscribe
    protected void onInit(InitEvent event) {
        initAttributeForm();
        initCalculatedValuesAndOptionsForm();
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initTargetScreensTable();
    }

    @Subscribe
    protected void onAfterShow(BeforeShowEvent event) { //todo
        initCategoryAttributeConfigurationField();
        initLocalizationTab();
        initDependsOnAttributesField();

        setupNumberFormat();
        refreshAttributesUI();
        setupFieldsLock();
    }

    @Subscribe("tabSheet")
    protected void onTabSheetSelectedTabChange(TabSheet.SelectedChangeEvent event) {
        String tabName = event.getSelectedTab().getId().orElseThrow();
        String dialogWidth;
        if (MAIN_TAB_NAME.equals(tabName) && getEditedEntity().getDataType() != null) {
            dialogWidth = TWO_COLUMNS_WIDTH;
        } else {
            dialogWidth = ONE_COLUMN_WIDTH;
        }
    }

    @Subscribe("defaultEntityIdField")
    protected void onDefaultEntityIdFieldValueChange(ValueSource.ValueChangeEvent<Object> event) {
        Object entity = event.getValue();
        Object objectDefaultEntityId = null;
        if (entity != null) {
            objectDefaultEntityId = referenceToEntitySupport.getReferenceId(entity);
        }

        getEditedEntity().setObjectDefaultEntityId(objectDefaultEntityId);
    }

    @Subscribe(id = "categoryAttributeDc", target = Target.DATA_CONTAINER)
    protected void onCategoryAttributeDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<CategoryAttribute> event) {
        String property = event.getProperty();
        if (DATA_TYPE_PROPERTY.equals(property)
                || LOOKUP_PROPERTY.equals(property)
                || DEFAULT_DATE_IS_CURRENT_PROPERTY.equals(property)
                || ENTITY_CLASS_PROPERTY.equals(property)) {
            refreshAttributesUI();
            refreshAttributesValues();
        }

        if (event.getPrevValue() == null
                && DATA_TYPE_PROPERTY.equals(property)) {
        }

        if (NAME_PROPERTY.equals(property)) {
            refreshCodeFieldValue();
        }

        if (SCREEN_PROPERTY.equals(property)
                || JOIN_CLAUSE_PROPERTY.equals(property)
                || WHERE_CLAUSE_PROPERTY.equals(property)) {
            // todo: filter support FilteringLookupAction
            //dynamicAttributesGuiTools.initEntityPickerField(defaultEntityIdField, e.getItem());
        }
    }

    @Subscribe(id = "configurationDc", target = Target.DATA_CONTAINER)
    protected void onConfigurationDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<CategoryAttributeConfiguration> event) {
        String property = event.getProperty();
        if (CONFIGURATION_NUMBER_FORMAT_PATTERN_PROPERTY.equals(property)) {
            setupNumberFormat();
        }

        if (CONFIGURATION_OPTIONS_LOADER_TYPE_PROPERTY.equals(property)) {
            refreshAttributesUI();
            refreshAttributesValues();
        }
    }

    @Subscribe("editEnumerationBtn")
    protected void onEditEnumerationBtnClick(ClickEvent<Button> event) {
        DialogWindow<AttributeEnumerationViewFragment> enumerationScreen = dialogWindows.view(this, AttributeEnumerationViewFragment.class)
                .withViewClass(AttributeEnumerationViewFragment.class)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        AttributeEnumerationViewFragment screen = afterCloseEvent.getSource().getView();
                        getEditedEntity().setEnumeration(screen.getEnumeration());
                        getEditedEntity().setEnumerationLocales(screen.getEnumerationLocales());
                    }
                })
                .build();

        enumerationScreen.getView().setEnumeration(getEditedEntity().getEnumeration());
        enumerationScreen.getView().setEnumerationLocales(getEditedEntity().getEnumerationLocales());
        enumerationScreen.open();
    }

    @Install(to = "targetScreensDl", target = Target.DATA_LOADER)
    protected List<TargetViewComponent> targetScreensDlLoadDelegate(LoadContext<TargetViewComponent> loadContext) {
        return targetScreens;
    }

    @Subscribe("targetScreensTable.create")
    protected void onTargetScreensTableCreate(ActionPerformedEvent event) {
        targetScreensDc.getMutableItems().add(metadata.create(TargetViewComponent.class));
    }

    @Install(to = "dependsOnAttributesField", subject = "validator")
    protected void dependsOnAttributesFieldValidator(Collection<CategoryAttribute> categoryAttributes) {
        if (recalculationScriptField.getValue() != null
                && CollectionUtils.isEmpty(categoryAttributes)) {
            throw new ValidationException(
                    messages.getMessage(CategoryAttributesDetailView.class, "dependsOnAttributes.validationMsg"));
        }
    }

    protected void initAttributeForm() {
        ComponentUtils.setItemsMap(defaultBooleanField, getBooleanOptions());
        ComponentUtils.setItemsMap(dataTypeField, getDataTypeOptions());
        ComponentUtils.setItemsMap(entityClassField, getEntityOptions());
//        validationScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
//                messages.getMessage(CategoryAttrsEdit.class, "validationScript"),
//                messages.getMessage(CategoryAttrsEdit.class, "validationScriptHelp")
//        ));
    }

    protected void initCalculatedValuesAndOptionsForm() {
//        recalculationScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
//                messages.getMessage(CategoryAttrsEdit.class, "recalculationScript"),
//                messages.getMessage(CategoryAttrsEdit.class, "recalculationScriptHelp")
//        ));

//    todo https://github.com/jmix-framework/jmix/issues/1678    whereClauseField.setSuggester((source, text, cursorPosition) -> requestHint(whereClauseField, cursorPosition));
//        joinClauseField.setSuggester((source, text, cursorPosition) -> requestHint(joinClauseField, cursorPosition));
    }

    protected void initTargetScreensTable() {
        loadTargetScreens();

        Category category = getEditedEntity().getCategory();
        if (category != null) {
            MetaClass categorizedEntityMetaClass = metadata.findClass(getEditedEntity().getCategory().getEntityType());
//            Map<String, String> availableScreensMap = categorizedEntityMetaClass != null ?
//                    new HashMap<>(screensHelper.getAvailableScreens(categorizedEntityMetaClass.getJavaClass(),
//                            Collections.singletonList(DynAttrFacet.FACET_NAME), true)) : new HashMap<>();

//        todo    targetScreensTable.addGeneratedColumn(
//                    "screen",
//                    entity -> {
//                        ComboBox<String> screenField = uiComponents.create(ComboBox.class);
//                        screenField.setValueSource(new ContainerValueSource<>(targetScreensTable.getInstanceContainer(entity), "screen"));
//                        screenField.setOptionsMap(availableScreensMap);
//                        screenField.setEnterPressHandler(enterPressEvent -> {
//                            String text = enterPressEvent.getText();
//                            if (!availableScreensMap.containsKey(text)) {
//                                availableScreensMap.put(text, text);
//                                screenField.setValue(text);
//                            }
//                        });
//                        screenField.setRequired(true);
//                        screenField.setWidth("100%");
//                        return screenField;
//                    }
//            );
        }
    }

    protected void loadTargetScreens() {
        targetScreens.clear();
        Set<String> targetScreensSet = getEditedEntity().getTargetScreensSet();
        for (String targetScreen : targetScreensSet) {
            TargetViewComponent targetViewComponent = metadata.create(TargetViewComponent.class);
            String screen;
            String component = null;

            if (targetScreen.contains("#")) {
                String[] split = targetScreen.split("#");
                screen = split[0];
                component = split[1];
            } else {
                screen = targetScreen;
            }

            targetViewComponent.setScreen(screen);
            targetViewComponent.setComponent(component);

            targetScreens.add(targetViewComponent);
        }
    }

    protected void initCategoryAttributeConfigurationField() {
        CategoryAttribute attribute = getEditedEntity();
        CategoryAttributeConfiguration configuration = attribute.getConfiguration();

        if (ENTITY.equals(attribute.getDataType())
                && Boolean.TRUE.equals(attribute.getLookup())
                && configuration.getOptionsLoaderType() == null) {
            optionsLoaderTypeField.setValue(JPQL);
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            Tab localizationTab = tabSheet.getTabAt(0); // 0 == "localizationTab"
            localizationTab.setVisible(true);

            CrudEntityContext crudEntityContext = new CrudEntityContext(configurationDc.getEntityMetaClass());
            accessManager.applyRegisteredConstraints(crudEntityContext);

            VerticalLayout localizationTabComponent = (VerticalLayout) tabSheet.getComponent(localizationTab);
            localizationFragment = views.create(AttributeLocalizationViewFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getNameMsgBundle());
            localizationFragment.setDescriptionMsgBundle(getEditedEntity().getDescriptionsMsgBundle());
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

            localizationTabComponent.add(localizationFragment);
        }
    }

    @SuppressWarnings("unchecked")
    protected void initDependsOnAttributesField() {
//        ValuesSelectAction<CategoryAttribute> selectAction =
//                (ValuesSelectAction<CategoryAttribute>) dependsOnAttributesField.getActionNN("select");
//        selectAction.setOptions(new ListEntityOptions<>(getAttributesOptions(), metadata));
//
//        if (getEditedEntity().getConfiguration() != null
//                && getEditedEntity().getConfiguration().getDependsOnAttributeCodes() != null) {
//            dependsOnAttributesField.setValue(getAttributesOptions().stream().filter(o ->
//                    getEditedEntity().getConfiguration().getDependsOnAttributeCodes().contains(o.getCode()))
//                    .collect(Collectors.toList()));
//        }
//        CrudEntityContext crudEntityContext = new CrudEntityContext(configurationDc.getEntityMetaClass());
//        accessManager.applyRegisteredConstraints(crudEntityContext);
//        if (!crudEntityContext.isUpdatePermitted()) {
//            dependsOnAttributesField.setEnabled(false);
//            dependsOnAttributesFieldClear.setEnabled(false);
//            dependsOnAttributesFieldSelect.setEnabled(false);
//        }
    }

    protected void setupNumberFormat() {
        String formatPattern = getEditedEntity().getConfiguration().getNumberFormatPattern();
        Datatype datatype;
        if (!Strings.isNullOrEmpty(formatPattern)) {
            datatype = new AdaptiveNumberDatatype(BigDecimal.class, formatPattern, "", "",
                    formatStringsRegistry);
        } else {
            datatype = datatypeRegistry.find(BigDecimal.class);
        }

        defaultDecimalField.setDatatype(datatype);
        minDecimalField.setDatatype(datatype);
        maxDecimalField.setDatatype(datatype);

        defaultDecimalField.setValue(defaultDecimalField.getValue());
        minDecimalField.setValue(minDecimalField.getValue());
        maxDecimalField.setValue(maxDecimalField.getValue());
    }

    protected void refreshAttributesUI() {
        CategoryAttribute categoryAttribute = getEditedEntity();
        CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();

        AttributeType attributeType = dataTypeField.getValue();
        Collection<String> visibleFields = FIELDS_VISIBLE_FOR_TYPES.get(attributeType);
        for (Component component : optionalAttributeForm.getChildren().toList()) {
            boolean visible = visibleFields.contains(component.getId());
            component.setVisible(visible);

            if (!visible && component instanceof HasValue) {
                ((HasValue) component).clear();
            }
        }

        if (MAIN_TAB_NAME.equals(tabSheet.getSelectedTab().getId()) && !visibleFields.isEmpty()) {
            optionalAttributeForm.setVisible(true);
        }

        if (ENTITY.equals(attributeType)) {
            if (!Strings.isNullOrEmpty(entityClassField.getValue())) {
                Class<?> javaClass = categoryAttribute.getJavaType();

                if (javaClass != null) {
                    defaultEntityIdField.setMetaClass(metadata.getClass(javaClass));
                    // todo: filter support FilteringLookupAction
                    //todo dynamicAttributesGuiTools.initEntityPickerField(defaultEntityId, attribute);
//                   todo screenField.setOptionsMap(screensHelper.getAvailableBrowserScreens(javaClass));
                    refreshDefaultEntityIdFieldValue();
                }
            } else {
                defaultEntityIdField.setEnabled(false);
            }
            screenField.setVisible(!lookupField.getValue());
        }

        if (DATE.equals(attributeType)) {
            defaultDateField.setVisible(!Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent()));
        }

        if (DATE_WITHOUT_TIME.equals(attributeType)) {
            defaultDateWithoutTimeField.setVisible(!Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent()));
        }

        OptionsLoaderType optionsType = configuration.getOptionsLoaderType();

        boolean jpqlLoaderVisible = optionsType == JPQL;
        joinClauseField.setVisible(jpqlLoaderVisible);
        whereClauseField.setVisible(jpqlLoaderVisible);

        boolean scriptLoaderVisible = optionsType == SQL
                || optionsType == GROOVY;
        optionsLoaderScriptField.setVisible(scriptLoaderVisible);

//     todo   if (optionsType == GROOVY) {
//            optionsLoaderScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
//                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderGroovyScript"),
//                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderGroovyScriptHelp")));
//            optionsLoaderScriptField.setMode(SourceCodeEditor.Mode.Groovy);
//        } else if (optionsType == SQL) {
//            optionsLoaderScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
//                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderSqlScript"),
//                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderSqlScriptHelp")));
//            optionsLoaderScriptField.setMode(SourceCodeEditor.Mode.SQL);
//        } else if (optionsType == JPQL) {
//            joinClauseField.setContextHelpIconClickHandler(e -> showMessageDialog(
//                    messages.getMessage(CategoryAttrsEdit.class, "joinClause"),
//                    messages.getMessage(CategoryAttrsEdit.class, "joinClauseHelp")));
//            whereClauseField.setContextHelpIconClickHandler(e -> showMessageDialog(
//                    messages.getMessage(CategoryAttrsEdit.class, "whereClause"),
//                    messages.getMessage(CategoryAttrsEdit.class, "whereClauseHelp")));
//        } else {
//            optionsLoaderScriptField.setContextHelpIconClickHandler(null);
//            optionsLoaderScriptField.setMode(SourceCodeEditor.Mode.Text);
//        }

        optionsLoaderTypeField.setEnabled(Boolean.TRUE.equals(categoryAttribute.getLookup()));
        optionsLoaderTypeField.setRequired(Boolean.TRUE.equals(categoryAttribute.getLookup()));
        ComponentUtils.setItemsMap(optionsLoaderTypeField, getLoaderOptions());
    }

    @Subscribe("screenField")
    public void onScreenFieldValueChange(HasValue.ValueChangeEvent event) {
        if (Strings.isNullOrEmpty(screenField.getValue())) {
            this.defaultEntityIdField.setEnabled(false);
        } else {
            this.defaultEntityIdField.setEnabled(true);
        }
    }

    @Subscribe("defaultEntityIdField.lookup")
    public void onDefaultEntityIdFieldLookup(ActionPerformedEvent event) {
        LookupWindowBuilder<Object, View<?>> lookup = dialogWindows.lookup(defaultEntityIdField);
        if (!Strings.isNullOrEmpty(screenField.getValue())) {
            lookup.withViewId(screenField.getValue());
        }
        try {
            lookup
                    .build()
                    .open();
        } catch (AccessDeniedException ex) {
            notifications.create(messages.getMessage(CategoryAttributesDetailView.class, "entityScreenAccessDeniedMessage"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(configurationDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            defaultEntityIdField.setEnabled(false);
            editEnumerationBtn.setEnabled(false);
        }
    }

    protected void refreshAttributesValues() {
        AttributeType attributeType = dataTypeField.getValue();
        CategoryAttribute categoryAttribute = getEditedEntity();
        CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();

        if (ENTITY.equals(attributeType)) {
            if (!Strings.isNullOrEmpty(categoryAttribute.getEntityClass())) {
//           todo     Map<String, String> options = ((MapOptions<String>) screenField.getDataProvider()).getItemsCollection();
//                categoryAttribute.setScreen(options.containsValue(categoryAttribute.getScreen()) ? categoryAttribute.getScreen() : null);
            }
            if (configuration.getOptionsLoaderType() == SQL) {
                configuration.setOptionsLoaderType(JPQL);
            }
        } else if (configuration.getOptionsLoaderType() == JPQL) {
            configuration.setOptionsLoaderType(null);
        }

        if (DATE.equals(attributeType)) {
            if (Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent())) {
                categoryAttribute.setDefaultDate(null);
            }
        }

        if (DATE_WITHOUT_TIME.equals(attributeType)) {
            if (Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent())) {
                categoryAttribute.setDefaultDateWithoutTime(null);
            }
        }

        if (BOOLEAN.equals(attributeType)) {
            categoryAttribute.setIsCollection(null);
        }

        if (categoryAttribute.getDataType() == null
                || !SUPPORTED_OPTIONS_TYPES.contains(categoryAttribute.getDataType())) {
            categoryAttribute.setLookup(false);
        }

        if (!Boolean.TRUE.equals(categoryAttribute.getLookup())) {
            configuration.setOptionsLoaderType(null);
            configuration.setOptionsLoaderScript(null);
            categoryAttribute.setWhereClause(null);
            categoryAttribute.setJoinClause(null);
        } else {
            OptionsLoaderType optionsType = configuration.getOptionsLoaderType();
            if (optionsType == JPQL) {
                configuration.setOptionsLoaderScript(null);
            } else if (optionsType == GROOVY || optionsType == SQL) {
                categoryAttribute.setWhereClause(null);
                categoryAttribute.setJoinClause(null);
            } else if (optionsType == null) {
                configuration.setOptionsLoaderScript(null);
                categoryAttribute.setWhereClause(null);
                categoryAttribute.setJoinClause(null);
                if (categoryAttribute.getDataType() == ENTITY) {
                    configuration.setOptionsLoaderType(JPQL);
                }
            }
        }
    }

    protected void refreshDefaultEntityIdFieldValue() {
        CategoryAttribute attribute = getEditedEntity();
        Class<?> javaClass = getEditedEntity().getJavaType();
        if (javaClass != null) {
            MetaClass metaClass = metadata.getClass(javaClass);
            if (attribute.getObjectDefaultEntityId() != null) {
                LoadContext<?> lc = new LoadContext<>(metadata.getClass(attribute.getJavaType()));
                FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.INSTANCE_NAME);
                lc.setFetchPlan(fetchPlan);
                String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
                lc.setQueryString(format("select e from %s e where e.%s = :entityId", metaClass.getName(), pkName))
                        .setParameter("entityId", attribute.getObjectDefaultEntityId());
                Object entity = dataManager.load(lc);
                if (entity != null) {
                    defaultEntityIdField.setValue(entity);
                } else {
                    defaultEntityIdField.setValue(null);
                }
            }
        }
    }

    protected void refreshCodeFieldValue() {
        CategoryAttribute attribute = getEditedEntity();
        if (Strings.isNullOrEmpty(attribute.getCode()) && !Strings.isNullOrEmpty(attribute.getName())) {
            String categoryName = StringUtils.EMPTY;
            if (attribute.getCategory() != null) {
                categoryName = StringUtils.defaultString(attribute.getCategory().getName());
            }
            codeField.setValue(StringUtils.deleteWhitespace(categoryName + attribute.getName()));
        }
    }

    protected Map<Boolean, String> getBooleanOptions() {
        Map<Boolean, String> booleanOptions = new TreeMap<>();
        booleanOptions.put(Boolean.TRUE, messages.getMessage("trueString"));
        booleanOptions.put(Boolean.FALSE, messages.getMessage("falseString"));
        return booleanOptions;
    }

    protected Map<AttributeType, String> getDataTypeOptions() {
        Map<AttributeType, String> options = new TreeMap<>();
        AttributeType[] types = AttributeType.values();
        for (AttributeType attributeType : types) {
            String key = AttributeType.class.getSimpleName() + "." + attributeType.toString();
            options.put(attributeType, messages.getMessage(AttributeType.class, key));
        }
        return options;
    }

    protected Map<String, String> getEntityOptions() {
        Map<String, String> optionsMap = new TreeMap<>();
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (!metadataTools.isSystemLevel(metaClass)) {
                if (metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
                    continue;
                }
                optionsMap.put(messageTools.getDetailedEntityCaption(metaClass), metaClass.getJavaClass().getName());
            }
        }

        return optionsMap;
    }

    protected Map<OptionsLoaderType, String> getLoaderOptions() {
        CategoryAttribute attribute = getEditedEntity();
        Map<OptionsLoaderType, String> options = new TreeMap<>();
        for (OptionsLoaderType type : OptionsLoaderType.values()) {
            if (attribute.getDataType() != ENTITY && type == JPQL) {
                continue;
            }
            if (attribute.getDataType() == ENTITY && type == SQL) {
                continue;
            }
            String key = OptionsLoaderType.class.getSimpleName() + "." + type.toString();
            options.put(type, messages.getMessage(OptionsLoaderType.class, key));
        }
        return options;
    }

    protected List<CategoryAttribute> getAttributesOptions() {
        List<CategoryAttribute> optionsList = new ArrayList<>();
        CategoryAttribute attribute = getEditedEntity();
        List<CategoryAttribute> categoryAttributes = attribute.getCategory().getCategoryAttrs();
        if (categoryAttributes != null) {
            optionsList.addAll(categoryAttributes);
            optionsList.remove(attribute);
            return optionsList;
        }
        return optionsList;
    }

    protected void showMessageDialog(String caption, String message) {
        dialogs.createMessageDialog()
                .withText(caption)
                .withContent(new Html(message))
                .withModal(false)
                .withWidth(MESSAGE_DIALOG_WIDTH)
                .open();
    }

    protected List<QuerySuggestions> requestHint(CodeEditor sender, int senderCursorPosition) {
        String joinStr = joinClauseField.getValue();
        String whereStr = whereClauseField.getValue();

        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        Class<?> javaClassForEntity = getEditedEntity().getJavaType();
        if (javaClassForEntity == null) {
            return new ArrayList<>();
        }

        String queryStart = format("select %s from %s %s ", entityAlias, metadata.getClass(javaClassForEntity), entityAlias);

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (sender == joinClauseField) {
                queryPosition = queryBuilder.length() + senderCursorPosition - 1;
            }
            if (!StringUtils.containsIgnoreCase(joinStr, "join") && !StringUtils.contains(joinStr, ",")) {
                queryBuilder.append("join ").append(joinStr);
                queryPosition += "join ".length();
            } else {
                queryBuilder.append(joinStr);
            }
        }
        if (StringUtils.isNotEmpty(whereStr)) {
            if (sender == whereClauseField) {
                queryPosition = queryBuilder.length() + JPQL_WHERE.length() + senderCursorPosition;
            }
            queryBuilder.append(JPQL_WHERE)
                    .append(" ")
                    .append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace("{E}", entityAlias);

//   todo     return jpqlUiSuggestionProvider.getSuggestions(query, queryPosition, sender.getAutoCompleteSupport());
        return new ArrayList<>();
    }

    @Subscribe
    protected void onValidation(ValidationEvent event) {
        ValidationErrors validationErrors = new ValidationErrors();
        CategoryAttribute attribute = getEditedEntity();
        AttributeType dataType = attribute.getDataType();
        CategoryAttributeConfiguration configuration = attribute.getConfiguration();

        if (INTEGER.equals(dataType)) {
            ValidationErrors errors = validateNumbers(
                    INTEGER,
                    configuration.getMinInt(),
                    configuration.getMaxInt(),
                    attribute.getDefaultInt()
            );
            validationErrors.addAll(errors);
        } else if (DOUBLE.equals(dataType)) {
            ValidationErrors errors = validateNumbers(
                    DOUBLE,
                    configuration.getMinDouble(),
                    configuration.getMaxDouble(),
                    attribute.getDefaultDouble()
            );
            validationErrors.addAll(errors);
        } else if (DECIMAL.equals(dataType)) {
            ValidationErrors errors = validateNumbers(
                    DECIMAL,
                    configuration.getMinDecimal(),
                    configuration.getMaxDecimal(),
                    attribute.getDefaultDecimal()
            );
            validationErrors.addAll(errors);
        } else if (ENUMERATION.equals(dataType)) {
            ValidationErrors errors = validateEnumeration(attribute.getEnumeration(), attribute.getDefaultString());
            validationErrors.addAll(errors);
        }

        Category category = getEditedEntity().getCategory();
        if (category != null && category.getCategoryAttrs() != null) {
            for (CategoryAttribute categoryAttribute : category.getCategoryAttrs()) {
                if (!categoryAttribute.equals(attribute)) {
                    if (categoryAttribute.getName().equals(attribute.getName())) {
                        validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "uniqueName"));
                        return;
                    } else if (categoryAttribute.getCode().equals(attribute.getCode())) {
                        validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "uniqueCode"));
                        return;
                    }
                }
            }
        }

        event.addErrors(validationErrors);
    }

    protected ValidationErrors validateEnumeration(String enumeration, String defaultValue) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (enumeration == null) {
            validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "enumerationField.required"));
        } else if (defaultValue != null) {
            if (Arrays.stream(enumeration.split(",")).noneMatch(defaultValue::equalsIgnoreCase))
                validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "defaultValueIsNotInEnumeration"));
        }
        return validationErrors;
    }

    protected ValidationErrors validateNumbers(AttributeType type, Number minNumber, Number maxNumber, Number defaultNumber) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (minNumber != null
                && maxNumber != null
                && compareNumbers(type, minNumber, maxNumber) > 0) {
            validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "minGreaterThanMax"));
        } else if (defaultNumber != null) {
            if (minNumber != null
                    && compareNumbers(type, minNumber, defaultNumber) > 0) {
                validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "defaultLessThanMin"));
            }

            if (maxNumber != null
                    && compareNumbers(type, maxNumber, defaultNumber) < 0) {
                validationErrors.add(messages.getMessage(CategoryAttributesDetailView.class, "defaultGreaterThanMax"));
            }
        }

        return validationErrors;
    }

    protected int compareNumbers(AttributeType type, Number first, Number second) {
        if (INTEGER.equals(type)) {
            return Integer.compare((Integer) first, (Integer) second);
        } else if (DOUBLE.equals(type)) {
            return Double.compare((Double) first, (Double) second);
        } else if (DECIMAL.equals(type)) {
            return ((BigDecimal) first).compareTo((BigDecimal) second);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(BeforeSaveEvent event) {
        preCommitLocalizationFields(event);
        preCommitTargetScreensField(event);
        preCommitConfiguration(event);
    }

    protected void preCommitLocalizationFields(BeforeSaveEvent event) {
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
            getEditedEntity().setLocaleDescriptions(localizationFragment.getDescriptionMsgBundle());
        }
    }

    protected void preCommitTargetScreensField(BeforeSaveEvent event) {
        CategoryAttribute attribute = getEditedEntity();
        StringBuilder stringBuilder = new StringBuilder();
        for (TargetViewComponent targetViewComponent : targetScreensDc.getItems()) {
            if (StringUtils.isNotBlank(targetViewComponent.getScreen())) {
                stringBuilder.append(targetViewComponent.getScreen());
                if (StringUtils.isNotBlank(targetViewComponent.getComponent())) {
                    stringBuilder.append("#");
                    stringBuilder.append(targetViewComponent.getComponent());
                }
                stringBuilder.append(",");
            }
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        attribute.setTargetScreens(stringBuilder.toString());
    }

    protected void preCommitConfiguration(BeforeSaveEvent event) {
        CategoryAttribute attribute = getEditedEntity();
        if (attribute.getConfiguration() != null) {
            if (dependsOnAttributesField.getValue() != null) {
                attribute.getConfiguration().setDependsOnAttributeCodes(
                        dependsOnAttributesField.getValue().stream()
                                .map(CategoryAttribute::getCode)
                                .collect(Collectors.toList()));
            } else {
                attribute.getConfiguration().setDependsOnAttributeCodes(Collections.emptyList());
            }
        }
        if (getViewData().getDataContext().isModified(attribute.getConfiguration())) {
            CategoryAttributeConfiguration configuration = configurationDc.getItemOrNull();
            if (configuration != null) {
                attribute.setConfiguration((CategoryAttributeConfiguration) configuration.clone());
                event.getDataContext().getModified().add(attribute);
            }
        }
    }
}
