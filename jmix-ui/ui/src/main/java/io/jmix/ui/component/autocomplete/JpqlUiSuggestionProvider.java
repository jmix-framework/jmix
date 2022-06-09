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

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.suggestion.QuerySuggestionProvider;
import io.jmix.core.suggestion.QuerySuggestions;
import io.jmix.core.suggestion.QuerySuggestionsContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component("ui_JpqlUiSuggestionProvider")
public class JpqlUiSuggestionProvider {

    protected static final String JOIN = "join ";
    protected static final String WHERE = " where ";
    protected static final String PLACEHOLDER = "{E}";
    protected static final String CURRENT_USER_PREFIX = "current_user_";

    @Autowired
    protected QuerySuggestionProvider querySuggestionProvider;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Metadata metadata;

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

    public List<Suggestion> getSuggestions(AutoCompleteSupport sender, @Nullable String joinStr, @Nullable String whereStr,
                                           String entityName, boolean inJoinClause) {

        int cursorPosition = sender.getCursorPosition();

        // CAUTION: the magic entity name! The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        String queryStart = "select " + entityAlias + " from " + entityName + " "
                + entityAlias + " ";

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (inJoinClause) {
                queryPosition = queryBuilder.length() + cursorPosition - 1;
            }
            if (!StringUtils.containsIgnoreCase(joinStr, JOIN.trim())
                    && !StringUtils.contains(joinStr, ",")) {
                queryBuilder.append(JOIN).append(joinStr);
                queryPosition += JOIN.length();
            } else {
                queryBuilder.append(joinStr);
            }
        }
        if (StringUtils.isNotEmpty(whereStr)) {
            if (!inJoinClause) {
                queryPosition = queryBuilder.length() + WHERE.length() + cursorPosition - 1;
            }
            queryBuilder.append(WHERE).append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace(PLACEHOLDER, entityAlias);

        List<Suggestion> suggestions = getSuggestions(query, queryPosition, sender);
        addSpecificSuggestions(sender, cursorPosition, suggestions);
        return suggestions;
    }

    protected void addSpecificSuggestions(AutoCompleteSupport sender, int cursorPosition, List<Suggestion> suggestions) {
        String text = (String) sender.getValue();
        if (cursorPosition <= 0 || text == null)
            return;
        int colonIdx = text.substring(0, cursorPosition).lastIndexOf(":");
        if (colonIdx < 0)
            return;

        List<String> strings = new ArrayList<>();
        addCurrentUserAttributes(strings);
        Collections.sort(strings);

        String entered = text.substring(colonIdx + 1, cursorPosition);
        for (String string : strings) {
            if (string.startsWith(entered)) {
                suggestions.add(new Suggestion(sender, string, string.substring(entered.length()), "", cursorPosition, cursorPosition));
            }
        }
    }

    protected void addCurrentUserAttributes(List<String> strings) {
        UserDetails user = currentAuthentication.getUser();
        MetaClass userMetaClass = metadata.findClass(user.getClass());
        if (userMetaClass != null) {
            List<String> names = userMetaClass.getProperties().stream()
                    .map(metaProperty -> CURRENT_USER_PREFIX + metaProperty.getName())
                    .collect(Collectors.toList());
            strings.addAll(names);
        }
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
