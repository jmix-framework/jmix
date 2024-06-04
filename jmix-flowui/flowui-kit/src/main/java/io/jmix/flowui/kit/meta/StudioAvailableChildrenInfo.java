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

package io.jmix.flowui.kit.meta;

/**
 * Description of various child components.
 * <p></p>
 * If {@link StudioAvailableChildrenInfo#availableTags()} and {@link StudioAvailableChildrenInfo#availableClasses()}
 * are empty, then the Studio will use an internal standard mechanism to identify available children.
 */
public @interface StudioAvailableChildrenInfo {

    /**
     * Placeholder to describe any xml tag.
     */
    String ANY_TAG = "${ANY_TAG}";

    /**
     * Placeholder to describe any component class.
     */
    String ANY_CLASS = "${ANY_CLASS}";

    /**
     * Qualified name of {@link com.vaadin.flow.component.Component}.
     */
    String FLOW_COMPONENT_FQN = "com.vaadin.flow.component.Component";

    /**
     * Array with the components tags qualified names (with namespace if exist) that
     * can be contained inside the current component.
     * <p></p>
     * Empty array is equivalent to the fact that checking for a tag will return true.
     */
    TagInfo[] availableTags() default {};

    /**
     * Array with the components qualified names that
     * can be contained inside the current component.
     * <p></p>
     * Empty array is equivalent to the fact that checking for a class will return true.
     */
    ClassInfo[] availableClasses() default {};

    /**
     * Policy describing the final result of whether a component can be a child or not.
     * See more info in {@link ConditionPolicy}.
     *
     * @see ConditionPolicy
     */
    ConditionPolicy conditionPolicy() default ConditionPolicy.ALL;

    /**
     * Description of how many times a component tag can occur inside the current component.
     */
    @interface TagInfo {
        String qualifiedName();

        long maxCount();
    }

    /**
     * Description of how many times a component class can occur inside the current component.
     */
    @interface ClassInfo {
        String qualifiedName();

        long maxCount();
    }

    /**
     * if {@link ConditionPolicy#AT_LEAST_ONE} then the check must pass according to at least one criterion
     * ({@link StudioAvailableChildrenInfo#availableTags()} <b>OR</b> {@link StudioAvailableChildrenInfo#availableClasses()}).
     * <br>
     * Otherwise, if {@link  ConditionPolicy#ALL} then the check must pass according to all criteria
     * ({@link StudioAvailableChildrenInfo#availableTags()} <b>AND</b> {@link StudioAvailableChildrenInfo#availableClasses()})
     */
    enum ConditionPolicy {
        AT_LEAST_ONE, ALL
    }
}
