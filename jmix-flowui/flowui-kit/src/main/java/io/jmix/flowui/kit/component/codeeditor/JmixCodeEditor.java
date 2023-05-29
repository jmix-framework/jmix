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

    protected static final int PRINT_MARGIN_COLUMN_DEFAULT_VALUE = 80;
    protected static final String FONT_SIZE_DEFAULT_VALUE = "1rem";

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
        setPrintMarginColumn(PRINT_MARGIN_COLUMN_DEFAULT_VALUE);
        setFontSize(FONT_SIZE_DEFAULT_VALUE);

        addValueChangeListener(e -> validate());
        addClientValidatedEventListener(e -> validate());
    }

    /** @see JmixCodeEditor#setHighlightActiveLine(boolean)
     *
     * @return Visibility of highlighting the active line
     */
    @Synchronize(PROPERTY_HIGHLIGHT_ACTIVE_LINE)
    public boolean isHighlightActiveLine() {
        return getElement().getProperty(PROPERTY_HIGHLIGHT_ACTIVE_LINE, true);
    }

    /**
     * Sets the highlight of the line the cursor is on
     */
    public void setHighlightActiveLine(boolean highlightActiveLine) {
        getElement().setProperty(PROPERTY_HIGHLIGHT_ACTIVE_LINE, highlightActiveLine);
    }

    /**
     * @see JmixCodeEditor#setShowGutter(boolean)
     * @return Gutter visibility
     */
    @Synchronize(PROPERTY_SHOW_GUTTER)
    public boolean isShowGutter() {
        return getElement().getProperty(PROPERTY_SHOW_GUTTER, true);
    }

    /**
     * Sets visibility of the Gutter
     */
    public void setShowGutter(boolean showGutter) {
        getElement().setProperty(PROPERTY_SHOW_GUTTER, showGutter);
    }

    /**
     * @see JmixCodeEditor#setShowLineNumbers(boolean)
     * @return Editor line numbering visibility
     */
    @Synchronize(PROPERTY_SHOW_LINE_NUMBERS)
    public boolean isShowLineNumbers() {
        return getElement().getProperty(PROPERTY_SHOW_LINE_NUMBERS, true);
    }

    /**
     * Sets the line numbering of the editor
     */
    public void setShowLineNumbers(boolean showLineNumbers) {
        getElement().setProperty(PROPERTY_SHOW_LINE_NUMBERS, showLineNumbers);
    }

    /**
     * @see JmixCodeEditor#setShowPrintMargin(boolean)
     * @return Print margin visibility
     */
    @Synchronize(PROPERTY_SHOW_PRINT_MARGIN)
    public boolean isShowPrintMargin() {
        return getElement().getProperty(PROPERTY_SHOW_PRINT_MARGIN, true);
    }

    /**
     * Sets print margin visibility
     */
    public void setShowPrintMargin(boolean showPrintMargin) {
        getElement().setProperty(PROPERTY_SHOW_PRINT_MARGIN, showPrintMargin);
    }

    /**
     * @see JmixCodeEditor#setPrintMarginColumn(int)
     * @return Print margin position in symbols
     */
    @Synchronize(PROPERTY_PRINT_MARGIN_COLUMN)
    public int getPrintMarginColumn() {
        return getElement().getProperty(PROPERTY_PRINT_MARGIN_COLUMN, PRINT_MARGIN_COLUMN_DEFAULT_VALUE);
    }

    /**
     * Set print margin position in symbols
     *
     * @param printMarginColumn print margin position in symbols
     */
    public void setPrintMarginColumn(int printMarginColumn) {
        getElement().setProperty(PROPERTY_PRINT_MARGIN_COLUMN, printMarginColumn);
    }

    /**
     * @see JmixCodeEditor#setTheme(CodeEditorTheme)
     * @return Current visual theme
     */
    @Synchronize(PROPERTY_THEME)
    public CodeEditorTheme getTheme() {
        return CodeEditorTheme.fromId(getElement().getProperty(PROPERTY_THEME));
    }

    /**
     * Sets the visual theme of the editor
     */
    public void setTheme(CodeEditorTheme theme) {
        getElement().setProperty(PROPERTY_THEME, theme.getId());
    }

    /**
     * @see JmixCodeEditor#setMode(CodeEditorMode)
     * @return current syntax highlighting mode
     */
    @Synchronize(PROPERTY_MODE)
    public CodeEditorMode getMode() {
        return CodeEditorMode.fromId(getElement().getProperty(PROPERTY_MODE));
    }

    /**
     * Sets syntax highlighting for a specific mode
     */
    public void setMode(CodeEditorMode mode) {
        getElement().setProperty(PROPERTY_MODE, mode.getId());
    }

    /**
     * @see JmixCodeEditor#setFontSize(String)
     * @return Font size of the editor
     */
    @Synchronize(PROPERTY_FONT_SIZE)
    public String getFontSize() {
        return getElement().getProperty(PROPERTY_FONT_SIZE, FONT_SIZE_DEFAULT_VALUE);
    }

    /**
     * Sets the font size for the editor
     */
    public void setFontSize(String fontSize) {
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
