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

package io.jmix.data.impl.jpql.generator;

import com.google.common.base.Strings;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Modifies JPQL query according to the tree of conditions.
 * See {@link #processQuery(String, ConditionGenerationContext)} method.
 */
@Component("data_ConditionJpqlGenerator")
public class ConditionJpqlGenerator {

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected ConditionGeneratorResolver resolver;

    /**
     * Returns a JPQL query modified according to the given tree of conditions.
     *
     * @param query   JPQL query
     * @param context condition generation context
     * @return a JPQL query modified according to the given tree of conditions
     */
    public String processQuery(String query, ConditionGenerationContext context) {
        if (context.getCondition() == null) {
            return query;
        }
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        QueryParser parser = queryTransformerFactory.parser(query);
        context.setEntityAlias(parser.getEntityAlias());

        if (context.getValueProperties() != null) {
            context.setSelectedExpressions(parser.getSelectedExpressionsList());
        }

        ConditionGenerator generator = resolver.getConditionGenerator(context);
        ConditionJpqlClause clause = generator.generateJoinAndWhere(context);
        String join = clause.getJoin();
        String where = clause.getWhere();

        if (!Strings.isNullOrEmpty(join)) {
            transformer.addJoinAndWhere(join, where);
        } else {
            transformer.addWhere(where);
        }
        return transformer.getResult();
    }
}
