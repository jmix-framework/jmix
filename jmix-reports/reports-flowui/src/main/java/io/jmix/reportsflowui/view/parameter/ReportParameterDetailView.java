package io.jmix.reportsflowui.view.parameter;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.PredefinedTransformation;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.libintegration.JmixObjectToStringConverter;
import io.jmix.reportsflowui.ReportsUiHelper;
import io.jmix.reportsflowui.view.report.detailview.ReportDetailView;
import io.jmix.reportsflowui.view.run.ParameterComponentGenerationStrategy;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "reports/parameters/:id", layout = DefaultMainViewParent.class)
@ViewController("report_ReportInputParameter.detail")
@ViewDescriptor("report-parameter-detail-view.xml")
@EditedEntityContainer("parameterDc")
@DialogMode(width = "40em")
public class ReportParameterDetailView extends StandardDetailView<ReportInputParameter> {

    protected static final String FIELD_ICON_SIZE_CLASS_NAME = "reports-field-icon-size";
    protected static final String FIELD_ICON_CLASS_NAME = "template-detailview-field-icon";

    @ViewComponent
    protected JmixComboBox<ParameterType> parameterTypeField;
    @ViewComponent
    protected JmixTabSheet tabsheet;
    @ViewComponent
    protected JmixComboBox<String> screenField;
    @ViewComponent
    protected JmixComboBox<String> enumerationField;
    @ViewComponent
    protected JmixComboBox<String> metaClassField;
    @ViewComponent
    protected JmixCheckbox isLookupField;
    @ViewComponent
    protected JmixCheckbox isDefaultDateIsCurrentField;
    @ViewComponent
    protected JmixComboBox<PredefinedTransformation> wildcardsField;
    @ViewComponent
    protected Div predefinedTransformationBox;
    @ViewComponent
    protected JmixTextArea localeField;
    @ViewComponent
    protected JmixTextArea transformationScript;
    @ViewComponent
    protected JmixTextArea validationScript;
    @ViewComponent
    protected JmixTextArea lookupJoin;
    @ViewComponent
    protected JmixTextArea lookupWhere;
    @ViewComponent
    protected HorizontalLayout defaultValueBox;
    @ViewComponent
    protected JmixCheckbox isPredefinedTransformationField;
    @ViewComponent
    protected InstanceContainer<ReportInputParameter> parameterDc;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ReportsUiHelper reportsUiHelper;
    @Autowired
    protected JmixObjectToStringConverter jmixObjectToStringConverter;
    @Autowired
    private ParameterComponentGenerationStrategy parameterComponentGenerationStrategy;

    @Subscribe
    public void onInit(InitEvent event) {
        parameterTypeField.setItems(Arrays.asList(ParameterType.TEXT, ParameterType.NUMERIC, ParameterType.BOOLEAN, ParameterType.ENUMERATION,
                ParameterType.DATE, ParameterType.TIME, ParameterType.DATETIME, ParameterType.ENTITY, ParameterType.ENTITY_LIST));
        initMetaClassLookup();
        initEnumsLookup();
        initLocaleField();
        //todo AN squeeze code editor methods
        initTransformationScript();
        initValidationScript();
        initLookupJoin();
        initLookupWhere();

    }

    @Override
    public void setEntityToEdit(ReportInputParameter entity) {
        super.setEntityToEdit(entity);

        if (getEditedEntity().getScreen() != null) {
            initScreensLookup();
            screenField.setValue(getEditedEntity().getScreen());
        }
    }

    @Subscribe("screenField")
    public void onScreenFieldAttach(final AttachEvent event) {
        if (getEditedEntity().getScreen() != null) {
            initScreensLookup();
            screenField.setValue(getEditedEntity().getScreen());
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ReportInputParameter editedParam = getEditedEntity();
        if (editedParam.getParameterClass() == null) {
            editedParam.setType(parameterTypeField.getValue());
            editedParam.setParameterClass(parameterClassResolver.resolveClass(editedParam));
        }
    }

    protected void initLocaleField() {
        Icon expandIcon = VaadinIcon.EXPAND_SQUARE.create();
        expandIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        expandIcon.addClickListener(this::onLocaleFieldExpandIconClick);

        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        helpIcon.addClickListener(this::onLocaleFieldHelpIconClick);

        localeField.setSuffixComponent(new Div(expandIcon, helpIcon));
    }


    protected void onLocaleFieldHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.localeTextHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.localeTextHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onLocaleFieldExpandIconClick(ClickEvent<Icon> event) {
        reportsUiHelper.showScriptEditorDialog(
                getScriptEditorDialogCaption(),
                parameterDc.getItem().getTransformationScript(),
                value -> parameterDc.getItem().setTransformationScript(value),
                CodeEditorMode.GROOVY,
                this::onLocaleFieldHelpIconClick
        );
    }

    protected void initTransformationScript() {
        Icon expandIcon = VaadinIcon.EXPAND_SQUARE.create();
        expandIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        expandIcon.addClickListener(this::onTransformationScriptExpandIconClick);

        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        helpIcon.addClickListener(this::onTransformationScriptHelpIconClick);

        transformationScript.setSuffixComponent(new Div(expandIcon, helpIcon));
    }

    protected void onTransformationScriptHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.transformationScriptHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.transformationScriptHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onTransformationScriptExpandIconClick(ClickEvent<Icon> event) {
        reportsUiHelper.showScriptEditorDialog(
                getScriptEditorDialogCaption(),
                parameterDc.getItem().getTransformationScript(),
                value -> parameterDc.getItem().setTransformationScript(value),
                CodeEditorMode.GROOVY,
                this::onTransformationScriptHelpIconClick
        );
    }

    protected void initValidationScript() {
        Icon expandIcon = VaadinIcon.EXPAND_SQUARE.create();
        expandIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        expandIcon.addClickListener(this::onValidationScriptExpandIconClick);

        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        helpIcon.addClickListener(this::onValidationScriptHelpIconClick);

        validationScript.setSuffixComponent(new Div(expandIcon, helpIcon));
    }


    protected void onValidationScriptHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(ReportDetailView.class, "parametersTab.validationFieldHelp.header"))
                .withContent(new Html(messages.getMessage(ReportDetailView.class, "parametersTab.validationFieldHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onValidationScriptExpandIconClick(ClickEvent<Icon> event) {
        reportsUiHelper.showScriptEditorDialog(
                getScriptEditorDialogCaption(),
                parameterDc.getItem().getValidationScript(),
                value -> parameterDc.getItem().setValidationScript(value),
                CodeEditorMode.GROOVY,
                this::onValidationScriptHelpIconClick
        );
    }

    protected void initLookupJoin() {
        Icon expandIcon = VaadinIcon.EXPAND_SQUARE.create();
        expandIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        expandIcon.addClickListener(this::onLookupJoinExpandIconClick);

        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        helpIcon.addClickListener(this::onLookupJoinHelpIconClick);

        lookupJoin.setSuffixComponent(new Div(expandIcon, helpIcon));
    }


    protected void onLookupJoinHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.lookupJoinHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.lookupJoinHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onLookupJoinExpandIconClick(ClickEvent<Icon> event) {
        reportsUiHelper.showScriptEditorDialog(
                getScriptEditorDialogCaption(),
                parameterDc.getItem().getLookupJoin(),
                value -> parameterDc.getItem().setLookupJoin(value),
                CodeEditorMode.GROOVY,
                this::onLookupJoinHelpIconClick
        );
    }

    protected void initLookupWhere() {
        Icon expandIcon = VaadinIcon.EXPAND_SQUARE.create();
        expandIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        expandIcon.addClickListener(this::onLookupWhereExpandIconClick);

        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        helpIcon.addClickListener(this::onLookupWhereHelpIconClick);

        lookupWhere.setSuffixComponent(new Div(expandIcon, helpIcon));
    }

    protected void onLookupWhereHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.lookupWhereHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.lookupWhereHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onLookupWhereExpandIconClick(ClickEvent<Icon> event) {
        reportsUiHelper.showScriptEditorDialog(
                getScriptEditorDialogCaption(),
                parameterDc.getItem().getLookupWhere(),
                value -> parameterDc.getItem().setLookupWhere(value),
                CodeEditorMode.GROOVY,
                this::onLookupWhereHelpIconClick
        );
    }

    protected String getScriptEditorDialogCaption() {
        String reportName = parameterDc.getItem().getName();
        String bandName = parameterDc.getItem().getName();

        if (ObjectUtils.isNotEmpty(bandName) && ObjectUtils.isNotEmpty(reportName)) {
            return messages.formatMessage(
                    "bandsTab.dataSetTypeLayout.jsonGroovyCodeEditor.expandIcon.dialog.header", reportName, bandName);
        }
        return StringUtils.EMPTY;
    }

    @Subscribe("parameterTypeField")
    public void onParameterTypeFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixComboBox<ParameterType>, ParameterType> event) {
        enableControlsByParamType(event.getValue());
    }

    @Subscribe(id = "parameterDc", target = Target.DATA_CONTAINER)
    public void onParameterDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ReportInputParameter> event) {
        String property = event.getProperty();

        boolean typeChanged = property.equalsIgnoreCase("type");
        boolean classChanged = property.equalsIgnoreCase("entityMetaClass")
                || property.equalsIgnoreCase("enumerationClass");
        boolean defaultDateIsCurrentChanged = property.equalsIgnoreCase("defaultDateIsCurrent");
        ReportInputParameter parameter = getEditedEntity();
        if (typeChanged || classChanged) {
            parameter.setParameterClass(parameterClassResolver.resolveClass(parameter));

            if (typeChanged) {
                parameter.setEntityMetaClass(null);
                parameter.setEnumerationClass(null);
            }

            parameter.setDefaultValue(null);
            parameter.setScreen(null);

            initScreensLookup();
            initDefaultValueField();
        }

        if (defaultDateIsCurrentChanged) {
            initDefaultValueField();
            initCurrentDateTimeField();
        }
    }

    @Subscribe("isLookupField")
    public void onIsLookupFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        tabsheet.getTabAt((int) tabsheet.getChildren().count() - 1).setVisible(Boolean.TRUE.equals(event.getValue()));
    }

    protected void initScreensLookup() {
        ReportInputParameter parameter = getEditedEntity();
        if (parameter.getType() == ParameterType.ENTITY || parameter.getType() == ParameterType.ENTITY_LIST) {
            Class clazz = parameterClassResolver.resolveClass(parameter);
            if (clazz != null) {
                String availableListViewId = viewRegistry.getAvailableListViewId(metadata.findClass(clazz));
                screenField.setItems(availableListViewId);

            }
        }
    }

    protected void initEnumsLookup() {
        Map<String, String> enumsOptionsMap = new TreeMap<>();
        for (Class enumClass : metadataTools.getAllEnums()) {
            String enumLocalizedName = messages.getMessage(enumClass, enumClass.getSimpleName());
            enumsOptionsMap.put(enumClass.getCanonicalName(), enumLocalizedName + " (" + enumClass.getSimpleName() + ")");
        }
        FlowuiComponentUtils.setItemsMap(enumerationField, enumsOptionsMap);
    }

    protected void initMetaClassLookup() {
        Map<String, String> metaClassesOptionsMap = new TreeMap<>();
        Collection<MetaClass> classes = metadata.getSession().getClasses();

        for (MetaClass clazz : classes) {
            if (!metadataTools.isSystemLevel(clazz)) {
                String caption = messageTools.getDetailedEntityCaption(clazz);
                metaClassesOptionsMap.put(clazz.getName(), caption);
            }
        }

        FlowuiComponentUtils.setItemsMap(metaClassField, metaClassesOptionsMap);
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        if (!(getEditedEntity().getType() == ParameterType.ENTITY && Boolean.TRUE.equals(isLookupField.getValue()))) {
            lookupWhere.clear();
            lookupJoin.clear();
        }
    }

    protected void initDefaultValueField() {
        defaultValueBox.removeAll();
        ReportInputParameter parameter = getEditedEntity();
        if (canHaveDefaultValue()) {
            AbstractField field;
            if (ParameterType.ENTITY.equals(parameter.getType()) && Boolean.TRUE.equals(parameter.getLookup())) {
                ReportInputParameter entityParam = metadata.create(ReportInputParameter.class);
                entityParam.setReport(parameter.getReport());
                entityParam.setType(parameter.getType());
                entityParam.setEntityMetaClass(parameter.getEntityMetaClass());
                entityParam.setScreen(parameter.getScreen());
                entityParam.setAlias(parameter.getAlias());
                entityParam.setRequired(parameter.getRequired());
                field = parameterComponentGenerationStrategy.createField(entityParam);
            } else {
                field = parameterComponentGenerationStrategy.createField(parameter);
            }

            field.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    parameter.setDefaultValue(jmixObjectToStringConverter.convertToString(e.getValue().getClass(), e.getValue()));
                } else {
                    parameter.setDefaultValue(null);
                }
            });

            if (parameter.getParameterClass() != null && parameter.getDefaultValue() != null) {
                Object value = jmixObjectToStringConverter.convertFromString(parameter.getParameterClass(), parameter.getDefaultValue());
                UiComponentUtils.setValue(field, value);
            }
            field.getElement().setProperty("required", false);
            field.getElement().setProperty("label", messages.getMessage(getClass(), "parameters.defaultValue"));

            field.getElement().getStyle().set("width", "100%");

            defaultValueBox.add(field);
        }
        defaultValueBox.setEnabled(secureOperations.isEntityUpdatePermitted(metadata.getClass(ReportInputParameter.class), policyStore));
    }

    @Subscribe(id = "parameterDc", target = Target.DATA_CONTAINER)
    public void onParameterDcItemChange(InstanceContainer.ItemChangeEvent<ReportInputParameter> event) {
        ReportInputParameter reportInputParameter = event.getItem();
        ReportInputParameter newParameter = metadata.create(reportInputParameter.getClass());
        metadataTools.copy(reportInputParameter, newParameter);
        newParameter.setId((UUID) Id.of(reportInputParameter).getValue());
        if (newParameter.getParameterClass() == null) {
            newParameter.setParameterClass(parameterClassResolver.resolveClass(newParameter));
        }

        enableControlsByParamType(newParameter.getType());
        initScreensLookup();
        initTransformations();
    }

    protected void initCurrentDateTimeField() {
        boolean parameterDateOrTime = isParameterDateOrTime();
        isDefaultDateIsCurrentField.setVisible(parameterDateOrTime);
    }

    protected boolean canHaveDefaultValue() {
        ReportInputParameter parameter = getEditedEntity();
        if (isParameterDateOrTime() && BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
            return false;
        }

        ParameterType type = parameter.getType();
        return type != null
                && type != ParameterType.ENTITY_LIST
                && (type != ParameterType.ENTITY || StringUtils.isNotBlank(parameter.getEntityMetaClass()))
                && (type != ParameterType.ENUMERATION || StringUtils.isNotBlank(parameter.getEnumerationClass()));
    }

    protected void enableControlsByParamType(ParameterType type) {
        boolean isSingleEntity = type == ParameterType.ENTITY;
        boolean isEntity = isSingleEntity || type == ParameterType.ENTITY_LIST;
        boolean isEnum = type == ParameterType.ENUMERATION;
        boolean isText = type == ParameterType.TEXT;

        metaClassField.setVisible(isEntity);
        isLookupField.setVisible(isSingleEntity);

        tabsheet.getTabAt((int) tabsheet.getChildren().count() - 1).setVisible(isSingleEntity && Boolean.TRUE.equals(isLookupField.getValue()));

        screenField.setVisible(isEntity);
        enumerationField.setVisible(isEnum);
        predefinedTransformationBox.setVisible(isText);

        if (!isText) {
            isPredefinedTransformationField.setValue(false);
        }
        initDefaultValueField();
        initCurrentDateTimeField();
    }

    protected void initTransformations() {
        ReportInputParameter parameter = getEditedEntity();
        isPredefinedTransformationField.setValue(parameter.getPredefinedTransformation() != null);
        enableControlsByTransformationType(parameter.getPredefinedTransformation() != null);
        isPredefinedTransformationField.addValueChangeListener(e -> {
            boolean hasPredefinedTransformation = e.getValue() != null && e.getValue();

            enableControlsByTransformationType(hasPredefinedTransformation);
            if (hasPredefinedTransformation) {
                parameter.setTransformationScript(null);
            } else {
                parameter.setPredefinedTransformation(null);
            }
        });
        isPredefinedTransformationField.setReadOnly(secureOperations.isEntityUpdatePermitted(metadata.getClass(ReportInputParameter.class), policyStore));
    }

    protected void enableControlsByTransformationType(boolean hasPredefinedTransformation) {
        transformationScript.setVisible(!hasPredefinedTransformation);
        wildcardsField.setVisible(hasPredefinedTransformation);
    }

    protected boolean isParameterDateOrTime() {
        ReportInputParameter parameter = getEditedEntity();
        return Optional.ofNullable(parameter)
                .map(reportInputParameter ->
                        ParameterType.DATE.equals(parameter.getType()) ||
                                ParameterType.DATETIME.equals(parameter.getType()) ||
                                ParameterType.TIME.equals(parameter.getType()))
                .orElse(false);
    }
//todo an return with code editor suggestions
//    protected List<SourceCodeAnalysis.Suggestion> requestHint(SourceCodeEditor sender, int senderCursorPosition) {

//        String joinStr = lookupJoin.getValue();
//        String whereStr = lookupWhere.getValue();
//
//        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
//        String entityAlias = "a39";
//
//        int queryPosition = -1;
//        Class javaClassForEntity = getEditedEntity().getParameterClass();
//        if (javaClassForEntity == null) {
//            return new ArrayList<>();
//        }
//
//        String queryStart = String.format("select %s from %s %s ", entityAlias, metadata.getClass(javaClassForEntity), entityAlias);
//
//        StringBuilder queryBuilder = new StringBuilder(queryStart);
//        if (StringUtils.isNotEmpty(joinStr)) {
//            if (sender == lookupJoin) {
//                queryPosition = queryBuilder.length() + senderCursorPosition - 1;
//            }
//            if (!StringUtils.containsIgnoreCase(joinStr, "join") && !StringUtils.contains(joinStr, ",")) {
//                queryBuilder.append("join ").append(joinStr);
//                queryPosition += "join ".length();
//            } else {
//                queryBuilder.append(joinStr);
//            }
//        }
//        if (StringUtils.isNotEmpty(whereStr)) {
//            if (sender == lookupWhere) {
//                queryPosition = queryBuilder.length() + WHERE.length() + senderCursorPosition - 1;
//            }
//            queryBuilder.append(WHERE).append(whereStr);
//        }
//        String query = queryBuilder.toString();
//        query = query.replace("{E}", entityAlias);
//
//        return jpqlUiSuggestionProvider.getSuggestions(query, queryPosition, sender.getAutoCompleteSupport());
//    }
}