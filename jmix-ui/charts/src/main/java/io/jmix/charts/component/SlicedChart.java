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

package io.jmix.charts.component;


import io.jmix.core.common.event.Subscription;
import io.jmix.charts.model.chart.SlicedChartModel;

import java.util.function.Consumer;

/**
 * Base interface for {@link PieChart} and {@link FunnelChart}.
 * <br>
 * See documentation for properties of AmSlicedChart JS object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmSlicedChart">http://docs.amcharts.com/3/javascriptcharts/AmSlicedChart</a>
 */
public interface SlicedChart<T extends SlicedChart> extends Chart<T>, SlicedChartModel<T> {
    /**
     * Adds a listener for a slice. Called when user clicks on the slice.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addSliceClickListener(Consumer<SliceClickEvent> listener);

    /**
     * Adds a listener for a slice. Called when user clicks on the slice.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addSliceRightClickListener(Consumer<SliceRightClickEvent> listener);

    /**
     * Adds a listener for a slice. Called when the slice did pull-in.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addSlicePullInListener(Consumer<SlicePullInEvent> listener);

    /**
     * Adds a listener for a slice. Called when the slice did pull-out.
     *
     * @param listener a listener to add
     * @return subscription
     */
    Subscription addSlicePullOutListener(Consumer<SlicePullOutEvent> listener);
}