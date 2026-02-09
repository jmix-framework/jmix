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

/**
 * Meta description that describes facet.
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioFacet {

    /**
     * @see StudioComponent#name
     */
    String name() default "";

    /**
     * @see StudioComponent#classFqn
     */
    String classFqn() default "";

    /**
     * @see StudioComponent#category
     */
    String category() default "";

    /**
     * @see StudioComponent#icon
     */
    String icon() default "";

    /**
     * @see StudioComponent#xmlElement
     */
    String xmlElement() default "";

    /**
     * @see StudioComponent#xmlns
     */
    String xmlns() default "";

    /**
     * @see StudioComponent#xmlnsAlias
     */
    String xmlnsAlias() default "";

    /**
     * Describes the available place in the hierarchy.
     * By default, facet component can be located inside facets.
     */
    String availablePlaceRegExp() default "^((mainView)|(view)|(fragment))?(/facets)$";

    /**
     * @see StudioComponent#properties
     */
    StudioProperty[] properties() default {};

    /**
     * @see StudioComponent#documentationLink
     */
    String documentationLink() default "";
}
