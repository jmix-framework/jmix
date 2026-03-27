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

package io.jmix.flowui.kit.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a reusable group of {@link StudioProperty} definitions.
 * <p>
 * A group should be declared on a class or interface and referenced from
 * {@code propertyGroups()} in {@link StudioComponent}, {@link StudioElement},
 * {@link StudioFacet}, {@link StudioDataComponent}, {@link StudioAction},
 * {@link StudioActionsGroup} or {@link StudioElementsGroup}.
 *
 * @see StudioPropertyGroups
 */
@Documented
@Target(ElementType.TYPE)
public @interface StudioPropertyGroup {

    /**
     * Properties included in the group.
     */
    StudioProperty[] properties();
}