/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.component.markdowneditor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.Registration;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A markdown editor field component that supports editing and rendered preview.
 * <p>
 * The component provides an Edit/Preview tab bar, a formatting toolbar with
 * common markdown actions and keyboard shortcuts, and renders the preview using
 * the built-in {@code vaadin-markdown} component.
 */
@Tag("jmix-markdown-editor")
@JsModule("./src/markdown-editor/jmix-markdown-editor.js")
public class JmixMarkdownEditor extends AbstractSinglePropertyField<JmixMarkdownEditor, String>
        implements HasLabel, HasHelper, HasPlaceholder, HasValidationProperties, HasValidator<String>,
        HasSize, HasEnabled, HasTooltip, HasValueChangeMode, HasAriaLabel, Focusable<JmixMarkdownEditor>,
        HasThemeVariant<MarkdownEditorVariant> {

    protected ValueChangeMode currentMode;
    protected int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

    protected JmixMarkdownEditorI18n i18n;

    protected Validator<String> defaultValidator = (value, context) -> {
        // Skip the required check when called from Binder — it has its own
        // required validation and passes a non-null context.
        boolean fromComponent = context == null;
        if (fromComponent) {
            return ValidationUtil.validateRequiredConstraint(
                    getI18nErrorMessage(JmixMarkdownEditorI18n::getRequiredErrorMessage),
                    isRequiredIndicatorVisible(), value, getEmptyValue());
        }
        return ValidationResult.ok();
    };

    protected ValidationController<JmixMarkdownEditor, String> validationController =
            new ValidationController<>(this);

    /**
     * Constructs an empty {@code JmixMarkdownEditor}.
     */
    public JmixMarkdownEditor() {
        super("value", "", false);

        // Tells the web component that validation is controlled server-side.
        getElement().setProperty("manualValidation", true);

        // Workaround for https://github.com/vaadin/flow/issues/3496.
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());

        getElement().addPropertyChangeListener("mode", event ->
                fireEvent(new ModeChangedEvent(this, event.isUserOriginated())));
    }

    /**
     * Constructs an empty {@code JmixMarkdownEditor} with the given label.
     *
     * @param label the text to set as the label
     */
    public JmixMarkdownEditor(String label) {
        this();
        setLabel(label);
    }

    @Override
    public String getEmptyValue() {
        return "";
    }

    @Override
    public Validator<String> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    /**
     * Gets current value change mode of the component.
     *
     * @return current value change mode of the component, or {@code null} if
     * the value is not synchronized
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    /**
     * Sets new value change mode for the component.
     *
     * @param valueChangeMode new value change mode, or {@code null} to disable the value
     *                        synchronization
     */
    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(
                ValueChangeMode.eventForMode(valueChangeMode, "input"));
        applyChangeTimeout();
    }

    /**
     * Returns the currently set timeout, for how often
     * {@link ValueChangeEvent}s are triggered when the ValueChangeMode is set
     * to {@link ValueChangeMode#LAZY}, or {@link ValueChangeMode#TIMEOUT}.
     *
     * @return the timeout in milliseconds of how often {@link ValueChangeEvent}s are triggered.
     */
    @Override
    public int getValueChangeTimeout() {
        return valueChangeTimeout;
    }

    /**
     * Sets how often {@link ValueChangeEvent}s are triggered when the
     * ValueChangeMode is set to {@link ValueChangeMode#LAZY}, or {@link ValueChangeMode#TIMEOUT}.
     *
     * @param valueChangeTimeout the timeout in milliseconds of how often
     *                           {@link ValueChangeEvent}s are triggered.
     */
    @Override
    public void setValueChangeTimeout(int valueChangeTimeout) {
        this.valueChangeTimeout = valueChangeTimeout;
        applyChangeTimeout();
    }

    protected void applyChangeTimeout() {
        ValueChangeMode.applyChangeTimeout(getValueChangeMode(),
                getValueChangeTimeout(), getSynchronizationRegistration());
    }

    /**
     * Alias for {@link #setRequiredIndicatorVisible(boolean)}.
     *
     * @param required {@code true} to make the field required, {@code false}
     *                 otherwise
     */
    public void setRequired(boolean required) {
        setRequiredIndicatorVisible(required);
    }

    /**
     * Alias for {@link #isRequiredIndicatorVisible()}
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredIndicatorVisible();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (this.i18n != null) {
            setI18nWithJS();
        }
    }

    /**
     * Gets the internationalization object previously set for this component.
     *
     * @return the i18n object, or {@code null} if none has been set
     */
    public JmixMarkdownEditorI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n the i18n object, not {@code null}
     */
    public void setI18n(JmixMarkdownEditorI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                setI18nWithJS();
            }
        });
    }

    /**
     * Returns the current mode of this editor.
     *
     * @return the current {@link MarkdownEditorMode}, never {@code null}
     */
    @Synchronize(property = "mode", value = "mode-changed")
    public MarkdownEditorMode getMode() {
        return MarkdownEditorMode.fromPropertyValue(getElement().getProperty("mode", "edit"));
    }

    /**
     * Sets the editor mode.
     *
     * @param mode the mode to set, not {@code null}
     */
    public void setMode(MarkdownEditorMode mode) {
        Objects.requireNonNull(mode, "Mode must not be null");
        getElement().setProperty("mode", mode.getPropertyValue());
    }

    /**
     * Adds a listener that is notified whenever the editor mode changes between
     * Edit and Preview.
     *
     * @param listener the listener to add, not {@code null}
     * @return a handle that can be used to remove the listener
     */
    public Registration addModeChangedListener(
            ComponentEventListener<ModeChangedEvent> listener) {
        return addListener(ModeChangedEvent.class, listener);
    }

    /**
     * Validates the current value against the field constraints and updates
     * the invalid state and error message accordingly. Has no effect when
     * manual validation is enabled.
     */
    protected void validate() {
        validationController.validate(getValue());
    }

    protected void setI18nWithJS() {
        ObjectNode i18nJson = JacksonUtils.beanToJson(i18n);
        getElement().executeJs("this.i18n = Object.assign({}, this.i18n, $0);", i18nJson);
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, context -> command.accept(ui)));
    }

    protected String getI18nErrorMessage(Function<JmixMarkdownEditorI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * Event fired when the editor switches between Edit and Preview mode.
     */
    public static class ModeChangedEvent extends ComponentEvent<JmixMarkdownEditor> {

        protected final MarkdownEditorMode mode;

        /**
         * Creates a new event.
         *
         * @param source     the component that fired the event
         * @param fromClient {@code true} if the event originated on the client
         */
        public ModeChangedEvent(JmixMarkdownEditor source, boolean fromClient) {
            super(source, fromClient);
            this.mode = source.getMode();
        }

        /**
         * Returns the new mode.
         *
         * @return the current {@link MarkdownEditorMode}, never {@code null}
         */
        public MarkdownEditorMode getMode() {
            return mode;
        }
    }

    /**
     * Internationalization properties for {@link JmixMarkdownEditor}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JmixMarkdownEditorI18n implements Serializable {

        @JsonIgnore
        protected String requiredErrorMessage;

        protected Tabs tabs;

        protected Toolbar toolbar;

        /**
         * Gets the error message displayed when the field is required but empty.
         *
         * @return the error message, or {@code null} if not set
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but empty.
         * <p>
         * Note: error messages set with
         * {@link JmixMarkdownEditor#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param requiredErrorMessage the error message, or {@code null} to clear it
         * @return this instance for method chaining
         */
        public JmixMarkdownEditorI18n setRequiredErrorMessage(String requiredErrorMessage) {
            this.requiredErrorMessage = requiredErrorMessage;
            return this;
        }

        /**
         * Gets the i18n properties for the mode tabs.
         *
         * @return the tabs i18n, or {@code null} if not set
         */
        public Tabs getTabs() {
            return tabs;
        }

        /**
         * Sets the i18n properties for the mode tabs.
         *
         * @param tabs the tabs i18n
         * @return this instance for method chaining
         */
        public JmixMarkdownEditorI18n setTabs(Tabs tabs) {
            this.tabs = tabs;
            return this;
        }

        /**
         * Gets the i18n properties for the toolbar buttons.
         *
         * @return the toolbar i18n, or {@code null} if not set
         */
        public Toolbar getToolbar() {
            return toolbar;
        }

        /**
         * Sets the i18n properties for the toolbar buttons.
         *
         * @param toolbar the toolbar i18n
         * @return this instance for method chaining
         */
        public JmixMarkdownEditorI18n setToolbar(Toolbar toolbar) {
            this.toolbar = toolbar;
            return this;
        }

        /**
         * Internationalization properties for the mode tabs.
         */
        public static class Tabs implements Serializable {

            protected String edit;
            protected String preview;

            /**
             * Gets the label for the Edit tab.
             *
             * @return the label, or {@code null} if not set
             */
            public String getEdit() {
                return edit;
            }

            /**
             * Sets the label for the Edit tab.
             *
             * @param edit the label, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Tabs setEdit(String edit) {
                this.edit = edit;
                return this;
            }

            /**
             * Gets the label for the Preview tab.
             *
             * @return the label, or {@code null} if not set
             */
            public String getPreview() {
                return preview;
            }

            /**
             * Sets the label for the Preview tab.
             *
             * @param preview the label, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Tabs setPreview(String preview) {
                this.preview = preview;
                return this;
            }
        }

        /**
         * Internationalization properties for the formatting toolbar.
         */
        public static class Toolbar implements Serializable {

            protected String accessibleLabel;
            protected String heading;
            protected String bold;
            protected String italic;
            protected String quote;
            protected String code;
            protected String link;
            protected String unorderedList;
            protected String orderedList;
            protected String taskList;
            protected String overflow;

            /**
             * Gets the accessible label for the toolbar element.
             *
             * @return the label, or {@code null} if not set
             */
            public String getAccessibleLabel() {
                return accessibleLabel;
            }

            /**
             * Sets the accessible label for the toolbar element.
             *
             * @param accessibleLabel the label, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setAccessibleLabel(String accessibleLabel) {
                this.accessibleLabel = accessibleLabel;
                return this;
            }

            /**
             * Gets the tooltip for the Heading button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getHeading() {
                return heading;
            }

            /**
             * Sets the tooltip for the Heading button.
             *
             * @param heading the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setHeading(String heading) {
                this.heading = heading;
                return this;
            }

            /**
             * Gets the tooltip for the Bold button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getBold() {
                return bold;
            }

            /**
             * Sets the tooltip for the Bold button.
             *
             * @param bold the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setBold(String bold) {
                this.bold = bold;
                return this;
            }

            /**
             * Gets the tooltip for the Italic button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getItalic() {
                return italic;
            }

            /**
             * Sets the tooltip for the Italic button.
             *
             * @param italic the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setItalic(String italic) {
                this.italic = italic;
                return this;
            }

            /**
             * Gets the tooltip for the Quote button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getQuote() {
                return quote;
            }

            /**
             * Sets the tooltip for the Quote button.
             *
             * @param quote the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setQuote(String quote) {
                this.quote = quote;
                return this;
            }

            /**
             * Gets the tooltip for the Code button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getCode() {
                return code;
            }

            /**
             * Sets the tooltip for the Code button.
             *
             * @param code the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setCode(String code) {
                this.code = code;
                return this;
            }

            /**
             * Gets the tooltip for the Link button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getLink() {
                return link;
            }

            /**
             * Sets the tooltip for the Link button.
             *
             * @param link the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setLink(String link) {
                this.link = link;
                return this;
            }

            /**
             * Gets the tooltip for the Unordered list button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getUnorderedList() {
                return unorderedList;
            }

            /**
             * Sets the tooltip for the Unordered list button.
             *
             * @param unorderedList the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setUnorderedList(String unorderedList) {
                this.unorderedList = unorderedList;
                return this;
            }

            /**
             * Gets the tooltip for the Ordered list button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getOrderedList() {
                return orderedList;
            }

            /**
             * Sets the tooltip for the Ordered list button.
             *
             * @param orderedList the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setOrderedList(String orderedList) {
                this.orderedList = orderedList;
                return this;
            }

            /**
             * Gets the tooltip for the Task list button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getTaskList() {
                return taskList;
            }

            /**
             * Sets the tooltip for the Task list button.
             *
             * @param taskList the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setTaskList(String taskList) {
                this.taskList = taskList;
                return this;
            }

            /**
             * Gets the tooltip for the overflow menu button.
             *
             * @return the tooltip, or {@code null} if not set
             */
            public String getOverflow() {
                return overflow;
            }

            /**
             * Sets the tooltip for the overflow menu button.
             *
             * @param overflow the tooltip, or {@code null} to use the default
             * @return this instance for method chaining
             */
            public Toolbar setOverflow(String overflow) {
                this.overflow = overflow;
                return this;
            }
        }
    }
}
