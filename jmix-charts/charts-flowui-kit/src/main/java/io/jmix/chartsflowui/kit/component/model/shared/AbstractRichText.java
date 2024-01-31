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

import java.util.HashMap;
import java.util.Map;

/**
 * The base class for text with rich option.
 *
 * @param <T> origin rich text class type
 */
public abstract class AbstractRichText<T extends AbstractRichText<T>> extends AbstractText<T> {

    protected Map<String, RichStyle> richStyles;

    public Map<String, RichStyle> getRichStyles() {
        return richStyles;
    }

    public void addRichStyle(String styleName, RichStyle style) {
        if (richStyles == null) {
            richStyles = new HashMap<>();
        }

        richStyles.put(styleName, style);
        addChild(style);
    }

    public void removeRichStyle(String styleName) {
        if (richStyles != null) {
            RichStyle removedStyle = richStyles.remove(styleName);
            removeChild(removedStyle);
        }
    }

    @SuppressWarnings("unchecked")
    public T withRichStyle(String styleName, RichStyle style) {
        addRichStyle(styleName, style);
        return (T) this;
    }
}
