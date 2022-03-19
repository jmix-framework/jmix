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

package io.jmix.charts.model.settings;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Responsive setting for chart. See documentation for responsive plugin.<br>
 *
 * <a href="https://github.com/amcharts/responsive">https://github.com/amcharts/responsive</a>
 */
@StudioElement(
        caption = "Responsive",
        xmlElement = "responsive",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Responsive extends AbstractChartObject {

    private static final long serialVersionUID = -7360797549413731632L;

    private Boolean enabled;
    private List<Rule> rules;

    public Boolean isEnabled() {
        return enabled;
    }

    @StudioProperty
    public Responsive setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<Rule> getRules() {
        return rules;
    }

    @StudioElementsGroup(caption = "Rules", xmlElement = "rules")
    public Responsive setRules(List<Rule> rules) {
        this.rules = rules;
        return this;
    }

    public Responsive addRule(Rule... rules) {
        if (rules != null) {
            if (this.rules == null) {
                this.rules = new ArrayList<>();
            }
            this.rules.addAll(Arrays.asList(rules));
        }
        return this;
    }
}