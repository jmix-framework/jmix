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

package io.jmix.ui.meta;

import io.jmix.ui.component.Accordion;
import io.jmix.ui.component.FlowBoxLayout;
import io.jmix.ui.component.GridLayout;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.ResponsiveGridLayout;
import io.jmix.ui.component.ScrollBoxLayout;
import io.jmix.ui.component.SplitPanel;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.VBoxLayout;

/**
 * Specifies standard container type for emulation in Studio Screen Designer.
 */
public enum ContainerType {

    /**
     * Accordion component.
     *
     * @see Accordion
     */
    ACCORDION,

    /**
     * Directional flow layout.
     *
     * @see FlowBoxLayout
     */
    FLOW,

    /**
     * Grid layout.
     *
     * @see GridLayout
     */
    GRID,

    /**
     * GroupBox layout.
     *
     * @see GroupBoxLayout
     */
    GROUP_BOX,

    /**
     * Horizontal layout.
     *
     * @see HBoxLayout
     */
    HORIZONTAL,

    /**
     * Responsive grid layout.
     *
     * @see ResponsiveGridLayout
     */
    RESPONSIVE_GRID,

    /**
     * ScrollBox layout.
     *
     * @see ScrollBoxLayout
     */
    SCROLL_BOX,

    /**
     * Split layout.
     *
     * @see SplitPanel
     */
    SPLIT,

    /**
     * TabSheet component.
     *
     * @see TabSheet
     */
    TAB_SHEET,

    /**
     * Vertical layout.
     *
     * @see VBoxLayout
     */
    VERTICAL
}