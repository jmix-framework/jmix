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

public interface SecureOperations {

    boolean isEntityCreatePermitted(MetaClass metaClass, PolicyStore policyStore);

    boolean isEntityReadPermitted(MetaClass metaClass, PolicyStore policyStore);

    boolean isEntityUpdatePermitted(MetaClass metaClass, PolicyStore policyStore);

    boolean isEntityDeletePermitted(MetaClass metaClass, PolicyStore policyStore);

    boolean isEntityAttrReadPermitted(MetaPropertyPath propertyPath, PolicyStore policyStore);

    boolean isEntityAttrUpdatePermitted(MetaPropertyPath propertyPath, PolicyStore policyStore);

    boolean isSpecificPermitted(String resourceName, PolicyStore policyStore);

    boolean isGraphQLPermitted(String resourceName, PolicyStore policyStore);
}
