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

package com.haulmont.cuba.gui.components;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.screen.Screen;

import java.util.function.Consumer;

/**
 * @deprecated for screens that based on {@link Screen} use {@link io.jmix.ui.component.SplitPanel} instead.
 */
@Deprecated
public interface SplitPanel extends io.jmix.ui.component.SplitPanel, HasSettings {

    /**
     * @deprecated Use {@link #setSplitPosition(int, SizeUnit)}
     */
    @Deprecated
    void setSplitPosition(int pos, int unit);

    /**
     * Sets a position of split from the left side by default.
     * If reversePosition is true position will be set from right.
     *
     * @deprecated Use {@link #setSplitPosition(int, SizeUnit, boolean)}
     */
    @Deprecated
    void setSplitPosition(int pos, int unit, boolean reversePosition);

    /**
     * @return unit of the splitter position.
     * See {@link Component#UNITS_PIXELS} and {@link Component#UNITS_PERCENTAGE}
     */
    @Deprecated
    int getSplitPositionUnit();

    /**
     * Sets the minimum available position of split.
     * Minimum position of split will be set from the right if position is reversed.
     *
     * @deprecated Use {@link #setMinSplitPosition(int, SizeUnit)}
     */
    @Deprecated
    void setMinSplitPosition(int pos, int unit);

    /**
     * Sets the maximum available position of split.
     * Maximum position of split will be set from the right if position is reversed.
     *
     * @deprecated Use {@link #setMaxSplitPosition(int, SizeUnit)}
     */
    @Deprecated
    void setMaxSplitPosition(int pos, int unit);

    /**
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeSplitPositionChangeListener(Consumer<SplitPositionChangeEvent> listener);
}
