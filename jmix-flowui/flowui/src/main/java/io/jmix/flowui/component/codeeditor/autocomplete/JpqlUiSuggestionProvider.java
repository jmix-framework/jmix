/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.codeeditor.autocomplete;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.suggestion.QuerySuggestionProvider;
import io.jmix.core.suggestion.QuerySuggestions;
import io.jmix.core.suggestion.QuerySuggestionsContext;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.component.codeeditor.autocomplete.Suggester;
import io.jmix.flowui.kit.component.codeeditor.autocomplete.Suggestion;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides auto-completion suggestions for JPQL queries in the user interface.
 * Used to provide auto-completion suggestions using {@link Suggester} in the {@link CodeEditor} component.
 */
@Component("flowui_JpqlUiSuggestionProvider")
public class JpqlUiSuggestionProvider {

    protected static final String INITIAL_QUERY = "select %s from %s %s ";
    protected static final String JOIN = "join ";
    protected static final String WHERE = " where ";
    protected static final String ENTITY_PLACEHOLDER = "{E}";

    protected static final String QUERY_LANGUAGE = "jpql";
    protected static final String CURRENT_USER_PREFIX = "current_user_";

    protected CurrentAuthentication currentAuthentication;
    protected Metadata metadata;
    protected QuerySuggestionProvider querySuggestionProvider;

    public JpqlUiSuggestionProvider(CurrentAuthentication currentAuthentication, Metadata metadata,
                                    QuerySuggestionProvider querySuggestionProvider) {
        this.currentAuthentication = currentAuthentication;
        this.metadata = metadata;
        this.querySuggestionProvider = querySuggestionProvider;
    }

    /**
     * @param context      the current state of the client-side of the component
     * @param join         {@code JOIN} clause used in the query
     * @param where        {@code WHERE} clause used in the query
     * @param entityName   the name of the entity for which the query is created
     * @param isJoinClause is suggestions requested for {@code JOIN} clause
     * @return list of suggestions for autocompletion of JPQL query based on the current editor state and the context
     * of the call
     */
    public List<Suggestion> getSuggestions(Suggester.SuggestionContext context,
                                           @Nullable String join, @Nullable String where,
                                           String entityName, boolean isJoinClause) {
        // CAUTION: the magic entity name! The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        String initialQuery = INITIAL_QUERY.formatted(entityAlias, entityName, entityAlias);

        StringBuilder queryBuilder = new StringBuilder(initialQuery);
        int cursorPosition = context.getCursorPosition();

        if (Strings.isNotEmpty(join)) {
            if (isJoinClause) {
                queryPosition = queryBuilder.length() + cursorPosition - 1;
            }

            if (!StringUtils.containsIgnoreCase(join, JOIN.trim())
                    && !StringUtils.contains(join, ',')) {
                queryBuilder.append(JOIN).append(join);
                queryPosition += JOIN.length();
            } else {
                queryBuilder.append(join);
            }
        }

        if (Strings.isNotEmpty(where)) {
            if (!isJoinClause) {
                queryPosition = queryBuilder.length() + WHERE.length() + cursorPosition - 1;
            }

            queryBuilder.append(WHERE)
                    .append(where);
        }

        String query = queryBuilder.toString();
        query = query.replace(ENTITY_PLACEHOLDER, entityAlias);

        List<Suggestion> suggestions = getSuggestions(context, query, queryPosition);
        addCurrentUserAttributesSuggestions(context, suggestions);

        return suggestions;
    }

    /**
     * @param context       the current state of the client-side of the component
     * @param query         the query for which the suggestions will be provided
     * @param queryPosition the position in the query for which suggestions will be provided
     * @return list of suggestions for autocompletion of JPQL query
     */
    public List<Suggestion> getSuggestions(Suggester.SuggestionContext context, String query, int queryPosition) {
        return getSuggestions(context, query, queryPosition, null);
    }

    /**
     * @param context            the current state of the client-side of the component
     * @param query              the query for which the suggestions will be provided
     * @param queryPosition      the position in the query for which suggestions will be provided
     * @param parametersSupplier supplier of parameters for the query
     * @return list of suggestions for autocompletion of JPQL query
     */
    public List<Suggestion> getSuggestions(Suggester.SuggestionContext context,
                                           String query, int queryPosition,
                                           @Nullable Supplier<Map<String, String>> parametersSupplier) {
        QuerySuggestionsContext querySuggestionsContext = new QuerySuggestionsContext();

        querySuggestionsContext.setQuery(query);
        querySuggestionsContext.setPosition(queryPosition);
        querySuggestionsContext.setLanguage(QUERY_LANGUAGE);
        querySuggestionsContext.setParametersSupplies(parametersSupplier);

        QuerySuggestions suggestions = querySuggestionProvider.getSuggestions(querySuggestionsContext);

        return suggestions == null
                ? Collections.emptyList()
                : suggestions.getOptions().stream()
                .map(this::queryOptionToSuggestion)
                .collect(Collectors.toList());
    }

    protected void addCurrentUserAttributesSuggestions(Suggester.SuggestionContext context,
                                                       List<Suggestion> suggestions) {
        String text = context.getText();
        int cursorPosition = context.getCursorPosition();

        if (cursorPosition == 0 || Strings.isEmpty(text)) {
            return;
        }

        int columnIndex = text.substring(0, cursorPosition).lastIndexOf(':');
        if (columnIndex < 0) {
            return;
        }

        if (columnIndex + context.getPrefix().length() + 1 == cursorPosition) {
            suggestions.addAll(getCurrentUserAttributes());
        }
    }

    protected List<Suggestion> getCurrentUserAttributes() {
        UserDetails user = currentAuthentication.getUser();
        MetaClass userMetaClass = metadata.findClass(user.getClass());

        if (userMetaClass == null) {
            return Collections.emptyList();
        }

        return userMetaClass.getProperties().stream()
                .map(this::propertyToSuggestionMapper)
                .toList();
    }

    protected Suggestion propertyToSuggestionMapper(MetaProperty metaProperty) {
        String suggestion = CURRENT_USER_PREFIX + metaProperty.getName();
        return new Suggestion(suggestion, suggestion);
    }

    protected Suggestion queryOptionToSuggestion(QuerySuggestions.Option option) {
        return new Suggestion(option.getValue(), option.getValue(), option.getDescription());
    }
}
