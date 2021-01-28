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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
public class ResourcePolicy implements Serializable {
    private static final long serialVersionUID = -6635135023832164413L;

    public static final String DEFAULT_EFFECT = ResourcePolicyEffect.ALLOW;
    public static final String DEFAULT_ACTION = "access";
    public static final String DEFAULT_POLICY_GROUP = "";

    private final String type;
    private final String resource;
    private String action = DEFAULT_ACTION;
    private String effect = DEFAULT_EFFECT;
    private String policyGroup = DEFAULT_POLICY_GROUP;

    private Map<String, String> customProperties;

    private ResourcePolicy(String type, String resource) {
        this.type = type;
        this.resource = resource;
    }

    public ResourcePolicy(Builder builder) {
        this.type = builder.type;
        this.resource = builder.resource;
        this.action = builder.action;
        this.effect = builder.effect;
        this.policyGroup = builder.policyGroup;
        this.customProperties = builder.customProperties;
    }

    public static Builder builder(String type, String resource) {
        return new Builder(type, resource);
    }

    /**
     * Returns policy type. Standard policies type are:
     *
     * <ul>
     *     <li>menu</li>
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

    /**
     * Returns policy group. For annotated roles policy group is typically a name of the method in the role interface
     *
     * @return policy group
     */
    public String getPolicyGroup() {
        return policyGroup;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public static class Builder {

        private final String type;
        private final String resource;
        private String action = DEFAULT_ACTION;
        private String effect = DEFAULT_EFFECT;
        private String policyGroup = DEFAULT_POLICY_GROUP;
        private Map<String, String> customProperties = new HashMap<>();


        public Builder(String type, String resource) {
            this.type = type;
            this.resource = resource;
        }

        public Builder withAction(String action) {
            this.action = action;
            return this;
        }

        public Builder withEffect(String effect) {
            this.effect = effect;
            return this;
        }

        public Builder withPolicyGroup(String policyGroup) {
            this.policyGroup = policyGroup;
            return this;
        }

        public Builder withCustomProperties(Map<String, String> customProperties) {
            this.customProperties = customProperties;
            return this;
        }

        public ResourcePolicy build() {
            return new ResourcePolicy(this);
        }
    }
}
