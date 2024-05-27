package io.jmix.reportsflowui.view.parameter;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.PredefinedTransformation;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.libintegration.JmixObjectToStringConverter;
import io.jmix.reportsflowui.constant.ReportStyleConstants;
import io.jmix.reportsflowui.helper.ReportScriptEditor;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import io.jmix.reportsflowui.view.run.ParameterComponentGenerationStrategy;
import io.jmix.reportsflowui.view.validators.ReportParamAliasValidator;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Route(value = "reports/parameters/:id", layout = DefaultMainViewParent.class)
@ViewController("report_ReportInputParameter.detail")
@ViewDescriptor("report-parameter-detail-view.xml")
@EditedEntityContainer("parameterDc")
@DialogMode(width = "40em")
public class ReportParameterDetailView extends StandardDetailView<ReportInputParameter> {

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
    protected VerticalLayout predefinedTransformationBox;
    @ViewComponent
    protected JmixTextArea localeField;
    @ViewComponent
    protected CodeEditor lookupJoinCodeEditor;
    @ViewComponent
    protected CodeEditor lookupWhereCodeEditor;
    @ViewComponent
    protected HorizontalLayout defaultValueBox;
    @ViewComponent
    protected JmixCheckbox isPredefinedTransformationField;
    @ViewComponent
    protected Div transformationEditorBox;
    @ViewComponent
    protected InstanceContainer<ReportInputParameter> parameterDc;
    @ViewComponent
    protected TypedTextField<String> alias;

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
    protected ReportScriptEditor reportScriptEditor;
    @Autowired
    protected JmixObjectToStringConverter jmixObjectToStringConverter;
    @Autowired
    protected ParameterComponentGenerationStrategy parameterComponentGenerationStrategy;
    @Autowired
    protected ReportParamAliasValidator reportParamAliasValidator;

    @Subscribe
    public void onInit(InitEvent event) {
        initParameterTypeField();
        initMetaClassLookup();
        initEnumsLookup();
        initLocaleField();
        initJoinCodeEditorLookupHelperText();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ReportInputParameter editedParam = getEditedEntity();
        if (editedParam.getParameterClass() == null) {
            editedParam.setType(parameterTypeField.getValue());
            editedParam.setParameterClass(parameterClassResolver.resolveClass(editedParam));
        }
        enableControlsByParamType(editedParam.getType());
        initScreensLookup();
        initTransformations();
    }

    private void initParameterTypeField() {
        parameterTypeField.setItems(ParameterType.values());
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

    @Install(to = "alias", subject = "validator")
    private void aliasValidator(final String value) {
        reportParamAliasValidator.accept(value);
    }

    protected void initLocaleField() {
        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(
                ReportStyleConstants.FIELD_ICON_SIZE_CLASS_NAME,
                ReportStyleConstants.FIELD_ICON_CLASS_NAME
        );
        helpIcon.addClickListener(this::onLocaleFieldHelpIconClick);

        localeField.setSuffixComponent(helpIcon);
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

    @Subscribe("fullScreenTransformationBtn")
    public void onFullScreenTransformationBtnClick(final ClickEvent<Button> event) {
        reportScriptEditor.create(this)
                .withTitle(messages.getMessage("fullScreenBtn.title"))
                .withValue(parameterDc.getItem().getTransformationScript())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> parameterDc.getItem().setTransformationScript(value))
                .withHelpOnClick(this::onTransformationScriptHelpIconClick)
                .open();
    }

    public void onTransformationScriptHelpIconClick() {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.transformationScriptHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.transformationScriptHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    @Subscribe("transformationScriptHelpBtn")
    public void onTransformationScriptHelpBtnClick(final ClickEvent<Button> event) {
        onTransformationScriptHelpIconClick();
    }

    @Subscribe("fullScreenValidationBtn")
    public void onFullScreenValidationBtnClick(final ClickEvent<Button> event) {
        reportScriptEditor.create(this)
                .withTitle(messages.getMessage("fullScreenBtn.title"))
                .withValue(parameterDc.getItem().getValidationScript())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> parameterDc.getItem().setValidationScript(value))
                .withHelpOnClick(this::onValidationScriptHelpIconClick)
                .open();
    }

    @Subscribe("validationScriptHelpBtn")
    public void onValidationScriptHelpBtnClick(final ClickEvent<Button> event) {
        onValidationScriptHelpIconClick();
    }

    protected void onValidationScriptHelpIconClick() {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(ReportDetailView.class, "parametersTab.validationFieldHelp.header"))
                .withContent(new Html(messages.getMessage(ReportDetailView.class, "parametersTab.validationFieldHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    @Subscribe("lookupWhereFullScreenBtn")
    public void onLookupWhereFullScreenBtnClick(final ClickEvent<Button> event) {
        reportScriptEditor.create(this)
                .withTitle(messages.getMessage("fullScreenBtn.title"))
                .withValue(parameterDc.getItem().getLookupWhere())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> parameterDc.getItem().setLookupWhere(value))
                .withHelpOnClick(this::onLookupWhereHelpIconClick)
                .open();
    }

    @Subscribe("lookupJoinFullScreenBtn")
    public void onLookupJoinFullScreenBtnClick(final ClickEvent<Button> event) {
        reportScriptEditor.create(this)
                .withTitle(messages.getMessage("fullScreenBtn.title"))
                .withValue(parameterDc.getItem().getLookupJoin())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> parameterDc.getItem().setLookupJoin(value))
                .withHelpOnClick(this::onLookupJoinHelpIconClick)
                .open();
    }

    @Subscribe("lookupJoinHelpBtn")
    public void onLookupJoinHelpBtnClick(final ClickEvent<Button> event) {
        onLookupJoinHelpIconClick();
    }

    @Subscribe("lookupWhereHelpBtn")
    public void onLookupWhereHelpBtnClick(final ClickEvent<Button> event) {
        onLookupWhereHelpIconClick();
    }

    protected void onLookupJoinHelpIconClick() {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.lookupJoinHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.lookupJoinHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onLookupWhereHelpIconClick() {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "parameters.lookupWhereHelp.header"))
                .withContent(new Html(messages.getMessage(getClass(), "parameters.lookupWhereHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
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
                String availableListViewId = viewRegistry.getAvailableListViewId(metadata.getClass(clazz));
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
        ComponentUtils.setItemsMap(enumerationField, enumsOptionsMap);
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

        ComponentUtils.setItemsMap(metaClassField, metaClassesOptionsMap);
    }

    protected void initJoinCodeEditorLookupHelperText() {
        String helperTextMessage =
                messages.getMessage(ReportParameterDetailView.class, "parameters.lookupJoin.helperText");
        lookupJoinCodeEditor.setHelperComponent(new Html(helperTextMessage));
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        boolean isValidated = getContent().getComponents().stream().noneMatch(component ->
                component instanceof SupportsValidation<?>
                        && component.isVisible()
                        && ((HasRequired) component).isRequired()
                        && ((AbstractSinglePropertyField<?, ?>) component).isEmpty());
        if (!isValidated) {
            event.addErrors(ValidationErrors.of(messages.getMessage(getClass(), "validationException.message")));
        }
        try {
            reportParamAliasValidator.accept(alias.getValue());
        } catch (ValidationException e) {
            event.addErrors(ValidationErrors.of(e.getMessage()));
        }
    }

    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        if (!(getEditedEntity().getType() == ParameterType.ENTITY && Boolean.TRUE.equals(isLookupField.getValue()))) {
            lookupWhereCodeEditor.clear();
            lookupJoinCodeEditor.clear();
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
                    String value;
                    if (e.getHasValue() instanceof SupportsTypedValue) {
                        Object typedValue = ((SupportsTypedValue) e.getHasValue()).getTypedValue();
                        value = jmixObjectToStringConverter.convertToString(parameter.getParameterClass(), typedValue);
                    } else {
                        value = jmixObjectToStringConverter.convertToString(e.getValue().getClass(), e.getValue());
                    }

                    parameter.setDefaultValue(value);
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

        boolean isTabVisible = isSingleEntity && Boolean.TRUE.equals(isLookupField.getValue());
        tabsheet.getTabAt((int) tabsheet.getChildren().count() - 1)
                .setVisible(isTabVisible);

        screenField.setVisible(isEntity);
        enumerationField.setVisible(isEnum);
        predefinedTransformationBox.setVisible(isText);
        UiComponentUtils.setValue(isPredefinedTransformationField, isText);

        initDefaultValueField();
        initCurrentDateTimeField();
    }

    @Subscribe("isPredefinedTransformationField")
    public void onIsPredefinedTransformationFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        ReportInputParameter parameter = getEditedEntity();
        boolean hasPredefinedTransformation = event.getValue() != null && event.getValue();

        enableControlsByTransformationType(hasPredefinedTransformation);
        if (hasPredefinedTransformation) {
            parameter.setTransformationScript(null);
        } else {
            parameter.setPredefinedTransformation(null);
        }
    }

    protected void initTransformations() {
        ReportInputParameter parameter = getEditedEntity();

        UiComponentUtils.setValue(isPredefinedTransformationField, parameter.getPredefinedTransformation() != null);
        enableControlsByTransformationType(parameter.getPredefinedTransformation() != null);
    }

    protected void enableControlsByTransformationType(boolean hasPredefinedTransformation) {
        transformationEditorBox.setVisible(!hasPredefinedTransformation);
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
}