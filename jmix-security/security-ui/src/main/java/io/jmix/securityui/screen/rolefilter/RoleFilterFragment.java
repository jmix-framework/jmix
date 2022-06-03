/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityui.screen.rolefilter;

import io.jmix.core.Messages;
import io.jmix.security.model.RoleSource;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@UiController("sec_RoleFilterFragment")
@UiDescriptor("role-filter-fragment.xml")
public class RoleFilterFragment extends ScreenFragment {

    @Autowired
    private Messages messages;
    @Autowired
    private TextField<String> codeFilterField;
    @Autowired
    private TextField<String> nameFilterField;
    @Autowired
    private ComboBox<String> sourceFilterField;
    @Autowired
    private Label sourceFilterLabel;

    private Consumer<RoleFilter> listener;

    public void setChangeListener(Consumer<RoleFilter> listener) {
        this.listener = listener;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        Map<String, String> options = new LinkedHashMap<>();
        options.put(getRoleSourceCaption(RoleSource.ANNOTATED_CLASS), RoleSource.ANNOTATED_CLASS);
        options.put(getRoleSourceCaption(RoleSource.DATABASE), RoleSource.DATABASE);
        sourceFilterField.setOptionsMap(options);
    }

    private String getRoleSourceCaption(String key) {
        return messages.getMessage("io.jmix.securityui.model/roleSource." + key);
    }

    @Subscribe("nameFilterField")
    public void onNameFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        notifyListener();
    }

    @Subscribe("codeFilterField")
    public void onCodeFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        notifyListener();
    }

    @Subscribe("sourceFilterField")
    public void onSourceFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.accept(new RoleFilter(nameFilterField.getValue(), codeFilterField.getValue(), sourceFilterField.getValue()));
        }
    }

    public void setSourceFieldVisible(boolean visible) {
        sourceFilterLabel.setVisible(visible);
        sourceFilterField.setVisible(visible);
    }
}
