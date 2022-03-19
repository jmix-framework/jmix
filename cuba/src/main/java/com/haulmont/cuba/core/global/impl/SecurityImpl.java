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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.security.entity.EntityAttrAccess;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.EntityOp;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.securityui.constraint.UiPolicyStore;
import io.jmix.securityui.constraint.UiSecureOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(Security.NAME)
public class SecurityImpl implements Security {
    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected UiPolicyStore uiPolicyStore;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected UiSecureOperations uiSecureOperations;

    @Override
    public boolean isScreenPermitted(String windowAlias) {
        return uiSecureOperations.isScreenPermitted(windowAlias, uiPolicyStore);
    }

    @Override
    public boolean isEntityOpPermitted(MetaClass metaClass, EntityOp entityOp) {
        switch (entityOp) {
            case CREATE:
                return secureOperations.isEntityCreatePermitted(metaClass, policyStore);
            case READ:
                return secureOperations.isEntityReadPermitted(metaClass, policyStore);
            case UPDATE:
                return secureOperations.isEntityUpdatePermitted(metaClass, policyStore);
            case DELETE:
                return secureOperations.isEntityDeletePermitted(metaClass, policyStore);
        }
        return false;
    }

    @Override
    public boolean isEntityOpPermitted(Class<?> entityClass, EntityOp entityOp) {
        return isEntityOpPermitted(metadata.getClass(entityClass), entityOp);
    }

    @Override
    public boolean isEntityAttrPermitted(MetaClass metaClass, String property, EntityAttrAccess access) {
        switch (access) {
            case MODIFY:
                return secureOperations.isEntityAttrUpdatePermitted(metaClass.getPropertyPath(property), policyStore);
            case VIEW:
                return secureOperations.isEntityAttrReadPermitted(metaClass.getPropertyPath(property), policyStore);
        }
        return false;
    }

    @Override
    public boolean isEntityAttrPermitted(Class<?> entityClass, String property, EntityAttrAccess access) {
        return isEntityAttrPermitted(metadata.getClass(entityClass), property, access);
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaClass metaClass, String propertyPath) {
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(metaClass, propertyPath);
        return metaPropertyPath != null && isEntityAttrUpdatePermitted(metaPropertyPath);
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaPropertyPath metaPropertyPath) {
        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);

        if (metadataTools.isJpaEmbeddable(propertyMetaClass)) {
            return isEntityOpPermitted(propertyMetaClass, EntityOp.UPDATE)
                    && isEntityAttrPermitted(propertyMetaClass, metaPropertyPath, EntityAttrAccess.MODIFY);
        }

        return (isEntityOpPermitted(propertyMetaClass, EntityOp.CREATE)
                || isEntityOpPermitted(propertyMetaClass, EntityOp.UPDATE))
                && isEntityAttrPermitted(propertyMetaClass, metaPropertyPath, EntityAttrAccess.MODIFY);
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaPropertyPath metaPropertyPath) {
        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        return isEntityOpPermitted(propertyMetaClass, EntityOp.READ)
                && isEntityAttrPermitted(propertyMetaClass, metaPropertyPath, EntityAttrAccess.VIEW);
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaClass metaClass, String propertyPath) {
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(metaClass, propertyPath);
        return metaPropertyPath != null && isEntityAttrReadPermitted(metaPropertyPath);
    }

    @Override
    public boolean isSpecificPermitted(String name) {
        return secureOperations.isSpecificPermitted(name, policyStore);
    }

    @Override
    public void checkSpecificPermission(String name) {
        if (!isSpecificPermitted(name))
            throw new AccessDeniedException("specific", name);
    }

    protected boolean isEntityAttrPermitted(MetaClass metaClass, MetaPropertyPath propertyPath, EntityAttrAccess access) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass != null) {
            metaClass = originalMetaClass;
        }
        return isEntityAttrPermitted(metaClass, propertyPath.getMetaProperty().getName(), access);
    }
}
