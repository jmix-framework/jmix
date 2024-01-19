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

package io.jmix.chartsflowui.kit.component.model.shared;

public class AreaStyle extends AbstractAreaStyle<AreaStyle> {

    protected Color[] colors;

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color firstColor, Color secondColor) {
        this.colors = new Color[]{firstColor, secondColor};
        markAsDirty();
    }

    public AreaStyle withColors(Color firstColor, Color secondColor) {
        setColors(firstColor, secondColor);
        return this;
    }
}
