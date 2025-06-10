/*
 * Copyright 2025 Haulmont.
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
 * Describes how generics should be resolved.
 */
public @interface GenericResolvingInfo {

    /**
     * Generic type parameter name.
     * For example, we have component with type {@code DataGrid<T>},
     * then {@code typeParameter} should be {@code T}.
     */
    String typeParameter();

    /**
     * Describes generic resolving strategy.
     *
     * @see ResolvingStrategy
     */
    ResolvingStrategy resolvingStrategy();

    /**
     * Describes how generic value can be resolved.
     * For example, we can resolve generic type from XML
     * (see {@link ParentTagByNameStrategy}, {@link ParentTagByDepthStrategy})
     * or define a concrete class FQN (see {@link ClassFqnStrategy})
     *
     * @see ClassFqnStrategy
     * @see ParentTagByNameStrategy
     * @see ParentTagByDepthStrategy
     */
    @interface ResolvingStrategy {

        /**
         * Describes resolving from XML by parent tag name.
         *
         * @see ParentTagByNameStrategy
         */
        ParentTagByNameStrategy parentTagByNameStrategy() default @ParentTagByNameStrategy(parentTagName = "");

        /**
         * Describes resolving from XML by parent tag depth.
         *
         * @see ParentTagByNameStrategy
         */
        ParentTagByDepthStrategy parentTagByDepthStrategy() default @ParentTagByDepthStrategy(parentTagDepth = -1);

        /**
         * Describes resolving from concrete class FQN.
         *
         * @see ClassFqnStrategy
         */
        ClassFqnStrategy classFqnStrategy() default @ClassFqnStrategy(classFqn = "");

        /**
         * Describes type from concrete class FQN with type parameters.
         */
        @interface ClassFqnStrategy {

            /**
             * Class FQN of type.
             */
            String classFqn();

            /**
             * Class FQNs of type parameters (if needed).
             */
            String[] typeParametersFqn() default {};
        }

        /**
         * Describes type from parent component that should be extracted from XML by tag name.
         */
        @interface ParentTagByNameStrategy {

            String parentTagName();

            /**
             * Class FQNs of type parameters.
             */
            String[] typeParametersFqn() default {};

            /**
             * Name of type parameter from resolved type.
             * <p>
             * If value is empty, then a resolved type will be used.
             * <p>
             * If value is not empty, then a type parameter from the resolved type will be used.
             */
            String takeFromTypeParameter() default "";
        }

        /**
         * Describes type from parent component that should be extracted from XML by tag depth.
         */
        @interface ParentTagByDepthStrategy {

            int parentTagDepth();

            /**
             * Class FQNs of type parameters.
             */
            String[] typeParametersFqn() default {};

            /**
             * Name of type parameter from resolved type.
             * <p>
             * If value is empty, then a resolved type will be used.
             * <p>
             * If value is not empty, then a type parameter from the resolved type will be used.
             */
            String takeFromTypeParameter() default "";
        }
    }
}
