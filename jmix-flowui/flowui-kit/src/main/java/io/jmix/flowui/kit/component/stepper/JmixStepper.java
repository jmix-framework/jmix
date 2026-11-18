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

package io.jmix.flowui.kit.component.stepper;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server-side component for the {@code <jmix-stepper>} element: ordered {@link StepperStep StepperSteps}, a header
 * that shows every visible step with its state, and the content of the selected step.
 * <p>
 * This class keeps the steps, the selected step, the furthest step reached so far and the state of each step.
 * It applies no navigation rules: a click on a step header only fires {@link StepClickEvent}.
 */
@NullMarked
@Tag("jmix-stepper")
@JsModule("./src/stepper/jmix-stepper.js")
public class JmixStepper extends Component implements HasSize, HasStyle, HasThemeVariant<StepperVariant> {

    protected static final String STEPS_SLOT = "steps";
    protected static final String INACTIVE_CONTENT_SLOT = "inactive";

    protected final List<StepperStep> steps = new ArrayList<>();

    @Nullable
    protected StepperStep selectedStep;
    @Nullable
    protected StepperStep furthestReachedStep;

    protected Orientation orientation = Orientation.HORIZONTAL;

    public JmixStepper() {
        getElement().setProperty("orientation", orientation.getValue());

        addListener(StepClickEvent.class, this::onStepClick);
    }

    /**
     * @return an unmodifiable list of all steps, hidden ones included
     */
    public List<StepperStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Adds a step after the existing ones. The first visible step added becomes the selected step.
     *
     * @param step the step to add
     */
    public void add(StepperStep step) {
        add(step, steps.size());
    }

    /**
     * Inserts a step at the given position. The first visible step added becomes the selected step.
     *
     * @param step  the step to add
     * @param index the position of the step among all steps
     */
    public void add(StepperStep step, int index) {
        Preconditions.checkNotNull(step, "Step cannot be null");
        Preconditions.checkArgument(step.getStepper() == null, "The step is already added to a stepper");
        Preconditions.checkPositionIndex(index, steps.size());

        attachStepElement(step, index);
        steps.add(index, step);
        step.setStepper(this);
        attachContent(step);

        if (selectedStep == null && step.isVisible()) {
            setSelectedStepInternal(step);
        } else {
            updateContent();
            updateStepStates();
        }
    }

    /**
     * Removes a step. Removing the selected step selects the nearest visible step before it, or after it when
     * there is none before.
     *
     * @param step the step to remove
     */
    public void remove(StepperStep step) {
        Preconditions.checkNotNull(step, "Step cannot be null");
        int index = steps.indexOf(step);
        Preconditions.checkArgument(index >= 0, "The step is not added to this stepper");

        boolean selected = step == selectedStep;
        StepperStep replacement = selected ? findReplacementStep(step) : null;
        if (step == furthestReachedStep) {
            furthestReachedStep = index > 0 ? steps.get(index - 1) : null;
        }

        steps.remove(index);
        step.setStepper(null);
        step.getElement().removeAttribute("slot");
        getElement().removeChild(step.getElement());
        detachContent(step.getContent());

        if (selected) {
            replaceSelectedStep(step, replacement);
        } else {
            updateStepStates();
        }
    }

    /**
     * @return the selected step, or {@code null} if no step is visible
     */
    @Nullable
    public StepperStep getSelectedStep() {
        return selectedStep;
    }

    /**
     * @return the orientation of the header
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of the header: a row above the content, or a column beside it.
     *
     * @param orientation the orientation
     */
    public void setOrientation(Orientation orientation) {
        Preconditions.checkNotNull(orientation, "Orientation cannot be null");

        this.orientation = orientation;
        getElement().setProperty("orientation", orientation.getValue());
    }

    protected void setSelectedStepInternal(@Nullable StepperStep step) {
        selectedStep = step;

        if (step != null
                && (furthestReachedStep == null || steps.indexOf(step) > steps.indexOf(furthestReachedStep))) {
            furthestReachedStep = step;
        }

        updateContent();
        updateStepStates();
    }

    /**
     * Replaces the selected step after it has been hidden or removed.
     *
     * @param oldStep the hidden or removed step
     * @param newStep the step to select, or {@code null} if no step is visible
     */
    protected void replaceSelectedStep(StepperStep oldStep, @Nullable StepperStep newStep) {
        setSelectedStepInternal(newStep);
    }

    protected void onStepVisibilityChanged(StepperStep step) {
        if (step == selectedStep && !step.isVisible()) {
            replaceSelectedStep(step, findReplacementStep(step));
        } else if (selectedStep == null && step.isVisible()) {
            setSelectedStepInternal(findFirstVisibleStep());
        } else {
            updateStepStates();
        }
    }

    protected void onStepContentChanged(StepperStep step, @Nullable Component oldContent) {
        detachContent(oldContent);
        attachContent(step);
        updateContent();
    }

    /**
     * Handles a click on a step header. Does nothing on this level: navigation rules belong to subclasses.
     *
     * @param event the click event
     */
    protected void onStepClick(StepClickEvent event) {
        // No navigation rules on this level
    }

    protected void attachStepElement(StepperStep step, int index) {
        step.getElement().setAttribute("slot", STEPS_SLOT);

        if (index < steps.size()) {
            Element nextStepElement = steps.get(index).getElement();
            getElement().insertChild(getElement().indexOfChild(nextStepElement), step.getElement());
        } else {
            getElement().appendChild(step.getElement());
        }
    }

    protected void attachContent(StepperStep step) {
        Component content = step.getContent();
        if (content != null) {
            getElement().appendChild(content.getElement());
        }
    }

    protected void detachContent(@Nullable Component content) {
        if (content != null && getElement().equals(content.getElement().getParent())) {
            content.getElement().removeAttribute("slot");
            getElement().removeChild(content.getElement());
        }
    }

    /**
     * Shows and enables the content of the selected step. Moves the content of every other step to a slot that
     * is not rendered and disables it at the node level, so the client cannot change it.
     */
    protected void updateContent() {
        for (StepperStep step : steps) {
            Component content = step.getContent();
            if (content == null) {
                continue;
            }

            Element contentElement = content.getElement();
            if (step == selectedStep) {
                contentElement.removeAttribute("slot");
                contentElement.setEnabled(true);
            } else {
                contentElement.setAttribute("slot", INACTIVE_CONTENT_SLOT);
                // Can't use setEnabled(false): it would also mark the elements as disabled in the DOM
                contentElement.getNode().setEnabled(false);
            }
        }
    }

    /**
     * Sends the state, the number and the last-step flag of every step to the client.
     */
    protected void updateStepStates() {
        int selectedIndex = selectedStep != null ? steps.indexOf(selectedStep) : -1;
        int furthestIndex = furthestReachedStep != null ? steps.indexOf(furthestReachedStep) : -1;
        StepperStep lastVisibleStep = findLastVisibleStep();

        int number = 0;
        for (int i = 0; i < steps.size(); i++) {
            StepperStep step = steps.get(i);
            if (step.isVisible()) {
                number++;
            }

            Element element = step.getElement();
            element.setProperty("number", number);
            element.setProperty("last", step == lastVisibleStep);
            step.setState(getStepState(i, selectedIndex, furthestIndex));
        }
    }

    protected StepperStep.State getStepState(int index, int selectedIndex, int furthestIndex) {
        if (index == selectedIndex) {
            return StepperStep.State.CURRENT;
        } else if (index < selectedIndex) {
            return StepperStep.State.COMPLETED;
        } else if (index <= furthestIndex) {
            return StepperStep.State.VISITED;
        } else {
            return StepperStep.State.UNAVAILABLE;
        }
    }

    @Nullable
    protected StepperStep findFirstVisibleStep() {
        return steps.stream()
                .filter(Component::isVisible)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    protected StepperStep findLastVisibleStep() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            if (steps.get(i).isVisible()) {
                return steps.get(i);
            }
        }

        return null;
    }

    @Nullable
    protected StepperStep findNextVisibleStep(StepperStep step) {
        for (int i = steps.indexOf(step) + 1; i < steps.size(); i++) {
            if (steps.get(i).isVisible()) {
                return steps.get(i);
            }
        }

        return null;
    }

    @Nullable
    protected StepperStep findPreviousVisibleStep(StepperStep step) {
        for (int i = steps.indexOf(step) - 1; i >= 0; i--) {
            if (steps.get(i).isVisible()) {
                return steps.get(i);
            }
        }

        return null;
    }

    @Nullable
    protected StepperStep findReplacementStep(StepperStep step) {
        StepperStep previous = findPreviousVisibleStep(step);
        return previous != null ? previous : findNextVisibleStep(step);
    }

    /**
     * Orientation of the stepper header.
     */
    public enum Orientation {

        /**
         * The headers in a row above the content.
         */
        HORIZONTAL("horizontal"),

        /**
         * The headers in a column on the inline-start side of the content.
         */
        VERTICAL("vertical");

        private final String value;

        Orientation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Fired when the user clicks a step header, or presses Enter or Space on it, for a completed or visited step.
     */
    @DomEvent("jmix-stepper-step-click")
    public static class StepClickEvent extends ComponentEvent<JmixStepper> {

        protected final int stepIndex;

        public StepClickEvent(JmixStepper source, boolean fromClient,
                              @EventData("event.detail.index") int stepIndex) {
            super(source, fromClient);
            this.stepIndex = stepIndex;
        }

        /**
         * @return the index of the step among all steps, hidden ones included
         */
        public int getStepIndex() {
            return stepIndex;
        }

        /**
         * @return the clicked step, or {@code null} if the index is out of range
         */
        @Nullable
        public StepperStep getStep() {
            List<StepperStep> steps = getSource().getSteps();
            return stepIndex >= 0 && stepIndex < steps.size() ? steps.get(stepIndex) : null;
        }
    }
}
