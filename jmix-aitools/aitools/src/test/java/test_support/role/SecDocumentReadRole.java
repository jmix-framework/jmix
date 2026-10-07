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

package test_support.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import test_support.entity.Document;
import test_support.entity.SecretDocument;

/**
 * Reads documents fully, including the {@code @ExcludeFromAi} subclass: what keeps its records from the AI is the
 * annotation, not a missing permission.
 */
@ResourceRole(name = SecDocumentReadRole.CODE, code = SecDocumentReadRole.CODE)
public interface SecDocumentReadRole {

    String CODE = "test-document-read";

    @EntityPolicy(entityClass = Document.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = Document.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void document();

    @EntityPolicy(entityClass = SecretDocument.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = SecretDocument.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void secretDocument();
}
