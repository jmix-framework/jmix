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

package io.jmix.ui.component.autocomplete;

import io.jmix.core.suggestion.QuerySuggestions;
import io.jmix.core.suggestion.QuerySuggestionsContext;
import io.jmix.core.suggestion.QuerySuggestionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component("ui_JpqlUiSuggestionProvider")
public class JpqlUiSuggestionProvider {
    @Autowired
    protected QuerySuggestionProvider querySuggestionProvider;

    public List<Suggestion> getSuggestions(String query,
                                           int queryPosition,
                                           AutoCompleteSupport sender,
                                           @Nullable Supplier<Map<String, String>> parametersSupplier) {

        QuerySuggestionsContext context = new QuerySuggestionsContext();
        context.setQuery(query);
        context.setPosition(queryPosition);
        context.setLanguage("jpql");
        context.setParametersSupplies(parametersSupplier);

        QuerySuggestions querySuggestions = querySuggestionProvider.getSuggestions(context);

        if (querySuggestions != null) {
            return querySuggestions.getOptions().stream()
                    .map(option -> createSuggestion(option, querySuggestions, sender))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public List<Suggestion> getSuggestions(String query,
                                           int queryPosition,
                                           AutoCompleteSupport sender) {

        return getSuggestions(query, queryPosition, sender, null);
    }

    protected Suggestion createSuggestion(QuerySuggestions.Option option,
                                          QuerySuggestions querySuggestions,
                                          AutoCompleteSupport sender) {

        String displayedValue = option.getDescription() == null ?
                option.getValue() : String.format("%s (%s)", option.getValue(), option.getDescription());

        int prefixLength = querySuggestions.getLastWord() != null ? querySuggestions.getLastWord().length() : 0;
        int startPosition = sender.getCursorPosition() - prefixLength;
        String valueSuffix = option.getValue().substring(prefixLength);

        return new Suggestion(sender, displayedValue, option.getValue(), valueSuffix, startPosition, sender.getCursorPosition());
    }
}
