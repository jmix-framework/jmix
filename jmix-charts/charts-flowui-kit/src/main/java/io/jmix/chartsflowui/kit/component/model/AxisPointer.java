/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer;
import io.jmix.chartsflowui.kit.component.model.shared.TriggerOnMode;

/**
 * The global option for {@code axisPointer}. AxisPointer is a tool for displaying
 * reference line and axis value under mouse pointer. More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#axisPointer">AxisPointer documentation</a>
 */
public class AxisPointer extends AbstractAxisPointer<AxisPointer> {

    protected String id;

    protected TriggerOnMode triggerOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public TriggerOnMode getTriggerOn() {
        return triggerOn;
    }

    public void setTriggerOn(TriggerOnMode triggerOn) {
        this.triggerOn = triggerOn;
        markAsDirty();
    }

    public AxisPointer withId(String id) {
        setId(id);
        return this;
    }

    public AxisPointer withTriggerOn(TriggerOnMode triggerOn) {
        setTriggerOn(triggerOn);
        return this;
    }
}