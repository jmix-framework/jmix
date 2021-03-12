/*
 * Copyright 2021 Haulmont.
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

package io.jmix.uidata.propertyfilter;

import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.property.UiFilterProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Internal
public class UiDataPropertyFilterSupport extends PropertyFilterSupport {

    protected static final Pattern AGGREGATE_JPQL_FUNCTION_PATTERN =
            Pattern.compile("(COUNT|SUM|AVG|MIN|MAX)\\s*\\(.*\\)", Pattern.CASE_INSENSITIVE);

    protected QueryTransformerFactory queryTransformerFactory;

    public UiDataPropertyFilterSupport(Messages messages,
                                       MessageTools messageTools,
                                       MetadataTools metadataTools,
                                       DataManager dataManager,
                                       DatatypeRegistry datatypeRegistry,
                                       UiFilterProperties uiFilterProperties,
                                       AccessManager accessManager,
                                       QueryTransformerFactory queryTransformerFactory) {
        super(messages, messageTools, metadataTools, dataManager, datatypeRegistry, uiFilterProperties, accessManager);
        this.queryTransformerFactory = queryTransformerFactory;
    }

    @Override
    protected boolean isAggregateFunction(MetaPropertyPath propertyPath, String query) {
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
