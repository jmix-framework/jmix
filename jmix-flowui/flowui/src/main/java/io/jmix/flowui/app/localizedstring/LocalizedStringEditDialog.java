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

package io.jmix.flowui.app.localizedstring;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.core.CoreProperties;
import io.jmix.core.LocaleResolver;
import io.jmix.core.LocalizedStringSupport;
import io.jmix.core.LocalizedStringValue;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.ViewValidation;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Edits a localized string value: one field for the default value, one for every available locale, and one for
 * every locale the stored value carries that is not available, so that nothing is edited blindly.
 * <p>
 * The view is opened by {@code value_localizedStringEdit} and returns the canonical value through
 * {@link #getValue()} when it closes with {@link StandardOutcome#SAVE}. A value that was not edited closes the
 * view without that outcome, so the caller writes nothing.
 * <p>
 * The fields are built when the view is shown, because the locale keys are known only then. Each carries an id
 * — {@link #DEFAULT_FIELD_ID}, and {@link #LOCALE_FIELD_ID_PREFIX} followed by the locale key — so it is
 * reachable the way any component of a view is.
 */
@ViewController("flowui_LocalizedStringEditDialog")
@ViewDescriptor("localized-string-edit-dialog.xml")
@DialogMode(width = "30em")
public class LocalizedStringEditDialog extends StandardView {

    public static final String DEFAULT_FIELD_ID = "defaultField";
    public static final String LOCALE_FIELD_ID_PREFIX = "localeField_";

    /**
     * The character a single-line field cannot carry. This is a property of the input component, not of the
     * stored format, which {@link LocalizedStringSupport} owns.
     */
    protected static final char LINE_BREAK = '\n';

    protected static final String MESSAGE_KEY_PREFIX = "localizedStringEditDialog.";

    @Autowired
    protected LocalizedStringSupport localizedStringSupport;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ViewValidation viewValidation;

    @ViewComponent
    protected JmixFormLayout fieldsForm;

    @Nullable
    protected String initialValue;
    @Nullable
    protected String value;

    protected boolean multiline;
    protected boolean required;
    @Nullable
    protected Integer maxLength;

    protected HasValueAndElement<?, String> defaultField;
    protected final Map<String, HasValueAndElement<?, String>> localeFields = new LinkedHashMap<>();
    /**
     * The keys in the order the stored value carried them.
     */
    protected final List<String> storedKeys = new ArrayList<>();

    @Subscribe
    protected void onReady(ReadyEvent event) {
        setupInitialValue();
    }

    protected void setupInitialValue() {
        LocalizedStringValue parsed = localizedStringSupport.parse(initialValue);
        storedKeys.addAll(parsed.values().keySet());

        boolean pureReference = localizedStringSupport.isMessageReference(initialValue) && !parsed.isLocalized();
        boolean multilineFields = isMultilineContent(parsed);

        defaultField = createField(DEFAULT_FIELD_ID, message("defaultValue.label"), multilineFields);
        defaultField.setValue(parsed.defaultValue());
        fieldsForm.add((Component) defaultField);

        for (String key : fieldKeys(parsed)) {
            HasValueAndElement<?, String> field =
                    createField(LOCALE_FIELD_ID_PREFIX + key, localeLabel(key), multilineFields);
            field.setValue(Strings.nullToEmpty(parsed.values().get(key)));

            if (pureReference) {
                field.setReadOnly(true);
                if (field instanceof HasHelper hasHelper) {
                    hasHelper.setHelperText(message("messageReference.helperText"));
                }
            }

            localeFields.put(key, field);
            fieldsForm.add((Component) field);
        }
    }

    /**
     * @return the locale keys to show: every available locale when more than one is configured, plus every
     * locale the stored value carries, so that no entry is edited without being visible
     */
    protected Set<String> fieldKeys(LocalizedStringValue value) {
        Set<String> keys = new LinkedHashSet<>();
        if (coreProperties.getAvailableLocales().size() > 1) {
            for (Locale locale : coreProperties.getAvailableLocales()) {
                keys.add(localizedStringSupport.localeKey(locale));
            }
        }

        keys.addAll(value.values().keySet());
        return keys;
    }

    protected String localeLabel(String localeKey) {
        try {
            return messageTools.getLocaleDisplayName(LocaleResolver.resolve(localeKey));
        } catch (RuntimeException e) {
            return localeKey;
        }
    }

    /**
     * A single-line field would drop the line breaks of a value that already has them, and the shortened text
     * would then be saved, so the stored value switches the fields to multi-line on its own.
     */
    protected boolean isMultilineContent(LocalizedStringValue value) {
        return multiline
                || value.defaultValue().indexOf(LINE_BREAK) >= 0
                || value.values()
                .values()
                .stream()
                .anyMatch(text -> text.indexOf(LINE_BREAK) >= 0);
    }

    protected HasValueAndElement<?, String> createField(String id, String label, boolean multilineField) {
        if (multilineField) {
            JmixTextArea field = uiComponents.create(JmixTextArea.class);
            field.setId(id);
            field.setLabel(label);
            field.setHeight("9.5em");
            field.addValidator(this::checkNoEntryLine);
            return field;
        }

        //noinspection unchecked
        TypedTextField<String> field = uiComponents.create(TypedTextField.class);
        field.setId(id);
        field.setLabel(label);
        field.addValidator(this::checkNoEntryLine);
        return field;
    }

    /**
     * Refuses a value that would be read back as a locale entry. It is a field validator rather than a rule of
     * the view, so that the field itself reports and clears the error, as it does for any other validator.
     */
    protected void checkNoEntryLine(@Nullable String value) {
        if (localizedStringSupport.containsEntryLine(value)) {
            throw new ValidationException(message("validation.entryLine"));
        }
    }

    /**
     * Validates the fields and, when valid, keeps the canonical value and closes with
     * {@link StandardOutcome#SAVE}.
     * <p>
     * A value that was not edited is neither validated nor returned: writing it would report the entity as
     * modified without a semantic change, and a value stored before must stay saveable whatever it holds. The
     * required rule is the exception and runs on every save, because it is about the value the attribute keeps.
     */
    @Subscribe("saveAndCloseBtn")
    protected void onSaveAndCloseBtnClick(ClickEvent<JmixButton> event) {
        LocalizedStringValue collected = collectValue();

        ValidationErrors errors = validateRequired(collected);
        if (!errors.isEmpty()) {
            showErrors(errors);
            return;
        }

        String formatted = localizedStringSupport.format(collected);
        if (formatted.equals(Strings.nullToEmpty(initialValue))) {
            close(StandardOutcome.CLOSE);
            return;
        }

        errors = validate(collected);
        if (!errors.isEmpty()) {
            showErrors(errors);
            return;
        }

        // An entirely empty value is written as null, the way the picker is cleared by ValueClearAction.
        value = formatted.isEmpty() ? null : formatted;
        close(StandardOutcome.SAVE);
    }

    /**
     * Collects the value from the fields. Every stored entry has a field, so a blank field drops its entry.
     * The entries keep the order the stored value carried them in and a new entry is appended after them, so
     * that a value that was not edited is collected back as it was stored.
     */
    protected LocalizedStringValue collectValue() {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : storedKeys) {
            collectEntry(values, key);
        }

        for (String key : localeFields.keySet()) {
            collectEntry(values, key);
        }

        return new LocalizedStringValue(Strings.nullToEmpty(defaultField.getValue()), values);
    }

    protected void collectEntry(Map<String, String> values, String key) {
        HasValueAndElement<?, String> field = localeFields.get(key);
        if (field == null) {
            return;
        }

        String text = Strings.nullToEmpty(field.getValue());
        if (!text.isBlank()) {
            values.put(key, text);
        }
    }

    protected ValidationErrors validateRequired(LocalizedStringValue value) {
        boolean empty = value.defaultValue().isBlank() && value.values().isEmpty();
        return empty && required
                ? error(defaultField, message("validation.required"))
                : ValidationErrors.none();
    }

    protected ValidationErrors validate(LocalizedStringValue value) {
        ValidationErrors errors = viewValidation.validateUiComponents(fieldsForm);
        if (!errors.isEmpty()) {
            return errors;
        }

        if (localizedStringSupport.isMessageReference(value.defaultValue()) && value.isLocalized()) {
            return error(defaultField, message("validation.referenceWithEntries"));
        }

        if (value.isLocalized() && value.defaultValue().isBlank()) {
            Locale appLocale = messageTools.getDefaultLocale();
            if (value.getValue(localizedStringSupport.localeKey(appLocale)) == null) {
                return error(defaultField, message("validation.defaultOrAppLocale",
                        messageTools.getLocaleDisplayName(appLocale)));
            }
        }

        String formatted = localizedStringSupport.format(value);
        if (maxLength != null && formatted.length() > maxLength) {
            return error(defaultField, message("validation.length", formatted.length(), maxLength));
        }

        return ValidationErrors.none();
    }

    protected void showErrors(ValidationErrors errors) {
        viewValidation.showValidationErrors(errors);
        viewValidation.focusProblemComponent(errors);
    }

    protected ValidationErrors error(HasValueAndElement<?, String> field, String description) {
        ValidationErrors errors = new ValidationErrors();
        errors.add((Component) field, description);
        return errors;
    }

    protected String message(String key, Object... params) {
        String message = messages.getMessage(LocalizedStringEditDialog.class, MESSAGE_KEY_PREFIX + key);
        return params.length == 0 ? message : String.format(message, params);
    }

    public void setValue(@Nullable String value) {
        this.initialValue = value;
    }

    /**
     * @return the canonical value, or {@code null} while the view has not been closed with
     * {@link StandardOutcome#SAVE}
     */
    @Nullable
    public String getValue() {
        return value;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setMaxLength(@Nullable Integer maxLength) {
        this.maxLength = maxLength;
    }
}
