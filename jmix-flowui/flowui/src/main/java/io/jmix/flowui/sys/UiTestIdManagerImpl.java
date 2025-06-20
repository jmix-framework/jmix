/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.sys;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.details.Details;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import org.springframework.lang.Nullable;

@org.springframework.stereotype.Component("flowui_UiTestIdManagerImpl")
public class UiTestIdManagerImpl implements UiTestIdManager {

    @Nullable
    @Override
    public String generateUiTestId(Component component) {
        if (component instanceof JmixButton button && button.getAction() != null) {
            return button.getAction().getId();
        }

        if (component instanceof SupportsValueSource<?> supportsValueSourceComponent
                && supportsValueSourceComponent.getValueSource() != null) {
            return UiTestIdUtil.getCalculatedTestId(supportsValueSourceComponent.getValueSource());
        }

        if (component instanceof ListDataComponent<?> listDataComponent
                && listDataComponent.getItems() != null) {
            return UiTestIdUtil.getCalculatedTestId(listDataComponent.getItems(), component);
        }

        if (component instanceof HasLabel hasLabel) {
            String label = ComponentUtils.getLabel(hasLabel);
            if (label != null) {
                return UiTestIdUtil.getNormalizedTestId(label, component);
            }
        }

        if (component instanceof HasText hasText && hasText.getText() != null) {
            return UiTestIdUtil.getNormalizedTestId(hasText.getText(), component);
        }

        if (component instanceof Details details) {
            return UiTestIdUtil.getNormalizedTestId(details.getSummaryText(), component);
        }

        return null;
    }
}
