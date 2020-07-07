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

package io.jmix.ui.component.autocomplete.impl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HintResponse {
    private final List<Option> options;
    private final String errorMessage;
    private final List<String> causeErrorMessage;
    private String lastWord;

    public HintResponse(String errorMessage, List<String> causeErrorMessage) {
        this.errorMessage = errorMessage;
        this.causeErrorMessage = new ArrayList<>(causeErrorMessage);
        options = Collections.emptyList();
    }

    public HintResponse(@Nullable List<Option> options, @Nullable String lastWord) {
        this.lastWord = lastWord;
        this.options = (options == null) ? Collections.<Option>emptyList() : options;
        errorMessage = null;
        this.causeErrorMessage = null;
    }

    public List<String> getOptions() {
        List<String> result = new ArrayList<>();
        for (Option option : options) {
            result.add(option.getValue());
        }
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }

    public List<Option> getOptionObjects() {
        Collections.sort(options, new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        return Collections.unmodifiableList(options);
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getCauseErrorMessages() {
        return Collections.unmodifiableList(causeErrorMessage);
    }

    @Nullable
    public String getLastWord() {
        return lastWord;
    }
}