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

import com.vaadin.flow.component.HasComponents;

@Target(ElementType.METHOD)
@Inherited
public @interface StudioComponent {

    String name() default "";

    /**
     * Fully-qualified name of component class.
     * This will be used as a type of injected field.
     */
    String classFqn() default "";

    /**
     * Category for Component Palette.
     */
    String category() default "";

    /**
     * Icon for Component Palette and Component Inspector.
     */
    String icon() default "io/jmix/flowui/kit/meta/icon/unknownComponent.svg";

    /**
     * Xml tag name.
     */
    String xmlElement() default "";

    String xmlns() default "";

    String xmlnsAlias() default "";

    /**
     * Describes the available place in the Component Hierarchy.
     * By default, components can be located inside layout or inside a component inherited from {@link HasComponents}
     */
    String availablePlaceRegExp() default "((^(mainView/appLayout)?((/drawerLayout)|(/navigationBar)|(/initialLayout)))$)|(^view/layout$)" +
            "|((^(mainView/appLayout)?((/drawerLayout)|(/navigationBar)|(/initialLayout))|(^view/layout))?(/hasComponents)*$)";

    StudioProperty[] properties() default {};

    StudioPropertiesBinding[] propertiesBindings() default {};

    StudioSupplyHandler[] supplyHandlers() default {};

    StudioAvailableChildrenInfo availableChildren() default @StudioAvailableChildrenInfo();

    /**
     * Link to the component documentation.
     * <p>It can be of the following types:</p>
     * <ol>
     *  <li>The full link (starting with https).</li>
     *  <li>Or part of the jmix documentation link (part after /jmix/).</li>
     * </ol>
     *  Studio also can resolve {@code %VERSION%}} placeholder.
     */
    String documentationLink() default "";
}
