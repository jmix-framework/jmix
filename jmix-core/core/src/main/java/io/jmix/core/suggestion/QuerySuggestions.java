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

package io.jmix.core.suggestion;

import io.jmix.core.common.util.Preconditions;

import org.springframework.lang.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class QuerySuggestions {
    private final List<Option> options;
    private final String errorMessage;
    private final List<String> causeErrorMessage;
    private String lastWord;

    public QuerySuggestions(String errorMessage, List<String> causeErrorMessage) {
        this.errorMessage = errorMessage;
        this.causeErrorMessage = new ArrayList<>(causeErrorMessage);
        this.options = Collections.emptyList();
    }

    public QuerySuggestions(@Nullable List<Option> options, @Nullable String lastWord) {
        this.lastWord = lastWord;
        this.options = options == null ? Collections.emptyList() : options;
        this.errorMessage = null;
        this.causeErrorMessage = Collections.emptyList();
        this.options.sort(Comparator.comparing(Option::getValue));
    }

    public List<String> getOptionStrings() {
        return options.stream()
                .map(Option::getValue)
                .collect(Collectors.toList());
    }

    public List<Option> getOptions() {
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

    public static class Option {
        private final String value;
        private final String description;

        public Option(String value, @Nullable String description) {
            Preconditions.checkNotNullArgument(value, "No value passed");

            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            return Objects.equals(value, option.value) && Objects.equals(description, option.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, description);
        }
    }
}