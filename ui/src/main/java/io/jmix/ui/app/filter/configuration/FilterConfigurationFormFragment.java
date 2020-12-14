/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.app.filter.configuration;

import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ui_FilterConfigurationForm.fragment")
@UiDescriptor("filter-configuration-form-fragment.xml")
public class FilterConfigurationFormFragment extends ScreenFragment {

    @Autowired
    protected TextField<String> codeField;
    @Autowired
    protected TextField<String> captionField;

    protected String caption;
    protected String code;

    public String getConfigurationCaption() {
        return caption;
    }

    public void setConfigurationCaption(String caption) {
        this.caption = caption;
    }

    public String getConfigurationCode() {
        return code;
    }

    public void setConfigurationCode(String code) {
        this.code = code;
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        if (code != null) {
            codeField.setValue(code);
            codeField.setEnabled(false);
        }

        if (caption != null) {
            captionField.setValue(caption);
        }
    }

    @Subscribe("codeField")
    protected void onCodeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        code = event.getValue();
    }

    @Subscribe("captionField")
    protected void onCaptionFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        caption = event.getValue();
    }
}
