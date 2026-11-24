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

package io.jmix.security.role.assignment;

import org.jspecify.annotations.NullMarked;

/**
 * Sources of stored role assignments: who wrote a role assignment and therefore who may remove it.
 * <ul>
 *     <li>{@link #MANUAL} - assigned by hand on the role assignment screens. The synchronization of an identity
 *     provider never removes such an assignment. Application code that assigns roles which must survive the
 *     synchronization sets this value itself.</li>
 *     <li>{@link #OIDC}, {@link #SAML}, {@link #LDAP} - written by the role assignment synchronization of the
 *     corresponding add-on, which removes the assignment when the identity provider no longer asserts the role.</li>
 *     <li>{@code null} - written before role assignments had a source, or created without one, for example by
 *     application code or an import. The synchronization treats such an assignment as its own.</li>
 * </ul>
 */
@NullMarked
public final class RoleAssignmentSource {

    public static final String MANUAL = "MANUAL";
    public static final String OIDC = "OIDC";
    public static final String SAML = "SAML";
    public static final String LDAP = "LDAP";

    private RoleAssignmentSource() {
    }
}
