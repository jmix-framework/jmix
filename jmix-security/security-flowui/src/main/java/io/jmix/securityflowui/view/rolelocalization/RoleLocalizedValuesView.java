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

package io.jmix.securityflowui.view.rolelocalization;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextFieldBase;
import io.jmix.core.CoreProperties;
import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Edits a bundle of the localized values of a role, its localized names or its localized descriptions: a field for
 * every available locale, the default one included, since the name or the description of the role is the fallback of
 * every locale. The entries of locales that are not available are kept.
 */
@ViewController(id = "sec_RoleLocalizedValuesView")
@ViewDescriptor(path = "role-localized-values-view.xml")
@DialogMode(width = "32em")
public class RoleLocalizedValuesView extends StandardView {

    @ViewComponent
    private FormLayout form;
    @ViewComponent
    private Action saveAction;

    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private UiViewProperties uiViewProperties;

    private final Map<String, TextFieldBase<?, String>> fields = new LinkedHashMap<>();

    @Nullable
    private String localizedValues;
    private Map<String, String> storedEntries = Map.of();

    private boolean storedBundleReadable = true;
    private boolean multiline;

    @Subscribe
    public void onInit(InitEvent event) {
        initSaveShortcut();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        readStoredEntries();
        initFields();
    }

    @Subscribe("saveAction")
    public void onSaveActionPerformed(ActionPerformedEvent event) {
        localizedValues = collectLocalizedValues();
        close(StandardOutcome.SAVE);
    }

    /**
     * Sets the bundle that the dialog edits, as the role keeps it.
     */
    @NullMarked
    public void setLocalizedValues(@Nullable String localizedValues) {
        this.localizedValues = localizedValues;
    }

    /**
     * @return the edited bundle once the dialog is saved, otherwise the bundle it was given
     */
    @NullMarked
    @Nullable
    public String getLocalizedValues() {
        return localizedValues;
    }

    /**
     * Sets whether the values are edited in text areas, as descriptions are.
     */
    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    /**
     * Binds the save action to the application's save shortcut, if the application sets one. The shortcut is not
     * declared in the descriptor, where an unset shortcut fails the loading of the view.
     */
    protected void initSaveShortcut() {
        KeyCombination kc = KeyCombination.create(uiViewProperties.getSaveShortcut());
        if (kc != null) {
            // The field in focus commits its value before the action runs, so that a value typed just before the
            // shortcut is saved.
            kc.setResetFocusOnActiveElement(true);
            saveAction.setShortcutCombination(kc);
        }
    }

    protected void readStoredEntries() {
        try {
            Map<String, String> entries = new LinkedHashMap<>(RoleLocalizedValuesUtils.read(localizedValues));
            // A blank entry counts as no entry, as it does where the role is shown, so that confirming the dialog
            // without a change keeps a bundle that holds one as it is.
            entries.values().removeIf(StringUtils::isBlank);
            storedEntries = entries;
        } catch (IllegalArgumentException e) {
            // The role is shown with its default texts for such a bundle anyway, so the dialog replaces it.
            storedBundleReadable = false;
            storedEntries = Map.of();
        }
    }

    protected void initFields() {
        for (Locale locale : coreProperties.getAvailableLocales()) {
            String key = LocaleResolver.localeToString(locale);
            TextFieldBase<?, String> field = createField();

            field.setLabel(messageTools.getLocaleDisplayName(locale));
            field.setValue(storedEntries.getOrDefault(key, ""));

            form.add(field);
            fields.put(key, field);
        }
    }

    @NullMarked
    protected TextFieldBase<?, String> createField() {
        if (multiline) {
            JmixTextArea textArea = uiComponents.create(JmixTextArea.class);
            textArea.setHeight("9.5em");
            return textArea;
        }

        @SuppressWarnings("unchecked")
        TypedTextField<String> textField = uiComponents.create(TypedTextField.class);
        return textField;
    }

    @NullMarked
    @Nullable
    protected String collectLocalizedValues() {
        Map<String, String> entries = new LinkedHashMap<>(storedEntries);
        for (Map.Entry<String, TextFieldBase<?, String>> localeField : fields.entrySet()) {
            String key = localeField.getKey();
            // A text area gives null for a value that a user cleared, since it trims user input to null.
            String value = localeField.getValue().getValue();
            if (StringUtils.isBlank(value)) {
                entries.remove(key);
            } else {
                entries.put(key, value);
            }
        }

        // Confirming the dialog without a change keeps the bundle as it is, so that the role is not modified.
        if (storedBundleReadable && entries.equals(storedEntries)) {
            return localizedValues;
        }

        return RoleLocalizedValuesUtils.write(entries);
    }
}
