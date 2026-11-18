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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A step of a {@link JmixStepper}. The component is the step's header item ({@code <jmix-step>}); the step's
 * content component is attached to the stepper, which shows only the content of the selected step.
 */
@NullMarked
@Tag("jmix-step")
@JsModule("./src/stepper/jmix-step.js")
public class StepperStep extends Component implements HasStyle {

    protected static final String ICON_SLOT = "icon";

    @Nullable
    protected Component content;
    @Nullable
    protected Component icon;
    @Nullable
    protected JmixStepper stepper;
    protected State state = State.UNAVAILABLE;

    /**
     * @return the step label shown in the header, or {@code null} if not set
     */
    @Nullable
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Sets the step label shown in the header.
     *
     * @param label the label, or {@code null} to remove it
     */
    public void setLabel(@Nullable String label) {
        setStringProperty("label", label);
    }

    /**
     * @return the secondary text shown under the label, or {@code null} if not set
     */
    @Nullable
    public String getDescription() {
        return getElement().getProperty("description");
    }

    /**
     * Sets the secondary text shown under the label.
     *
     * @param description the description, or {@code null} to remove it
     */
    public void setDescription(@Nullable String description) {
        setStringProperty("description", description);
    }

    /**
     * @return the icon shown in the step indicator instead of the step number, or {@code null} if not set
     */
    @Nullable
    public Component getIcon() {
        return icon;
    }

    /**
     * Sets the icon shown in the step indicator instead of the step number.
     *
     * @param icon the icon component, or {@code null} to show the step number
     */
    public void setIcon(@Nullable Component icon) {
        if (this.icon != null) {
            this.icon.getElement().removeAttribute("slot");
            getElement().removeChild(this.icon.getElement());
        }

        this.icon = icon;

        if (icon != null) {
            icon.getElement().setAttribute("slot", ICON_SLOT);
            getElement().appendChild(icon.getElement());
        }
    }

    /**
     * @return the content component of the step, or {@code null} if the step has no content
     */
    @Nullable
    public Component getContent() {
        return content;
    }

    /**
     * Sets the content component of the step.
     *
     * @param content the content component, or {@code null} for a step without content
     */
    public void setContent(@Nullable Component content) {
        Component oldContent = this.content;
        this.content = content;

        if (stepper != null) {
            stepper.onStepContentChanged(this, oldContent);
        }
    }

    /**
     * Shows or hides the step. A hidden step is skipped by the navigation and not shown in the header.
     *
     * @param visible whether the step is visible
     */
    @Override
    public void setVisible(boolean visible) {
        boolean changed = visible != isVisible();
        super.setVisible(visible);

        if (changed && stepper != null) {
            stepper.onStepVisibilityChanged(this);
        }
    }

    /**
     * @return the state of the step in the header; the stepper sets it
     */
    public State getState() {
        return state;
    }

    protected void setState(State state) {
        this.state = state;
        getElement().setProperty("state", state.getValue());
    }

    @Nullable
    protected JmixStepper getStepper() {
        return stepper;
    }

    protected void setStepper(@Nullable JmixStepper stepper) {
        this.stepper = stepper;
    }

    protected void setStringProperty(String name, @Nullable String value) {
        if (value == null) {
            getElement().removeProperty(name);
        } else {
            getElement().setProperty(name, value);
        }
    }

    /**
     * State of a step in the stepper header.
     */
    public enum State {

        /**
         * The selected step.
         */
        CURRENT("current"),

        /**
         * A step before the selected one: it passed its check when the user left it and has not changed since.
         */
        COMPLETED("completed"),

        /**
         * A step after the selected one that the user has already reached.
         */
        VISITED("visited"),

        /**
         * A step after the furthest step the user has reached.
         */
        UNAVAILABLE("unavailable");

        private final String value;

        State(String value) {
            this.value = value;
        }

        /**
         * @return the value of the {@code state} attribute of the {@code <jmix-step>} element
         */
        public String getValue() {
            return value;
        }
    }
}
