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

import io.jmix.chartsflowui.kit.component.model.shared.Align;
import io.jmix.chartsflowui.kit.component.model.shared.VerticalAlign;

public interface HasAlign<T> {

    Align getAlign();

    void setAlign(Align align);

    @SuppressWarnings("unchecked")
    default T withAlign(Align align) {
        setAlign(align);
        return (T) this;
    }

    VerticalAlign getVerticalAlign();

    void setVerticalAlign(VerticalAlign verticalAlign);

    @SuppressWarnings("unchecked")
    default T withVerticalAlign(VerticalAlign verticalAlign) {
        setVerticalAlign(verticalAlign);
        return (T) this;
    }
}
