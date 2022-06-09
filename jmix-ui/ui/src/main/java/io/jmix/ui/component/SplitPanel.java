/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.*;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * A split panel contains two components and lays them vertically or horizontally.
 */
@StudioComponent(
        caption = "VerticalSplitPanel",
        category = "Containers",
        xmlElement = "split",
        icon = "io/jmix/ui/icon/container/verticalSplitPanel.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.SPLIT,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/split-panel.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "reversePosition", type = PropertyType.BOOLEAN, defaultValue = "false"),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "200px"),
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "100px")
        }
)
public interface SplitPanel extends ComponentContainer, Component.BelongToFrame, Component.HasIcon,
        Component.HasCaption, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {

    String NAME = "split";

    int ORIENTATION_VERTICAL = 0;
    int ORIENTATION_HORIZONTAL = 1;

    /**
     * Specifies SplitPanel docking direction.
     */
    enum DockMode {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    /**
     * @return a split panel orientation
     */
    int getOrientation();

    /**
     * Sets a split panel orientation.
     *
     * @param orientation a split panel orientation
     * @see #ORIENTATION_VERTICAL
     * @see #ORIENTATION_HORIZONTAL
     */
    void setOrientation(int orientation);

    /**
     * Sets a position of split from the left side by default.
     *
     * @param pos the new size of the first region.
     */
    void setSplitPosition(int pos);

    /**
     * Sets a position of split from the left side by default.
     *
     * @param pos  the new size of the first region.
     * @param unit the unit (from {@link SizeUnit}) in which the size is given.
     */
    @StudioProperty(name = "pos", type = PropertyType.STRING, defaultValue = "50")
    void setSplitPosition(int pos, SizeUnit unit);

    /**
     * Sets a position of split from the left side by default.
     * If reversePosition is true position will be set from right.
     *
     * @param pos             the new size of the first region.
     * @param unit            the unit (from {@link SizeUnit}) in which the size is given.
     * @param reversePosition if set to true the split splitter position is measured
     *                        by the second region else it is measured by the first region
     */
    void setSplitPosition(int pos, SizeUnit unit, boolean reversePosition);

    /**
     * @return position of the splitter.
     */
    float getSplitPosition();

    /**
     * Returns the unit of position of the splitter.
     *
     * @return unit of position of the splitter
     */
    SizeUnit getSplitPositionSizeUnit();

    /**
     * Return from which side position is set.
     */
    boolean isSplitPositionReversed();

    /**
     * Sets the minimum split position to the given position and unit. If the
     * split position is reversed, maximum and minimum are also reversed.
     *
     * @param pos  the new size of the first region.
     * @param unit the unit (from {@link SizeUnit}) in which the size is given.
     */
    @StudioProperty(name = "minSplitPosition", type = PropertyType.STRING)
    void setMinSplitPosition(int pos, SizeUnit unit);

    /**
     * Returns the minimum position of the splitter.
     *
     * @return minimum position of the splitter
     */
    float getMinSplitPosition();

    /**
     * Returns the unit of the minimum position of the splitter.
     *
     * @return unit of the minimum position of the splitter
     */
    SizeUnit getMinSplitPositionSizeUnit();

    /**
     * Sets the maximum split position to the given position and unit. If the
     * split position is reversed, maximum and minimum are also reversed.
     *
     * @param pos  the new size of the first region.
     * @param unit the unit (from {@link SizeUnit}) in which the size is given.
     */
    @StudioProperty(name = "maxSplitPosition", type = PropertyType.STRING)
    void setMaxSplitPosition(int pos, SizeUnit unit);

    /**
     * Returns the maximum position of the splitter.
     *
     * @return maximum position of the splitter
     */
    float getMaxSplitPosition();

    /**
     * Returns the unit of the maximum position of the splitter.
     *
     * @return unit of the maximum position of the splitter
     */
    SizeUnit getMaxSplitPositionSizeUnit();

    /**
     * Sets whether users are able to change the separator position or not.
     *
     * @param locked locked
     */
    @StudioProperty(defaultValue = "false")
    void setLocked(boolean locked);

    /**
     * @return whether users are able to change the separator position or not.
     */
    boolean isLocked();

    /**
     * Enables or disables SplitPanel dock button.
     *
     * @param dockable dockable
     */
    @StudioProperty(defaultValue = "false")
    void setDockable(boolean dockable);

    /**
     * @return whether dock button is enabled or not
     */
    boolean isDockable();

    /**
     * Sets docking direction.
     *
     * @param dockMode one of {@link DockMode} options
     */
    void setDockMode(DockMode dockMode);

    /**
     * @return docking direction
     */
    DockMode getDockMode();

    /**
     * Adds a listener for {@link SplitPositionChangeEvent}s fired by a SplitPanel.
     *
     * @param listener a listener to add
     */
    Subscription addSplitPositionChangeListener(Consumer<SplitPositionChangeEvent> listener);

    /**
     * Event that indicates a change in SplitPanel's splitter position.
     */
    class SplitPositionChangeEvent extends EventObject implements HasUserOriginated {
        private final float previousPosition;
        private final float newPosition;
        private final boolean userOriginated;

        public SplitPositionChangeEvent(SplitPanel splitPanel, float previousPosition, float newPosition) {
            this(splitPanel, previousPosition, newPosition, false);
        }

        public SplitPositionChangeEvent(Object source,
                                        float previousPosition, float newPosition, boolean userOriginated) {
            super(source);
            this.previousPosition = previousPosition;
            this.newPosition = newPosition;
            this.userOriginated = userOriginated;
        }

        @Override
        public SplitPanel getSource() {
            return (SplitPanel) super.getSource();
        }

        public float getPreviousPosition() {
            return previousPosition;
        }

        public float getNewPosition() {
            return newPosition;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }
}