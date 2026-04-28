/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.persistence.JpqlSortExpressionSupplier;
import io.jmix.data.persistence.SortExpressionContext;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

public class TestJpqlSortExpressionSupplier implements JpqlSortExpressionSupplier {

    protected Set<MetaPropertyPath> propertyPaths = new HashSet<>();
    protected String functionName;
    protected String nullsOrder;

    public TestJpqlSortExpressionSupplier(String functionName, String nullsOrder) {
        this.functionName = functionName;
        this.nullsOrder = nullsOrder;
    }

    @Override
    public String getDatatypeSortExpression(@NonNull SortExpressionContext context) {
        MetaPropertyPath metaPropertyPath = context.metaPropertyPath();
        if (propertyPaths.contains(metaPropertyPath)) {
            return String.format("%s({E}.%s) %s %s", functionName, metaPropertyPath,
                    context.isSortDirectionAsc() ? "asc" : "desc", nullsOrder);
        }

        return null;
    }

    public void addPropertyPath(MetaPropertyPath metaPropertyPath) {
        propertyPaths.add(metaPropertyPath);
    }

    public void resetPropertyPaths() {
        propertyPaths.clear();
    }
}
