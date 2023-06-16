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

package io.jmix.flowuidata.genericfilter;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.component.genericfilter.FilterMetadataTools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Internal
public class UiDataFilterMetadataTools extends FilterMetadataTools {

    protected static final Pattern AGGREGATE_JPQL_FUNCTION_PATTERN =
            Pattern.compile("(COUNT|SUM|AVG|MIN|MAX)\\s*\\(.*\\)", Pattern.CASE_INSENSITIVE);

    protected QueryTransformerFactory queryTransformerFactory;

    public UiDataFilterMetadataTools(MetadataTools metadataTools,
                                     UiComponentProperties uiComponentProperties,
                                     AccessManager accessManager,
                                     QueryTransformerFactory queryTransformerFactory,
                                     Metadata metadata) {
        super(metadataTools, uiComponentProperties, accessManager, metadata);
        this.queryTransformerFactory = queryTransformerFactory;
    }

    @Override
    protected boolean isAggregateFunction(MetaPropertyPath propertyPath, String query) {
        if (Strings.isNullOrEmpty(query)) {
            return false;
        }

        MetaClass filterMetaClass = propertyPath.getMetaClass();
        int index = new ArrayList<>(filterMetaClass.getProperties()).indexOf(propertyPath.getMetaProperty());

        QueryParser queryParser = queryTransformerFactory.parser(query);
        List<String> selectedExpressions = queryParser.getSelectedExpressionsList();

        if (index >= 0 && index < selectedExpressions.size()) {
            String selectedExpression = selectedExpressions.get(index);
            return AGGREGATE_JPQL_FUNCTION_PATTERN.matcher(selectedExpression).matches();
        }

        return super.isAggregateFunction(propertyPath, query);
    }
}
