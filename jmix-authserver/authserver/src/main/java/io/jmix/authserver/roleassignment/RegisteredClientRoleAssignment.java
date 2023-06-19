/*
 * Copyright 2022 Haulmont.
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

package io.jmix.authserver.roleassignment;

import java.util.List;

/**
 * A record stores a list resource and rol-level roles assigned to OAuth2 client. This is relevant for Client
 * Credentials authorization grant type.
 *
 * @param registrationId
 * @param clientId
 * @param resourceRoles
 * @param rowLevelRoles
 */
public record RegisteredClientRoleAssignment(String registrationId,
                                             String clientId,
                                             List<String> resourceRoles,
                                             List<String> rowLevelRoles) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String registrationId;
        private String clientId;
        private List<String> resourceRoles;
        private List<String> rowLevelRoles;

        public Builder registrationId(String registrationId) {
            this.registrationId = registrationId;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder resourceRoles(List<String> resourceRoles) {
            this.resourceRoles = resourceRoles;
            return this;
        }

        public Builder rowLevelRoles(List<String> rowLevelRoles) {
            this.rowLevelRoles = rowLevelRoles;
            return this;
        }

        public RegisteredClientRoleAssignment build() {
            return new RegisteredClientRoleAssignment(registrationId, clientId, resourceRoles, rowLevelRoles);
        }
    }
}