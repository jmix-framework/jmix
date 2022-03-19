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

package io.jmix.security.model;

/**
 * Action for {@link io.jmix.security.role.annotation.EntityAttributePolicy}
 *
 * @see ResourcePolicy
 */
public enum EntityAttributePolicyAction {
    VIEW("view"),
    MODIFY("modify");

    private String id;

    EntityAttributePolicyAction(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
