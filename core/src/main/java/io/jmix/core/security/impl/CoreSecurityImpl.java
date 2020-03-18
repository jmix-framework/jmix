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

package io.jmix.core.security.impl;

import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.*;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component(Security.NAME)
@Conditional(OnCoreSecurityImplementation.class)
public class CoreSecurityImpl implements Security {

    @Override
    public boolean isScreenPermitted(String windowAlias) {
        return true;
    }

    @Override
    public boolean isEntityOpPermitted(MetaClass metaClass, EntityOp entityOp) {
        return true;
    }

    @Override
    public boolean isEntityOpPermitted(Class<?> entityClass, EntityOp entityOp) {
        return true;
    }

    @Override
    public boolean isEntityAttrPermitted(MetaClass metaClass, String property, EntityAttrAccess access) {
        return true;
    }

    @Override
    public boolean isEntityAttrPermitted(Class<?> entityClass, String property, EntityAttrAccess access) {
        return true;
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaClass metaClass, String propertyPath) {
        return true;
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaPropertyPath metaPropertyPath) {
        return true;
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaPropertyPath metaPropertyPath) {
        return true;
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaClass metaClass, String propertyPath) {
        return true;
    }

    @Override
    public boolean isSpecificPermitted(String name) {
        return true;
    }

    @Override
    public void checkSpecificPermission(String name) {

    }

    @Override
    public boolean isPermitted(Entity entity, ConstraintOperationType operationType) {
        return true;
    }

    @Override
    public boolean isPermitted(Entity entity, String customCode) {
        return true;
    }

    @Override
    public boolean hasConstraints() {
        return false;
    }

    @Override
    public boolean hasConstraints(MetaClass metaClass) {
        return false;
    }

    @Override
    public boolean hasInMemoryConstraints(MetaClass metaClass, ConstraintOperationType... operationTypes) {
        return false;
    }

    @Override
    public Object evaluateConstraintScript(Entity entity, String groovyScript) {
        return null;
    }
}
