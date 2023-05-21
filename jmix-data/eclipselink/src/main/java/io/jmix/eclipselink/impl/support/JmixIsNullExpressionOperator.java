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

package io.jmix.eclipselink.impl.support;

import io.jmix.core.MetadataTools;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;
import org.eclipse.persistence.internal.expressions.QueryKeyExpression;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.JmixUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class JmixIsNullExpressionOperator extends ExpressionOperator {

    private MetadataTools metadataTools;

    public JmixIsNullExpressionOperator(MetadataTools metadataTools) {
        setType(ComparisonOperator);
        setSelector(IsNull);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.add("(");
        v.add(" IS NULL)");
        printsAs(v);
        bePrefix();
        printsJavaAs(".isNull()");
        setNodeClass(ClassConstants.FunctionExpression_Class);
        this.metadataTools = metadataTools;
    }

    @Override
    public void printCollection(List<Expression> items, ExpressionSQLPrinter printer) {
        if (items.size() == 1 && items.get(0) instanceof QueryKeyExpression && !JmixUtil.isSoftDeletion()) {
            QueryKeyExpression expression = (QueryKeyExpression) items.get(0);
            //noinspection unchecked
            Class<?> clazz = expression.getContainingDescriptor().getJavaClass();

            String deletedDateFieldName = metadataTools.findDeletedDateProperty(clazz);
            if (Objects.equals(deletedDateFieldName, expression.getName())) {
                try {
                    printer.getWriter().write("(0=0)");
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.printCollection(items, printer);
    }
}