/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core;

import io.jmix.data.impl.jpql.DomainModel;
import io.jmix.data.impl.jpql.QueryParserAstBased;
import io.jmix.data.impl.jpql.model.EntityBuilder;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryParserAstKeywordsTest {
    @Test
    public void testSetKeywordEntity() {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select e from test_Entity e where e.set = :par"
        );
        Assertions.assertEquals("test_Entity", parser.getEntityName());
    }

    private DomainModel prepareDomainModel() {
        EntityBuilder builder = EntityBuilder.create();
        builder.startNewEntity("test_Entity");
        builder.addStringAttribute("set");
        builder.addStringAttribute("createdBy");
        JpqlEntityModel testEntity = builder.produce();

        return new DomainModel(testEntity);
    }
}
