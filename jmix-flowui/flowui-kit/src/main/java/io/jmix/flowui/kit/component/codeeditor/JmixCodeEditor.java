/*
 * Copyright 2023 Haulmont.
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
package io.jmix.flowui.kit.component.codeeditor;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.HasTitle;

@Tag("jmix-code-editor")
@NpmPackage(
        value = "ace-builds",
        version = "1.18.0"
)
@JsModule("./src/code-editor/jmix-code-editor.js")
public class JmixCodeEditor extends AbstractSinglePropertyField<JmixCodeEditor, String>
        implements CompositionNotifier, Focusable<JmixCodeEditor>, HasClientValidation, HasHelper,
        HasLabel, HasTitle, HasSize, HasStyle, HasTooltip, HasValidationProperties,
        HasValidator<String>, InputNotifier, KeyNotifier {

    protected static final String CODE_EDITOR_VALUE_CHANGED_EVENT = "value-changed";

    protected static final String PROPERTY_VALUE = "value";
    protected static final String PROPERTY_THEME = "theme";
    protected static final String PROPERTY_MODE = "mode";
    protected static final String PROPERTY_HIGHLIGHT_ACTIVE_LINE = "highlightActiveLine";
    protected static final String PROPERTY_SHOW_GUTTER = "showGutter";
    protected static final String PROPERTY_SHOW_LINE_NUMBERS = "showLineNumbers";
    protected static final String PROPERTY_SHOW_PRINT_MARGIN = "showPrintMargin";
    protected static final String PROPERTY_PRINT_MARGIN_COLUMN = "printMarginColumn";
    protected static final String PROPERTY_FONT_SIZE = "fontSize";

    protected CodeEditorValidationSupport validationSupport;

    public JmixCodeEditor() {
        super(PROPERTY_VALUE, "", true);

        ComponentUtil.addListener(this, JmixCodeEditorValueChangedEvent.class, this::onCodeEditorValueChangedEvent);

        initComponent();
    }

    protected void initComponent() {
        if ((getElement().getProperty(PROPERTY_VALUE) == null)) {
            setPresentationValue(getEmptyValue());
        }

        setInvalid(false);
        addValueChangeListener(e -> validate());
        addClientValidatedEventListener(e -> validate());
    }

    @Synchronize(PROPERTY_HIGHLIGHT_ACTIVE_LINE)
    public boolean isHighlightActiveLine() {
        return getElement().getProperty(PROPERTY_HIGHLIGHT_ACTIVE_LINE, true);
    }

    public void setHighlightActiveLine(boolean highlightActiveLine) {
        getElement().setProperty(PROPERTY_HIGHLIGHT_ACTIVE_LINE, highlightActiveLine);
    }

    @Synchronize(PROPERTY_SHOW_GUTTER)
    public boolean isShowGutter() {
        return getElement().getProperty(PROPERTY_SHOW_GUTTER, true);
    }

    public void setShowGutter(boolean showGutter) {
        getElement().setProperty(PROPERTY_SHOW_GUTTER, showGutter);
    }

    @Synchronize(PROPERTY_SHOW_LINE_NUMBERS)
    public boolean isShowLineNumbers() {
        return getElement().getProperty(PROPERTY_SHOW_LINE_NUMBERS, true);
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        getElement().setProperty(PROPERTY_SHOW_LINE_NUMBERS, showLineNumbers);
    }

    @Synchronize(PROPERTY_SHOW_PRINT_MARGIN)
    public boolean isShowPrintMargin() {
        return getElement().getProperty(PROPERTY_SHOW_PRINT_MARGIN, true);
    }

    public void setShowPrintMargin(boolean showPrintMargin) {
        getElement().setProperty(PROPERTY_SHOW_PRINT_MARGIN, showPrintMargin);
    }

    @Synchronize(PROPERTY_PRINT_MARGIN_COLUMN)
    public int getPrintMarginColumn() {
        return getElement().getProperty(PROPERTY_PRINT_MARGIN_COLUMN, 80);
    }

    public void setPrintMarginColumn(int printMarginColumn) {
        getElement().setProperty(PROPERTY_PRINT_MARGIN_COLUMN, printMarginColumn);
    }

    @Synchronize(PROPERTY_THEME)
    public CodeEditorTheme getTheme() {
        return CodeEditorTheme.valueOf(getElement().getProperty(PROPERTY_THEME));
    }

    public void setTheme(CodeEditorTheme theme) {
        getElement().setProperty(PROPERTY_THEME, theme.getThemeName());
    }

    @Synchronize(PROPERTY_MODE)
    public CodeEditorMode getMode() {
        return CodeEditorMode.valueOf(getElement().getProperty(PROPERTY_MODE));
    }

    public void setMode(CodeEditorMode mode) {
        getElement().setProperty(PROPERTY_MODE, mode.getModeName());
    }

    @Synchronize(PROPERTY_FONT_SIZE)
    public int getFontSize() {
        return getElement().getProperty(PROPERTY_FONT_SIZE, 16);
    }

    public void setFontSize(int fontSize) {
        getElement().setProperty(PROPERTY_FONT_SIZE, fontSize);
    }

    /**
     * Determines whether the field is marked as input required.
     *
     * @return {@code true} if the input is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return getElement().getProperty("required", false);
    }

    /**
     * Specifies that the user must fill in a value.
     *
     * @param required the boolean value to set
     */
    public void setRequired(boolean required) {
        getElement().setProperty("required", required);
        getValidationSupport().setRequired(required);
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        getElement().setProperty(PROPERTY_VALUE, newPresentationValue);
    }

    public String getEmptyValue() {
        return "";
    }

    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        getValidationSupport().setRequired(requiredIndicatorVisible);
    }

    public Validator<String> getDefaultValidator() {
        return (value, context) -> getValidationSupport().isInvalid(value)
                ? ValidationResult.ok()
                : ValidationResult.error("");
    }

    public Registration addValidationStatusChangeListener(ValidationStatusChangeListener<String> listener) {
        return addClientValidatedEventListener((event) ->
                listener.validationStatusChanged(new ValidationStatusChangeEvent<>(this, !isInvalid())));
    }

    protected void validate() {
        setInvalid(getValidationSupport().isInvalid(getValue()));
    }

    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    protected void onCodeEditorValueChangedEvent(JmixCodeEditorValueChangedEvent event) {
        handleValueChanged(event.isFromClient(), event.getValue());
    }

    protected void handleValueChanged(boolean isFromClient, String newValue) {
        setModelValue(newValue, isFromClient);
    }

    protected CodeEditorValidationSupport getValidationSupport() {
        if (this.validationSupport == null) {
            this.validationSupport = new CodeEditorValidationSupport(this);
        }

        return validationSupport;
    }

    @DomEvent(CODE_EDITOR_VALUE_CHANGED_EVENT)
    public static class JmixCodeEditorValueChangedEvent extends ComponentEvent<JmixCodeEditor> {

        protected String value;

        public JmixCodeEditorValueChangedEvent(JmixCodeEditor source, boolean fromClient,
                                               @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
