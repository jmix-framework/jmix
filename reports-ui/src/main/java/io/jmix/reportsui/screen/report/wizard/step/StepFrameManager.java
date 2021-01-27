/*
 * Copyright (c) 2008-2019 Haulmont.
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

import io.jmix.ui.component.Frame;

import java.util.List;

public class StepFrameManager {

    protected List<StepFrame> stepFrames;
    protected MainWizardFrame mainWizardFrame;
    protected int currentFrameIdx = 0;

    public StepFrameManager(MainWizardFrame reportWizardCreatorFrame, List<StepFrame> stepFrames) {
        this.mainWizardFrame = reportWizardCreatorFrame;
        this.stepFrames = stepFrames;
        //frames initialization is in showCurrentFrame() method
    }

    public List<StepFrame> getStepFrames() {
        return stepFrames;
    }

    public void showCurrentFrame() {
        setMainWindowProps();
        getCurrentStepFrame().initFrame();
        getCurrentStepFrame().beforeShow();
        getCurrentStepFrame().getFrame().setVisible(true);
    }

    protected StepFrame getCurrentStepFrame() {
        return stepFrames.get(currentFrameIdx);
    }

    public Frame getCurrentFrame() {
        return stepFrames.get(currentFrameIdx).getFrame();
    }

    public void setMainWindowProps() {
//        String newWindowCaption = getCurrentStepFrame().getName() + " " +
//                mainWizardFrame..formatMessage("stepNo", currentFrameIdx + 1, stepFrames.size());
//
//        Window window = mainWizardFrame.getMainWizardFrame();
//        window.setCaption(newWindowCaption);

        setNavigationButtonProps();
    }

    protected void setNavigationButtonProps() {
        if (getCurrentStepFrame().isLast()) {
            mainWizardFrame.getForwardBtn().setVisible(false);
        } else if (currentFrameIdx + 1 >= stepFrames.size()) {
            mainWizardFrame.getForwardBtn().setEnabled(false);
        } else {
            mainWizardFrame.getForwardBtn().setVisible(true);
            mainWizardFrame.getForwardBtn().setEnabled(true);
        }

        if (getCurrentStepFrame().isFirst()) {
            mainWizardFrame.getBackwardBtn().setVisible(false);
        } else if (currentFrameIdx - 1 < 0) {
            mainWizardFrame.getBackwardBtn().setEnabled(false);
        } else {
            mainWizardFrame.getBackwardBtn().setVisible(true);
            mainWizardFrame.getBackwardBtn().setEnabled(true);
        }
        mainWizardFrame.removeBtns();
        if (mainWizardFrame.getBackwardBtn().isVisible())
            mainWizardFrame.addBackwardBtn();
        if (mainWizardFrame.getForwardBtn().isVisible())
            mainWizardFrame.addForwardBtn();
        mainWizardFrame.addSaveBtn();
    }

    public boolean prevFrame() {
        if (currentFrameIdx == 0) {
            throw new ArrayIndexOutOfBoundsException("Previous frame is not exists");
        }
        if (!getCurrentStepFrame().isValidateBeforePrev() || validateCurrentFrame()) {
            hideCurrentFrame();
            currentFrameIdx--;
            showCurrentFrame();
            return true;
        } else {
            return false;
        }
    }

    public boolean nextFrame() {
        if (currentFrameIdx > stepFrames.size()) {
            throw new ArrayIndexOutOfBoundsException("Next frame is not exists");
        }
        if (!getCurrentStepFrame().isValidateBeforeNext() || validateCurrentFrame()) {
            hideCurrentFrame();
            currentFrameIdx++;
            showCurrentFrame();
            return true;
        } else {
            return false;
        }
    }

    protected boolean validateCurrentFrame() {
        List<String> validationErrors = getCurrentStepFrame().validateFrame();
        if (!validationErrors.isEmpty()) {
            //todo
//            mainWizardFrame.getMainWizardFrame().showNotification(
//                    org.springframework.util.StringUtils.arrayToDelimitedString(validationErrors.toArray(), "<br/>"),
//                    Frame.NotificationType.TRAY_HTML);
            return false;
        }
        return true;
    }

    protected void hideCurrentFrame() {
        getCurrentStepFrame().beforeHide();
        getCurrentStepFrame().getFrame().setVisible(false);
    }
}