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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.shared.Decal;

/**
 * Same as {@link AbstractItemStyle} but with a decal pattern style.
 */
public class ItemStyleWithDecal extends AbstractItemStyle<ItemStyleWithDecal> {

    protected Decal decal;

    public Decal getDecal() {
        return decal;
    }

    public void setDecal(Decal decal) {
        this.decal = decal;
        markAsDirty();
    }

    public ItemStyleWithDecal withDecal(Decal decal) {
        setDecal(decal);
        return this;
    }
}
