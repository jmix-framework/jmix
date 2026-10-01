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

import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;

import java.util.Collection;

public final class TestRoleFilters {

    private TestRoleFilters() {
    }

    /**
     * Enters the text into the name field of the role filter of a view.
     */
    public static void enterName(View<?> view, String text) {
        RoleFilter roleFilter = UiComponentUtils.getComponents(view).stream()
                .filter(RoleFilter.class::isInstance)
                .map(RoleFilter.class::cast)
                .findFirst()
                .orElseThrow();

        // The name field is the first field of the filter.
        TextField nameField = roleFilter.getChildren()
                .map(UiComponentUtils::getComponents)
                .flatMap(Collection::stream)
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .findFirst()
                .orElseThrow();
        nameField.setValue(text);
    }
}
