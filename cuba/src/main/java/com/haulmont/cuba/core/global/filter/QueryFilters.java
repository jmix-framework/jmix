/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core.global.filter;

import org.dom4j.Element;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cuba_QueryFilters")
public class QueryFilters {

    @Autowired
    protected ObjectProvider<QueryFilter> objectProvider;

    public QueryFilter createQueryFilter(Condition condition) {
        return objectProvider.getObject(condition);
    }

    public QueryFilter createQueryFilter(Element element) {
        return objectProvider.getObject(element);
    }

    public QueryFilter merge(QueryFilter src1, QueryFilter src2) {
        if (src1 == null || src2 == null)
            throw new IllegalArgumentException("Source query filter is null");

        Condition root = new LogicalCondition("root", LogicalOp.AND);
        root.getConditions().add(src1.getRoot());
        root.getConditions().add(src2.getRoot());

        return createQueryFilter(root);
    }
}
