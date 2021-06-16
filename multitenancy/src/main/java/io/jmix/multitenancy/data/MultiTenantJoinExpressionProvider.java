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

package io.jmix.multitenancy.data;

import io.jmix.core.annotation.TenantId;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.impl.mapping.AbstractJoinExpressionProvider;
import io.jmix.multitenancy.core.TenantEntityOperation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.ManyToOneMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.Arrays;

@Component("mten_MultiTenantJoinExpressionProvider")
public class MultiTenantJoinExpressionProvider extends AbstractJoinExpressionProvider {

    private final TenantEntityOperation tenantEntityOperation;

    public MultiTenantJoinExpressionProvider(TenantEntityOperation tenantEntityOperation) {
        this.tenantEntityOperation = tenantEntityOperation;
    }

    @Override
    protected Expression processOneToManyMapping(OneToManyMapping mapping) {
        return null;
    }

    @Override
    protected Expression processOneToOneMapping(OneToOneMapping mapping) {
        return createToOneJoinExpression(mapping);
    }

    @Override
    protected Expression processManyToOneMapping(ManyToOneMapping mapping) {
        return createToOneJoinExpression(mapping);
    }

    @Override
    protected Expression processManyToManyMapping(ManyToManyMapping mapping) {
        return null;
    }

    @Nullable
    private Expression createToOneJoinExpression(OneToOneMapping oneToOneMapping) {
        ClassDescriptor descriptor = oneToOneMapping.getDescriptor();
        Class<?> referenceClass = oneToOneMapping.getReferenceClass();
        if (isMultiTenant(referenceClass) && isMultiTenant(descriptor.getJavaClass())) {
            MetaProperty tenantIdField = tenantEntityOperation.findTenantProperty(referenceClass);
            Field parentTenantId = findTenantField(descriptor.getJavaClass());
            if (tenantIdField != null && parentTenantId != null) {
                String columnName = tenantIdField.getName();
                ExpressionBuilder builder = new ExpressionBuilder();
                Expression tenantColumnExpression = builder.get(columnName);
                return tenantColumnExpression.equal(
                        builder.getParameter(
                                parentTenantId.getAnnotation(Column.class).name()));
            }
        }
        return null;
    }

    private Field findTenantField(Class<?> entityClass) {
        return Arrays.stream(FieldUtils.getAllFields(entityClass))
                .filter(f -> f.isAnnotationPresent(TenantId.class))
                .findFirst().orElse(null);
    }

    private boolean isMultiTenant(Class<?> referenceClass) {
        return tenantEntityOperation.findTenantProperty(referenceClass) != null;
    }

}
