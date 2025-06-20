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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattr.OptionsLoaderType;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattr.utils.DynAttrStringUtils;
import io.jmix.dynattrflowui.impl.DynAttrFacetInfo;
import io.jmix.dynattrflowui.impl.model.TargetViewComponent;
import io.jmix.dynattrflowui.utils.DataProviderUtils;
import io.jmix.dynattrflowui.utils.DynAttrUiHelper;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationComponent;
import io.jmix.flowui.*;
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.*;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.jmix.dynattr.AttributeType.*;
import static io.jmix.dynattr.OptionsLoaderType.*;
import static java.lang.String.format;

@ViewController("dynat_CategoryAttribute.detail")
@ViewDescriptor("category-attributes-detail-view.xml")
@Route(value = "dynat/category/:categoryId/attributes/:id", layout = DefaultMainViewParent.class)
@PrimaryDetailView(CategoryAttribute.class)
@EditedEntityContainer("categoryAttributeDc")
@DialogMode(minWidth = "60em", resizable = true)
public class CategoryAttributesDetailView extends StandardDetailView<CategoryAttribute> {

    public static final String CATEGORY_ID_ROUTE_PARAMETER = "categoryId";

    protected static final String DATA_TYPE_PROPERTY = "dataType";
    protected static final String DEFAULT_DATE_IS_CURRENT_PROPERTY = "defaultDateIsCurrent";
    protected static final String ENTITY_CLASS_PROPERTY = "entityClass";
    protected static final String LOOKUP_PROPERTY = "lookup";
    protected static final String NAME_PROPERTY = "name";

    protected static final String MAIN_TAB_NAME = "mainTab";

    protected static final String CONFIGURATION_NUMBER_FORMAT_PATTERN_PROPERTY = "numberFormatPattern";
    protected static final String CONFIGURATION_OPTIONS_LOADER_TYPE_PROPERTY = "optionsLoaderType";

    protected static final Multimap<AttributeType, String> FIELDS_VISIBLE_FOR_TYPES = ArrayListMultimap.create();
    protected static final Set<AttributeType> SUPPORTED_OPTIONS_TYPES = ImmutableSet.of(STRING, DOUBLE, DECIMAL, INTEGER, ENTITY);

    static {
        FIELDS_VISIBLE_FOR_TYPES.put(BOOLEAN, "defaultBooleanField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "defaultStringField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "rowsCountField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "defaultDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "minDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "maxDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "defaultDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "minDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "maxDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "numberFormatPatternField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "defaultIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "minIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "maxIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "defaultDateField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "defaultDateIsCurrentField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "defaultDateWithoutTimeField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "defaultDateIsCurrentField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "enumerationBox");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "enumerationField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "editEnumerationBtn");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "defaultEnumField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "entityClassField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "screenField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "defaultEntityIdField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "isCollectionField");
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
    protected DynAttrFacetInfo dynAttrFacetInfo;
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
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected MsgBundleTools msgBundleTools;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected DynAttrUiHelper dynAttrUiHelper;
    @Autowired
    protected EntityStates entityStates;

    @ViewComponent
    protected JmixCheckbox lookupField;
    @ViewComponent
    protected TypedDateTimePicker<Date> defaultDateField;
    @ViewComponent
    protected TypedDatePicker<Date> defaultDateWithoutTimeField;
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
    protected JmixComboBox<String> defaultEnumField;
    @ViewComponent
    protected JmixComboBox<OptionsLoaderType> optionsLoaderTypeField;
    @ViewComponent
    protected JmixValuePicker<Object> defaultEntityIdField;
    @ViewComponent
    protected CodeEditor validationScriptField;
    @ViewComponent
    protected CodeEditor optionsLoaderScriptField;
    @ViewComponent
    protected CodeEditor joinClauseField;
    @ViewComponent
    protected CodeEditor whereClauseField;
    @ViewComponent
    protected CodeEditor recalculationScriptField;
    @ViewComponent
    protected JmixTabSheet tabSheet;
    @ViewComponent
    protected TypedTextField<String> codeField;
    @ViewComponent
    protected TypedTextField<String> defaultStringField;
    @ViewComponent
    protected TypedTextField<BigDecimal> defaultDecimalField;
    @ViewComponent
    protected TypedTextField<BigDecimal> minDecimalField;
    @ViewComponent
    protected TypedTextField<BigDecimal> maxDecimalField;
    @ViewComponent
    protected JmixMultiSelectComboBoxPicker<CategoryAttribute> dependsOnAttributesField;
    @ViewComponent
    protected CollectionContainer<TargetViewComponent> targetScreensDc;
    @ViewComponent
    protected InstanceContainer<CategoryAttributeConfiguration> configurationDc;
    @ViewComponent
    protected CollectionLoader<TargetViewComponent> targetScreensDl;
    @ViewComponent
    protected InstanceContainer<CategoryAttribute> categoryAttributeDc;

    @ViewComponent("dependsOnAttributesField.clear")
    protected ValueClearAction<String> dependsOnAttributesFieldClear;
    @ViewComponent("dependsOnAttributesField.select")
    protected MultiValueSelectAction<String> dependsOnAttributesFieldSelect;
    @ViewComponent
    protected JmixButton editEnumerationBtn;

    protected boolean isRefreshing = false;
    protected AttributeLocalizationComponent localizationFragment;

    protected final List<String> defaultEnumValues = new ArrayList<>();
    protected List<TargetViewComponent> targetScreens = new ArrayList<>();

    protected UUID parentCategoryUuid;

    @Subscribe
    protected void onInit(InitEvent event) {
        initAttributeForm();
        initCalculatedValuesAndOptionsForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get(CATEGORY_ID_ROUTE_PARAMETER)
                .ifPresent(paramValue -> parentCategoryUuid = UUID.fromString(paramValue));

        super.beforeEnter(event);
    }

    @Override
    protected void processBeforeEnterInternal(BeforeEnterEvent event) {
        super.processBeforeEnterInternal(event);

        event.getRouteParameters().get(CATEGORY_ID_ROUTE_PARAMETER)
                .ifPresent(paramValue -> parentCategoryUuid = UUID.fromString(paramValue));
    }

    @Override
    protected void setupEntityToEdit(CategoryAttribute entityToEdit) {
        // do nothing
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<CategoryAttribute> event) {
        Category category = loadParentCategory();
        event.getEntity().setCategory(category);
    }

    protected Category loadParentCategory() {
        return dataManager.load(Id.of(parentCategoryUuid, Category.class))
                .one();
    }

    @Override
    public String getPageTitle() {
        return super.getPageTitle().formatted(metadataTools.getInstanceName(getEditedEntity().getCategory()));
    }

    protected void initDefaultEnumField() {
        defaultEnumValues.addAll(getEnumValues());
        defaultEnumField.setItems(defaultEnumValues);
        defaultEnumField.addValueChangeListener(e -> {
            getEditedEntity().setDefaultString(e.getValue());
        });
    }

    protected void initDefaultEnumFieldValue() {
        if (StringUtils.isNotBlank(getEditedEntity().getDefaultString())) {
            defaultEnumField.setValue(getEditedEntity().getDefaultString());
        }

        if (!StringUtils.isBlank(categoryAttributeDc.getItem().getDefaultString())) {
            defaultEnumField.getDataProvider().refreshAll();
            defaultEnumField.setValue(categoryAttributeDc.getItem().getDefaultString());
        }
    }

    protected List<String> getEnumValues() {
        if (StringUtils.isBlank(categoryAttributeDc.getItem().getEnumeration())) {
            return List.of();
        }
        Spliterator<String> enumSpliterator = Splitter.on(",")
                .omitEmptyStrings()
                .split(categoryAttributeDc.getItem().getEnumeration())
                .spliterator();
        return StreamSupport.stream(enumSpliterator, false).toList();
    }

    @Subscribe
    protected void onAfterShow(BeforeShowEvent event) {
        initDefaultEntityFieldId();
        initDefaultEnumField();
        if (isDataTypeEnum()) {
            initDefaultEnumFieldValue();
        }
        initCategoryAttributeConfigurationField();
        initLocalizationTab();
        initDependsOnAttributesField();

        setupNumberFormat();
        if (!isRefreshing) {
            isRefreshing = true;
            refreshAttributesUI();
            isRefreshing = false;
        }
        setupFieldsLock();

        entityClassField.setValue(getEditedEntity().getEntityClass());
        entityClassField.addValueChangeListener(e -> getEditedEntity().setEntityClass(e.getValue()));
        if (getEditedEntity().getEntityClass() != null) {
            Class<?> javaClass = getEditedEntity().getJavaType();
            MetaClass metaClass = metadata.getClass(javaClass);
            screenField.setItems(List.of(
                    viewRegistry.getListViewId(metaClass),
                    viewRegistry.getLookupViewId(metaClass)));
            screenField.setValue(getEditedEntity().getScreen());
        }
        tabSheet.addSelectedChangeListener(e -> refreshOnce());
        screenField.addValueChangeListener(e -> getEditedEntity().setScreen(e.getValue()));
        loadTargetViews();
    }

    protected void initDefaultEntityFieldId() {
        refreshDefaultEntityIdFieldValue();

        // add listener after setting initial value
        defaultEntityIdField.addValueChangeListener(this::onDefaultEntityIdFieldValueChange);
    }

    protected boolean isDataTypeEnum() {
        return getEditedEntity().getDataType() != null &&
                getEditedEntity().getDataType().equals(AttributeType.ENUMERATION);
    }

    @Install(to = "nameField", subject = "validator")
    protected void nameFieldUniqueNameValidator(String value) {
        validateUniqueStringOnAttribute(value, CategoryAttribute::getName, "notUniqueAttributeName");
    }

    @Install(to = "codeField", subject = "validator")
    protected void codeFieldUniqueNameValidator(String value) {
        validateUniqueStringOnAttribute(value, CategoryAttribute::getCode, "notUniqueAttributeCode");
    }

    protected void validateUniqueStringOnAttribute(String value,
                                                   Function<CategoryAttribute, String> mapper,
                                                   String messageKey) {
        if (categoryAttributeDc.getItem().getCategory() == null ||
                categoryAttributeDc.getItem().getCategory().getCategoryAttrs() == null) {
            return;
        }
        List<CategoryAttribute> attributes = categoryAttributeDc.getItem()
                .getCategory()
                .getCategoryAttrs();
        if (attributes.stream()
                .filter(item -> !Objects.equals(categoryAttributeDc.getItem(), item))
                .map(mapper)
                .anyMatch(attrName -> Objects.equals(attrName, value))) {
            throw new ValidationException(messages.getMessage(getClass(), messageKey));
        }
    }

    protected void onDefaultEntityIdFieldValueChange(JmixValuePicker.ValueChangeEvent<Object> event) {
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
            refreshOnce();
        }

        if (NAME_PROPERTY.equals(property)) {
            refreshCodeFieldValue();
        }
    }

    @Subscribe(id = "configurationDc", target = Target.DATA_CONTAINER)
    protected void onConfigurationDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<CategoryAttributeConfiguration> event) {
        String property = event.getProperty();
        if (CONFIGURATION_NUMBER_FORMAT_PATTERN_PROPERTY.equals(property)) {
            setupNumberFormat();
        }

        if (CONFIGURATION_OPTIONS_LOADER_TYPE_PROPERTY.equals(property)) {
            refreshOnce();
        }
    }

    protected void refreshOnce() {
        if (!isRefreshing) {
            isRefreshing = true;
            refreshAttributesUI();
            refreshAttributesValues();
            isRefreshing = false;
        }
    }

    @Subscribe("editEnumerationBtn")
    protected void onEditEnumerationBtnClick(ClickEvent<Button> event) {
        DialogWindow<AttributeEnumerationDetailView> enumerationScreen = dialogWindows.view(this, AttributeEnumerationDetailView.class)
                .withViewClass(AttributeEnumerationDetailView.class)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        AttributeEnumerationDetailView screen = afterCloseEvent.getSource().getView();
                        getEditedEntity().setEnumeration(screen.getEnumeration());
                        getEditedEntity().setEnumerationLocales(screen.getEnumerationLocales());

                        defaultEnumValues.clear();
                        defaultEnumValues.addAll(getEnumValues());
                        clearEnumValueIfCurrentItemAbsentOnEnum();
                        defaultEnumField.getDataProvider().refreshAll();
                    }
                })
                .build();

        // localizedEnumValuesDataGrid will be shown
        if (coreProperties.getAvailableLocales().size() > 1) {
            enumerationScreen.setWidth("65em");
        }
        enumerationScreen.getView().setEnumeration(getEditedEntity().getEnumeration());
        enumerationScreen.getView().setEnumerationLocales(getEditedEntity().getEnumerationLocales());
        enumerationScreen.open();
    }

    protected void clearEnumValueIfCurrentItemAbsentOnEnum() {
        if (!defaultEnumValues.contains(defaultEnumField.getValue())) {
            defaultEnumField.clear();
        }
    }

    @Install(to = "targetScreensDl", target = Target.DATA_LOADER)
    protected List<TargetViewComponent> targetScreensDlLoadDelegate(LoadContext<TargetViewComponent> loadContext) {
        return targetScreens;
    }

    @Subscribe("targetScreensTable.create")
    protected void onTargetScreensTableCreate(ActionPerformedEvent event) {
        targetScreensDc.getMutableItems().add(metadata.create(TargetViewComponent.class));
    }

    @Subscribe("targetScreensTable.addAllViews")
    protected void onTargetScreensTableAddAllViews(ActionPerformedEvent event) {
        String entityType = categoryAttributeDc.getItem().getCategory().getEntityType();
        MetaClass metaClass = metadata.getClass(entityType);
        List<String> usedViewIds = targetScreensDc.getItems()
                .stream()
                .map(TargetViewComponent::getView)
                .toList();

        dynAttrFacetInfo.getDynAttrViewIds(metaClass)
                .stream()
                .filter(viewId -> !usedViewIds.contains(viewId))
                .forEach(this::addKnownTargetView);
    }

    protected void addKnownTargetView(String viewId) {
        TargetViewComponent targetViewComponent = metadata.create(TargetViewComponent.class);
        targetViewComponent.setView(viewId);

        targetScreensDc.getMutableItems().add(targetViewComponent);
    }

    @Install(to = "dependsOnAttributesField", subject = "validator")
    protected void dependsOnAttributesFieldValidator(Collection<CategoryAttribute> categoryAttributes) {
        if (org.springframework.util.StringUtils.hasText(recalculationScriptField.getValue())
                && CollectionUtils.isEmpty(categoryAttributes)) {
            throw new ValidationException(
                    messages.getMessage(CategoryAttributesDetailView.class, "dependsOnAttributes.validationMsg"));
        }
    }

    @Supply(to = "targetScreensTable.view", subject = "renderer")
    protected Renderer<TargetViewComponent> targetScreensTableViewRenderer() {
        return new ComponentRenderer<>(this::createComboBox, this::targetViewItemUpdater);
    }

    @Supply(to = "targetScreensTable.component", subject = "renderer")
    protected Renderer<TargetViewComponent> targetScreensTableComponentRenderer() {
        return new ComponentRenderer<>(this::createComboBox, this::targetComponentItemUpdater);
    }

    @SuppressWarnings("unchecked")
    protected JmixComboBox<String> createComboBox() {
        JmixComboBox<String> comboBox = uiComponents.create(JmixComboBox.class);
        comboBox.setWidthFull();
        comboBox.setAllowCustomValue(true);
        return comboBox;
    }

    protected void targetViewItemUpdater(JmixComboBox<String> comboBox, TargetViewComponent item) {
        if (categoryAttributeDc.getItem().getCategory().getEntityType() != null) {
            MetaClass categoryMetaClass = metadata.getClass(categoryAttributeDc.getItem().getCategory().getEntityType());
            comboBox.setItems(dynAttrFacetInfo.getDynAttrViewIds(categoryMetaClass));
        }

        setValueIfAbsentInItems(comboBox, item.getView());

        comboBox.addValueChangeListener(e -> item.setView(e.getValue()));
        comboBox.addCustomValueSetListener(e -> item.setView(e.getDetail()));
    }

    protected void targetComponentItemUpdater(JmixComboBox<String> comboBox, TargetViewComponent item) {
        if (item.getView() != null && categoryAttributeDc.getItem().getCategory().getEntityType() != null) {
            MetaClass categoryMetaClass = metadata.getClass(categoryAttributeDc.getItem().getCategory().getEntityType());
            comboBox.setItems(dynAttrFacetInfo.getDynAttrViewIds(categoryMetaClass));
            Collection<String> targetComponents = dynAttrFacetInfo.getDynAttrViewTargetComponentIds(categoryMetaClass, item.getView());
            comboBox.setItems(targetComponents);
        }

        setValueIfAbsentInItems(comboBox, item.getComponent());

        comboBox.addValueChangeListener(e -> item.setComponent(e.getValue()));
        comboBox.addCustomValueSetListener(e -> item.setComponent(e.getDetail()));
    }

    protected <T> void setValueIfAbsentInItems(JmixComboBox<T> comboBox, T value) {
        if (value == null) {
            return;
        }
        List<T> items = comboBox.getListDataView().getItems().toList();
        if (!items.contains(value)) {
            ArrayList<T> extendedItemsByCustomValue = new ArrayList<>(items);
            extendedItemsByCustomValue.add(value);
            comboBox.setItems(extendedItemsByCustomValue);
        }
        comboBox.setValue(value);
    }

    protected void initAttributeForm() {
        ComponentUtils.setItemsMap(defaultBooleanField, getBooleanOptions());
        ComponentUtils.setItemsMap(dataTypeField, getDataTypeOptions());
        ComponentUtils.setItemsMap(entityClassField, getEntityOptions());

        attachHelperForSuffix(
                validationScriptField,
                dynAttrUiHelper.createHelperButton(
                        messages.getMessage(CategoryAttributesDetailView.class, "validationScriptHelp")));
    }

    protected void attachHelperForSuffix(CodeEditor codeEditor, Component componentSuffix) {
        Element parent = codeEditor.getElement().getParent();
        componentSuffix.getStyle().set("padding-top", "1.9em");
        if (parent.getChildCount() > 0) {
            parent.getComponent().ifPresent(e -> {
                if (e instanceof Div) {
                    parent.removeAllChildren();
                    ((Div) e).add(codeEditor);
                }
            });
        }
        parent.getComponent()
                .ifPresent((Component component) -> {
                    if (component instanceof Div) {
                        ((Div) component).add(componentSuffix);
                    }
                });
    }

    protected void initCalculatedValuesAndOptionsForm() {
        attachHelperForSuffix(
                recalculationScriptField,
                dynAttrUiHelper.createHelperButton(
                        messages.getMessage(CategoryAttributesDetailView.class, "recalculationScriptHelp")));
        String joinHelperMessage = messages.getMessage(CategoryAttributesDetailView.class, "joinHelperText");
        joinClauseField.setHelperComponent(new Html(joinHelperMessage));
    }

    protected void loadTargetViews() {
        targetScreens.clear();
        Set<String> targetScreensSet = getEditedEntity().getTargetScreensSet();
        for (String targetView : targetScreensSet) {
            TargetViewComponent targetViewComponent = metadata.create(TargetViewComponent.class);
            String view;
            String component = null;

            if (targetView.contains("#")) {
                String[] split = targetView.split("#");
                view = split[0];
                component = split[1];
            } else {
                view = targetView;
            }

            targetViewComponent.setView(view);
            targetViewComponent.setComponent(component);

            targetScreens.add(targetViewComponent);
        }
        targetScreensDl.load();
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
            Tab localizationTab = tabSheet.getTabAt(2); // 2 == "localizationTab"
            localizationTab.setVisible(true);

            CrudEntityContext crudEntityContext = new CrudEntityContext(configurationDc.getEntityMetaClass());
            accessManager.applyRegisteredConstraints(crudEntityContext);

            VerticalLayout localizationTabComponent = (VerticalLayout) tabSheet.getComponent(localizationTab);
            localizationFragment = new AttributeLocalizationComponent(coreProperties,
                    msgBundleTools,
                    metadata,
                    messages,
                    messageTools,
                    uiComponents,
                    dataComponents,
                    getViewData().getDataContext());
            localizationFragment.setNameMsgBundle(getEditedEntity().getNameMsgBundle());
            localizationFragment.setDescriptionMsgBundle(getEditedEntity().getDescriptionsMsgBundle());
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

            Assert.notNull(localizationTabComponent, "localizationTabComponent not found");
            localizationTabComponent.add(localizationFragment);
        }
    }

    @SuppressWarnings("unchecked")
    protected void initDependsOnAttributesField() {
        MultiValueSelectAction<CategoryAttribute> selectAction =
                (MultiValueSelectAction<CategoryAttribute>) dependsOnAttributesField.getAction("select");
        Assert.notNull(selectAction, "select action not found");
        selectAction.setItems(DataProviderUtils.createCallbackDataProvider(getAttributesOptions()));
        dependsOnAttributesField.addValueChangeListener(e -> {
            dependsOnAttributesField.setRequired(false);
            if (getEditedEntity().getConfiguration() != null) {
                if (getEditedEntity().getConfiguration().getDependsOnAttributeCodes() == null) {
                    getEditedEntity().getConfiguration().setDependsOnAttributeCodes(new ArrayList<>());
                }
                getEditedEntity().getConfiguration().getDependsOnAttributeCodes().addAll(e.getValue()
                        .stream()
                        .map(CategoryAttribute::getCode)
                        .toList());
            }
        });

        dependsOnAttributesField.setItems(getAttributesOptions());

        if (getEditedEntity().getConfiguration() != null
                && getEditedEntity().getConfiguration().getDependsOnAttributeCodes() != null) {
            dependsOnAttributesField.setValue(getAttributesOptions().stream().filter(o ->
                            getEditedEntity().getConfiguration().getDependsOnAttributeCodes().contains(o.getCode()))
                    .collect(Collectors.toList()));
        }
        CrudEntityContext crudEntityContext = new CrudEntityContext(configurationDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            dependsOnAttributesField.setEnabled(false);
            dependsOnAttributesFieldClear.setEnabled(false);
            dependsOnAttributesFieldSelect.setEnabled(false);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void setupNumberFormat() {
        String formatPattern = getEditedEntity().getConfiguration().getNumberFormatPattern();

        Datatype datatype;
        if (!Strings.isNullOrEmpty(formatPattern)) {
            datatype = new AdaptiveNumberDatatype(BigDecimal.class, formatPattern, "", "",
                    formatStringsRegistry, messages);
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

        List<Component> itemsToUpdate = optionalAttributeForm.getChildren().collect(Collectors.toList());
        // component from advanced tab should be handled too
        itemsToUpdate.add(lookupField);

        for (Component component : itemsToUpdate) {
            boolean visible = visibleFields.contains(component.getId().orElse(StringUtils.EMPTY));
            component.setVisible(visible);

            if (!visible && component instanceof HasValue) {
                if (component.equals(defaultStringField) &&
                        ENUMERATION.equals(attributeType) &&
                        !defaultEnumField.isEmpty()) {
                    continue;
                }
                ((HasValue<?, ?>) component).clear();
            }
        }

        if (MAIN_TAB_NAME.equals(tabSheet.getSelectedTab().getId().orElseThrow())) {
            optionalAttributeForm.setVisible(!visibleFields.isEmpty());
        }

        if (ENTITY.equals(attributeType)) {
            if (!Strings.isNullOrEmpty(entityClassField.getValue())) {
                Class<?> javaClass = categoryAttribute.getJavaType();
                MetaClass metaClass = metadata.getClass(javaClass);
                //noinspection ConstantValue
                if (javaClass != null) {
                    screenField.setItems(List.of(
                            viewRegistry.getListViewId(metaClass),
                            viewRegistry.getLookupViewId(metaClass)));
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
        joinClauseField.getElement().getParent().setVisible(jpqlLoaderVisible);
        whereClauseField.getElement().getParent().setVisible(jpqlLoaderVisible);

        boolean scriptLoaderVisible = optionsType == SQL
                || optionsType == GROOVY;
        optionsLoaderScriptField.getElement().getParent().setVisible(scriptLoaderVisible);

        if (optionsType == GROOVY) {
            attachHelperForSuffix(
                    optionsLoaderScriptField,
                    dynAttrUiHelper.createHelperButton(
                            messages.getMessage(CategoryAttributesDetailView.class, "optionsLoaderGroovyScriptHelp"))
            );
            optionsLoaderScriptField.setMode(CodeEditorMode.GROOVY);
        } else if (optionsType == SQL) {
            attachHelperForSuffix(
                    optionsLoaderScriptField,
                    dynAttrUiHelper.createHelperButton(
                            messages.getMessage(CategoryAttributesDetailView.class, "optionsLoaderSqlScriptHelp"))
            );
            optionsLoaderScriptField.setMode(CodeEditorMode.SQL);
        } else if (optionsType == JPQL) {
            attachHelperForSuffix(
                    joinClauseField,
                    dynAttrUiHelper.createHelperButton(
                            messages.getMessage(CategoryAttributesDetailView.class, "joinClauseHelp"))
            );
            attachHelperForSuffix(
                    whereClauseField,
                    dynAttrUiHelper.createHelperButton(
                            messages.getMessage(CategoryAttributesDetailView.class, "whereClauseHelp"))
            );
        } else {
            attachHelperForSuffix(
                    whereClauseField,
                    new Div()
            );
            optionsLoaderScriptField.setMode(CodeEditorMode.TEXT);
        }

        optionsLoaderTypeField.setEnabled(Boolean.TRUE.equals(categoryAttribute.getLookup()));
        optionsLoaderTypeField.setRequired(Boolean.TRUE.equals(categoryAttribute.getLookup()));

        OptionsLoaderType type = null;
        if (getEditedEntity().getConfiguration() != null &&
                getEditedEntity().getConfiguration().getOptionsLoaderType() != null) {
            type = getEditedEntity().getConfiguration().getOptionsLoaderType();
        }

        ComponentUtils.setItemsMap(optionsLoaderTypeField, getLoaderOptions());

        if (type != null) {
            optionsLoaderTypeField.setValue(type);
        }
    }

    @Subscribe("screenField")
    public void onScreenFieldValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        this.defaultEntityIdField.setEnabled(!Strings.isNullOrEmpty(screenField.getValue()));
    }

    @Subscribe("defaultEntityIdField.lookup")
    public void onDefaultEntityIdFieldLookup(ActionPerformedEvent event) {
        List<MetaClass> metaClasses = metadataTools.getAllJpaEntityMetaClasses()
                .stream()
                .filter(metaClass -> metaClass.getName().equals(getEditedEntity().getCategory().getEntityType()))
                .toList();
        if (metaClasses.size() != 1) {
            throw new IllegalStateException();
        }
        MetaClass targetClass = metaClasses.get(0);
        LookupWindowBuilder<Object, View<?>> lookupBuilder = dialogWindows.lookup(this, targetClass.getJavaClass())
                .withSelectHandler(e -> {
                    if (e.size() != 1) {
                        return;
                    }

                    Object targetEntity = e.iterator().next();
                    defaultEntityIdField.setValue(targetEntity);
                });

        if (!Strings.isNullOrEmpty(screenField.getValue())) {
            lookupBuilder.withViewId(screenField.getValue());
        }

        try {
            lookupBuilder.open();
        } catch (AccessDeniedException ex) {
            notifications.create(messages.getMessage(CategoryAttributesDetailView.class, "entityViewAccessDeniedMessage"))
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
                List<String> views = viewRegistry.getViewInfos().stream().map(ViewInfo::getId).toList();
                categoryAttribute.setScreen(views.contains(categoryAttribute.getScreen()) ? categoryAttribute.getScreen() : null);
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

                defaultEntityIdField.setValue(entity);
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
            char[] delimiters = {' ', '.', '_', '-', '\t'};

            String categoryNameInCamelCaseUncapitalized = DynAttrStringUtils.toCamelCase(categoryName, delimiters);
            String attributeNameInCamelCaseUncapitalized = DynAttrStringUtils.toCamelCase(attribute.getName(), delimiters);

            String resultCodeName = !Strings.isNullOrEmpty(categoryNameInCamelCaseUncapitalized) ?
                    categoryNameInCamelCaseUncapitalized + StringUtils.capitalize(attributeNameInCamelCaseUncapitalized) :
                    attributeNameInCamelCaseUncapitalized;

            codeField.setValue(resultCodeName);
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
                if (!Stores.isMain(metaClass.getStore().getName())) {
                    continue;
                }
                optionsMap.put(metaClass.getJavaClass().getName(), messageTools.getDetailedEntityCaption(metaClass));
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
    protected void onPreCommit(DataContext.PreSaveEvent event) {
        preCommitOrderNo();
        preCommitLocalizationFields();
        preCommitTargetViewsField();
        preCommitConfiguration();
    }

    protected void preCommitOrderNo() {
        if (!entityStates.isNew(getEditedEntity()) && parentCategoryUuid != null) {
            return;
        }

        int orderNo = dataManager.loadValue(
                        "select max(a.orderNo) from dynat_CategoryAttribute a " +
                                "where a.category.id = :categoryId", Integer.class
                )
                .parameter("categoryId", parentCategoryUuid)
                .optional()
                .orElse(0);

        getEditedEntity().setOrderNo(++orderNo);
    }

    protected void preCommitLocalizationFields() {
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
            getEditedEntity().setLocaleDescriptions(localizationFragment.getDescriptionMsgBundle());
        }
    }

    protected void preCommitTargetViewsField() {
        CategoryAttribute attribute = getEditedEntity();
        StringBuilder stringBuilder = new StringBuilder();
        for (TargetViewComponent targetViewComponent : targetScreensDc.getItems()) {
            if (StringUtils.isNotBlank(targetViewComponent.getView())) {
                stringBuilder.append(targetViewComponent.getView());
                if (StringUtils.isNotBlank(targetViewComponent.getComponent())) {
                    stringBuilder.append("#");
                    stringBuilder.append(targetViewComponent.getComponent());
                }
                stringBuilder.append(",");
            }
        }

        if (!stringBuilder.isEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        attribute.setTargetScreens(stringBuilder.toString());
    }

    protected void preCommitConfiguration() {
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
            }
        }
    }
}
