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
public @interface StudioElementsGroup {

    String name() default "";

    String elementClassFqn() default "";

    String icon() default "io/jmix/flowui/kit/meta/icon/unknownComponent.svg";

    String xmlElement() default "";

    String xmlns() default "";

    String xmlnsAlias() default "";

    String defaultProperty() default "";

    String[] target() default {};

    String[] unsupportedTarget() default {};

    boolean visible() default false;

    /**
     * Defines whether an elements group can be added to component an unlimited number of times.
     * By default, an elements group can only be added once.
     *
     * @return whether an elements group can be added to component an unlimited number of times
     */
    boolean unlimitedCount() default false;

    StudioProperty[] properties() default {};

    /**
     * @see StudioComponent#documentationLink()
     */
    String documentationLink() default "";
}
