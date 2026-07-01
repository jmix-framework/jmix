/*
 * Copyright 2024 Haulmont.
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

package io.jmix.chartsflowui.kit.component.event.dto;

import io.jmix.chartsflowui.kit.component.event.ChartMagicTypeChangedEvent;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * @see ChartMagicTypeChangedEvent
 */
public class ChartMagicTypeChangedEventDetail extends BaseChartEventDetail {

    protected String currentType;

    protected Map<String, Object> newOption;

    protected String featureName;

    @Nullable
    public String getCurrentType() {
        return currentType;
    }

    public void setCurrentType(String currentType) {
        this.currentType = currentType;
    }

    @Nullable
    public Map<String, Object> getNewOption() {
        return newOption;
    }

    public void setNewOption(Map<String, Object> newOption) {
        this.newOption = newOption;
    }

    @Nullable
    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
}
