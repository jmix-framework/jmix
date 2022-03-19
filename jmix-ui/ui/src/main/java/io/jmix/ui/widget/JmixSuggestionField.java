/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.widget;

import java.util.Objects;

public class JmixSuggestionField<V> extends JmixAbstractSuggestionField<V, V> {

    @Override
    protected void doSetValue(V value) {
        super.doSetValue(value);

        updateTextPresentation(value);
    }

    public void updateTextPresentation(V value) {
        String stringValue = textViewConverter.apply(value);

        if (!Objects.equals(getState(false).text, stringValue)) {
            getState().text = stringValue;
        }
    }

    @Override
    protected void onSelectSuggestion(String suggestionId) {
        V suggestion = keyMapper.get(suggestionId);
        setValue(suggestion, true);

        updateTextPresentation(getValue());
    }
}
