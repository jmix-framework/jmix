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

package io.jmix.graphql.security.role;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.GraphQLPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(name = "GraphQL: minimal access", code = GraphQLMinimalRole.CODE, description = "Enables access to GraphQL API", scope = SecurityScope.API)
public interface GraphQLMinimalRole {
    String CODE = "graphql-minimal";

    @SpecificPolicy(resources = "graphql.enabled")
    @GraphQLPolicy(operations = "userInfo")
    void graphqlAccess();
}

