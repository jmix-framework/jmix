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

package io.jmix.data.impl.jpql.suggestion;

import com.google.common.collect.Lists;
import io.jmix.core.suggestion.QuerySuggestionProvider;
import io.jmix.core.suggestion.QuerySuggestions;
import io.jmix.core.suggestion.QuerySuggestionsContext;
import io.jmix.data.impl.jpql.*;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import io.jmix.data.impl.jpql.model.NoJpqlEntityModel;
import io.jmix.data.impl.jpql.pointer.CollectionPointer;
import io.jmix.data.impl.jpql.pointer.EntityPointer;
import io.jmix.data.impl.jpql.pointer.NoPointer;
import io.jmix.data.impl.jpql.pointer.Pointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.data.impl.jpql.suggestion.JpqlSuggestionsUtil.*;

@Primary
@Component("data_JpqlSuggestionProvider")
public class JpqlSuggestionProvider implements QuerySuggestionProvider {
    protected DomainModelBuilder domainModelBuilder;

    @Autowired
    @Qualifier("withCaptions")
    public void setDomainModel(DomainModelBuilder domainModelBuilder) {
        this.domainModelBuilder = domainModelBuilder;
    }

    @Override
    public QuerySuggestions getSuggestions(QuerySuggestionsContext context) {
        if (Objects.equals("jpql", context.getLanguage())) {
            QueryWithPosition queryWithPosition = removeAliases(context.getQuery(), context.getPosition());

            String query = queryWithPosition.getQuery();
            int position = queryWithPosition.getPosition();

            DomainModel domainModel = domainModelBuilder.produce();

            String lastWord = getLastWord(query, position);
            if (isSuggestionByParameter(lastWord) && context.getParametersSupplier() != null) {
                Map<String, String> result = context.getParametersSupplier().get();
                if (result != null) {
                    List<QuerySuggestions.Option> options = result.entrySet().stream()
                            .map(entity -> new QuerySuggestions.Option(entity.getKey(), entity.getValue()))
                            .collect(Collectors.toList());
                    return new QuerySuggestions(options, lastWord);
                }
            } else if (isSuggestionByField(lastWord)) {
                return getSuggestionsByFieldName(domainModel, lastWord, query, position, getExpectedTypes(query, position));
            } else {
                return getSuggestionsByEntityName(domainModel, lastWord);
            }
        }
        return null;
    }

    protected QuerySuggestions getSuggestionsByFieldName(DomainModel domainModel,
                                                         String lastWord,
                                                         String query,
                                                         int caretPosition,
                                                         Set<InferredType> expectedTypes) {


        QueryTree queryTree;
        try {
            queryTree = new QueryTree(domainModel, query, false);
        } catch (JPA2RecognitionException e) {
            return new QuerySuggestions("Query error", Lists.newArrayList(e.getMessage()));
        }

        List<ErrorRec> errorRecs = queryTree.getInvalidIdVarNodes();

        if (queryTree.getQueryVariableContext() == null) {
            return new QuerySuggestions("Query variable context is null", formatErrorMessages(errorRecs));
        }

        QueryVariableContext variableContext = queryTree.getQueryVariableContext().getContextByCaretPosition(caretPosition);
        EntityPath entityPath = EntityPath.parseEntityPath(lastWord);
        Pointer pointer = entityPath.resolvePointer(domainModel, variableContext);

        if (pointer instanceof NoPointer) {
            return new QuerySuggestions(String.format("Cannot parse [%s]", lastWord), formatErrorMessages(errorRecs));
        }

        if (pointer instanceof CollectionPointer) {
            return new QuerySuggestions(String.format("Cannot get attribute of collection [%s]", lastWord),
                    formatErrorMessages(errorRecs));
        }

        if (!(pointer instanceof EntityPointer)) {
            return new QuerySuggestions("Query error", formatErrorMessages(errorRecs));
        }

        JpqlEntityModel entity = ((EntityPointer) pointer).getEntity();
        if (entity instanceof NoJpqlEntityModel) {
            return new QuerySuggestions(Collections.emptyList(), entityPath.lastEntityFieldPattern);
        }

        List<QuerySuggestions.Option> options =
                entity.findAttributesStartsWith(entityPath.lastEntityFieldPattern, expectedTypes).stream()
                        .map(attribute -> new QuerySuggestions.Option(attribute.getName(), attribute.getDisplayedName()))
                        .collect(Collectors.toList());

        return new QuerySuggestions(options, entityPath.lastEntityFieldPattern);
    }

    protected QuerySuggestions getSuggestionsByEntityName(DomainModel domainModel, String lastWord) {
        List<QuerySuggestions.Option> options = domainModel.findEntitiesStartingWith(lastWord).stream()
                .map(entity -> new QuerySuggestions.Option(entity.getName(), entity.getDisplayedName()))
                .collect(Collectors.toList());

        return new QuerySuggestions(options, lastWord);
    }
}
