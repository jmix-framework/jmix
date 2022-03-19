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

package io.jmix.security.constraint;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;

/**
 * Provides methods to check permissions of the current user.
 */
public interface SecureOperations {

    /**
     * Check if the current user has a permission to create the entity specified by the given meta-class.
     */
    boolean isEntityCreatePermitted(MetaClass metaClass, PolicyStore policyStore);

    /**
     * Check if the current user has a permission to read the entity specified by the given meta-class.
     */
    boolean isEntityReadPermitted(MetaClass metaClass, PolicyStore policyStore);

    /**
     * Check if the current user has a permission to update the entity specified by the given meta-class.
     */
    boolean isEntityUpdatePermitted(MetaClass metaClass, PolicyStore policyStore);

    /**
     * Check if the current user has a permission to delete the entity specified by the given meta-class.
     */
    boolean isEntityDeletePermitted(MetaClass metaClass, PolicyStore policyStore);

    /**
     * Check if the current user has a permission to read the entity attribute specified by the given meta-property path.
     */
    boolean isEntityAttrReadPermitted(MetaPropertyPath propertyPath, PolicyStore policyStore);

    /**
     * Check if the current user has a permission to update the entity attribute specified by the given meta-property path.
     */
    boolean isEntityAttrUpdatePermitted(MetaPropertyPath propertyPath, PolicyStore policyStore);

    /**
     * Check if the current user has the given specific permission.
     */
    boolean isSpecificPermitted(String resourceName, PolicyStore policyStore);

    /**
     * Check if the current user has a permission to the given GraphQL resource.
     */
    boolean isGraphQLPermitted(String resourceName, PolicyStore policyStore);
}
