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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Inherited
public @interface StudioFacet {

    String name() default "";

    String classFqn() default "";

    String category() default "";

    String icon() default "";

    String xmlElement() default "";

    String xmlns() default "";

    String xmlnsAlias() default "";

    /**
     * Describes the available place in the hierarchy.
     * By default, facet component can be located inside facets.
     */
    String availablePlaceRegExp() default "^((mainView)|(view))?(/facets)$";

    StudioProperty[] properties() default {};
}
