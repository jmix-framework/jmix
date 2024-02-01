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

package io.jmix.chartsflowui.kit.component.model.toolbox;

/**
 * A tool feature for restoring a chart options.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#toolbox.feature.restore">Restore documentation</a>
 */
public class RestoreFeature extends AbstractFeature<RestoreFeature> {

    protected String title;

    protected String icon;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        markAsDirty();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        markAsDirty();
    }

    public RestoreFeature withTitle(String title) {
        setTitle(title);
        return this;
    }

    public RestoreFeature withIcon(String icon) {
        setIcon(icon);
        return this;
    }

    @Override
    protected String getFeatureName() {
        return "restore";
    }
}
