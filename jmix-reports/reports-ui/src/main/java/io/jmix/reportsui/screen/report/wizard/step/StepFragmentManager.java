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

import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component("report_StepFragmentManager")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StepFragmentManager {

    @Autowired
    protected Messages messages;

    @Autowired
    protected Notifications notifications;

    protected List<StepFragment> stepFragments;
    protected WizardScreen wizardScreen;
    protected int currentFragmentIdx = 0;

    public void setStepFragments(List<StepFragment> stepFragments) {
        this.stepFragments = stepFragments;

        updateWizardCaption();
    }

    public void setWizardFragment(WizardScreen wizardScreen) {
        this.wizardScreen = wizardScreen;
    }

    public void showCurrentFragment() {
        updateWizardCaption();
        updateWizardDescription();

        visibleButtons();

        getCurrentStepFragment().initFragment();
        getCurrentStepFragment().beforeShow();
        getCurrentStepFragment().getFragment().setVisible(true);
        getCurrentStepFragment().afterShow();
    }

    protected StepFragment getCurrentStepFragment() {
        return stepFragments.get(currentFragmentIdx);
    }

    public void updateWizardDescription() {
        wizardScreen.setDescription(getCurrentStepFragment().getDescription());
    }

    public void updateWizardCaption() {
        wizardScreen.setCaption(messages.formatMessage(getClass(), "stepNo",
                getCurrentStepFragment().getCaption(),
                currentFragmentIdx + 1,
                stepFragments.size())
        );
    }

    protected void visibleButtons() {
        if (currentFragmentIdx <= 0) {
            wizardScreen.getForwardBtn().setVisible(true);
            wizardScreen.getBackwardBtn().setVisible(false);
            wizardScreen.getSaveBtn().setVisible(false);
        } else if (currentFragmentIdx >= stepFragments.size() - 1) {
            wizardScreen.getForwardBtn().setVisible(false);
            wizardScreen.getBackwardBtn().setVisible(true);
            wizardScreen.getSaveBtn().setVisible(true);
        } else {
            wizardScreen.getBackwardBtn().setVisible(true);
            wizardScreen.getForwardBtn().setVisible(true);
            wizardScreen.getSaveBtn().setVisible(false);
        }
    }

    public boolean prevFragment() {
        if (currentFragmentIdx == 0) {
            throw new ArrayIndexOutOfBoundsException("Previous step is not exists");
        }
        if (!getCurrentStepFragment().isValidateBeforePrev() || validateCurrentFragment()) {
            hideCurrentFrame();
            currentFragmentIdx--;
            showCurrentFragment();
            return true;
        } else {
            return false;
        }
    }

    public boolean nextFragment() {
        if (currentFragmentIdx > stepFragments.size()) {
            throw new ArrayIndexOutOfBoundsException("Next step is not exists");
        }
        if (!getCurrentStepFragment().isValidateBeforeNext() || validateCurrentFragment()) {
            hideCurrentFrame();
            currentFragmentIdx++;
            showCurrentFragment();
            return true;
        } else {
            return false;
        }
    }

    public boolean validateCurrentFragment() {
        List<String> validationErrors = getCurrentStepFragment().validateFragment();
        if (!validationErrors.isEmpty()) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withHtmlSanitizer(true)
                    .withCaption(StringUtils.arrayToDelimitedString(validationErrors.toArray(), "<br/>"))
                    .show();
            return false;
        }
        return true;
    }

    protected void hideCurrentFrame() {
        getCurrentStepFragment().beforeHide();
        getCurrentStepFragment().getFragment().setVisible(false);
    }
}