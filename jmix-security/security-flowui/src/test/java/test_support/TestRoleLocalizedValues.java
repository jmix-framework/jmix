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

package test_support;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.securityflowui.view.rolelocalization.RoleLocalizedValuesView;

import java.util.List;
import java.util.Objects;

/**
 * Works with the fields of the localized values of a role and with the dialog that edits them.
 */
public final class TestRoleLocalizedValues {

    public static final String EDIT_ACTION_ID = "editRoleLocalizedValues";

    private TestRoleLocalizedValues() {
    }

    /**
     * Performs the action of the field and returns the dialog it opens.
     */
    public static RoleLocalizedValuesView editLocalizedValues(JmixValuePicker<String> field) {
        Action action = Objects.requireNonNull(field.getAction(EDIT_ACTION_ID));
        action.actionPerform(field);
        return UiTestUtils.getLastOpenedViewDialog();
    }

    /**
     * @return the text that the field shows collapsed
     */
    public static String getCollapsedValue(JmixValuePicker<String> field) {
        return field.getElement().getProperty("value", "");
    }

    /**
     * @return the fields of the dialog, in the order of the available locales
     */
    @SuppressWarnings("unchecked")
    public static List<TextFieldBase<?, String>> getDialogFields(RoleLocalizedValuesView dialog) {
        FormLayout form = UiTestUtils.getComponent(dialog, "form");
        return form.getChildren()
                .<TextFieldBase<?, String>>map(component -> (TextFieldBase<?, String>) component)
                .toList();
    }

    /**
     * Enters a value into a field of the dialog as the client sends it, so that the field treats it as user input:
     * a server-side {@code setValue} skips what a field does to the input of a user, such as trimming it.
     */
    public static void enterValue(TextFieldBase<?, String> field, String value) {
        try {
            field.getElement().getNode()
                    .getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient("value", value)
                    .run();
        } catch (PropertyChangeDeniedException e) {
            throw new IllegalStateException("The field does not accept its value from the client", e);
        }
    }

    /**
     * Clicks a button of the view, as a user does.
     */
    public static void click(View<?> view, String buttonId) {
        JmixButton button = UiTestUtils.getComponent(view, buttonId);
        button.click();
    }
}
