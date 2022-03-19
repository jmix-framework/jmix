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

/**
 * Component container which supports expand ratio of components
 */
public interface SupportsExpandRatio {

    /**
     * Sets expand ratio for the component. The ratio must be greater than or equal to 0.
     *
     * @param component component to expand with ratio
     * @param ratio ratio
     */
    void setExpandRatio(Component component, float ratio);

    /**
     * @param component component for which returns ratio
     * @return ratio for the component, 0.0f by default
     */
    float getExpandRatio(Component component);
}
