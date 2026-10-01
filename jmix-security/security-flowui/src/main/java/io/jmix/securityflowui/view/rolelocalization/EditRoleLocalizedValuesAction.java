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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.CoreProperties;
import io.jmix.core.LocaleResolver;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.SupportsFormatter;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Edits the bundle of localized values that its picker holds in {@link RoleLocalizedValuesView} and sets the edited
 * bundle to the picker when the dialog is saved. Attached to its picker, it shows the bundle collapsed as the keys of
 * the available locales that have a value.
 */
@ActionType(EditRoleLocalizedValuesAction.ID)
public class EditRoleLocalizedValuesAction
        extends PickerAction<EditRoleLocalizedValuesAction, PickerComponent<String>, String> {

    public static final String ID = "sec_editRoleLocalizedValues";

    protected DialogWindows dialogWindows;
    protected CoreProperties coreProperties;

    protected boolean multiline;

    public EditRoleLocalizedValuesAction() {
        this(ID);
    }

    public EditRoleLocalizedValuesAction(String id) {
        super(id);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    protected void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.editRoleLocalizedValues");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        // The icon may be set in initAction(), which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.GLOBE);
        }
    }

    /**
     * Sets whether the values are edited in text areas, as descriptions are.
     */
    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    @NullMarked
    @SuppressWarnings("unchecked")
    @Override
    public void setTarget(@Nullable PickerComponent<String> target) {
        super.setTarget(target);

        // The picker is attached while its descriptor is loaded, before the role gives it a value: the formatter
        // applies to the values the picker shows after it is set.
        if (target instanceof SupportsFormatter<?> supportsFormatter) {
            ((SupportsFormatter<String>) supportsFormatter).setFormatter(this::formatLocales);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        checkTarget();

        View<?> origin = UiComponentUtils.getView((Component) target);
        String localizedValues = ((HasValue<?, String>) target).getValue();
        String title = ((HasLabel) target).getLabel();

        dialogWindows.view(origin, RoleLocalizedValuesView.class)
                .withViewConfigurer(view -> {
                    view.setLocalizedValues(localizedValues);
                    view.setMultiline(multiline);
                    view.setPageTitle(title);
                })
                .withAfterCloseListener(event -> {
                    if (event.closedWith(StandardOutcome.SAVE)) {
                        target.setValueFromClient(event.getView().getLocalizedValues());
                    }
                })
                .open();
    }

    @NullMarked
    protected String formatLocales(@Nullable String localizedValues) {
        Map<String, String> entries;
        try {
            entries = RoleLocalizedValuesUtils.read(localizedValues);
        } catch (IllegalArgumentException e) {
            // The role is shown with its default texts for such a bundle, so no locale has a value.
            return "";
        }

        return coreProperties.getAvailableLocales().stream()
                .map(LocaleResolver::localeToString)
                .filter(key -> StringUtils.isNotBlank(entries.get(key)))
                .collect(Collectors.joining(", "));
    }
}
