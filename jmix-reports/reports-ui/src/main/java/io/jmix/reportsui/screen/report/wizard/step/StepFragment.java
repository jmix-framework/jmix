/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.screen.report.wizard.step;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.Validatable;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.screen.MessageBundle;
import io.jmix.ui.screen.ScreenFragment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class StepFragment extends ScreenFragment {

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected Metadata metadata;

    protected boolean validateBeforeNext = true;
    protected boolean validateBeforePrev = false;

    /**
     * Sets the caption for the wizard screen
     *
     * @return caption
     */
    public abstract String getCaption();

    /**
     * Sets the description for the wizard screen
     *
     * @return description
     */
    public abstract String getDescription();

    /**
     * Entry point for adding functionality before hiding the fragment
     */
    public void beforeHide() {
    }

    /**
     * Entry point for adding functionality before showing the fragment
     */
    public void beforeShow() {
    }

    /**
     * Entry point for adding functionality after the fragment is shown
     */
    public void afterShow() {

    }

    public void initFragment() {
        for (Component c : getFragment().getComponents()) {
            if (c instanceof Field) {
                Field field = (Field) c;
                if (field.isRequired() && StringUtils.isBlank(field.getRequiredMessage()) && StringUtils.isBlank(field.getCaption())) {
                    field.setRequiredMessage(getDefaultRequiredMessage(messages.getMessage(field.getId())));
                }
            }
        }
    }

    public List<String> validateFragment() {
        List<String> errors = new ArrayList<>();
        for (Component c : getFragment().getComponents()) {
            if (c instanceof Validatable) {
                Validatable validatable = (Validatable) c;
                try {
                    validatable.validate();
                } catch (ValidationException e) {
                    errors.add(e.getMessage());
                }
            }
        }
        return errors;
    }

    public boolean isValidateBeforeNext() {
        return validateBeforeNext;
    }

    public boolean isValidateBeforePrev() {
        return validateBeforePrev;
    }

    protected String getDefaultRequiredMessage(String name) {
        return messages.formatMessage("", "validation.required.defaultMsg", name);
    }
}