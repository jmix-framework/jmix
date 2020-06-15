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
 * Describes a permission to a resource.
 * <p>
 * A resource may be a screen, entity CRUD operation, entity attribute operation, etc.
 * <p>
 * For example, a policy that allows UPDATE operation on the sample_Order entity will look as follows:
 *
 * <ul>
 *     <li>type = "entity"</li>
 *     <li>resource = "sample_Order"</li>
 *     <li>action = "update"</li>
 *     <li>effect = "allow"</li>
 * </ul>
 */
public class ResourcePolicy {

    public static final String DEFAULT_EFFECT = ResourcePolicyEffect.ALLOW;
    public static final String DEFAULT_ACTION = "access";

    private String type;
    private String resource;
    private String action;
    private String effect;

    public ResourcePolicy(String type, String resource) {
        this(type, resource, DEFAULT_ACTION, DEFAULT_EFFECT);
    }

    public ResourcePolicy(String type, String resource, String action) {
        this(type, resource, action, DEFAULT_EFFECT);
    }

    public ResourcePolicy(String type, String resource, String action, String effect) {
        this.type = type;
        this.resource = resource;
        this.action = action;
        this.effect = effect;
    }

    /**
     * Returns policy type. Standard policies type are:
     *
     * <ul>
     *     <li>screen</li>
     *     <li>entity</li>
     *     <li>entityAttribute</li>
     *     <li>specific</li>
     * </ul>
     * <p>
     * They are listed in the {@link ResourcePolicyType}
     *
     * @return policy type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns a resource description. For screen policies it a screen id, for entity - entity name, for entity
     * attributes it is a string that contains an entity name and an attribute name separated by a dot.
     *
     * @return resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Returns policy action. The action is an operation that policy allows or denies.
     *
     * @return policy action
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns policy effect. Usually it is "allow" or "deny". The constant values are in the {@link
     * ResourcePolicyEffect} class
     *
     * @return policy effect
     */
    public String getEffect() {
        return effect;
    }
}
