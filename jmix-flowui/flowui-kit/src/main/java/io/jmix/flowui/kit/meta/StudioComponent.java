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

/**
 * Meta description that describes component.
 */
@Target(ElementType.METHOD)
@Inherited
public @interface StudioComponent {

    /**
     * Presentable name that Studio will use in palette.
     */
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
     * Path to the icon that Studio will use in palette and inspector.
     */
    String icon() default "io/jmix/flowui/kit/meta/icon/unknownComponent.svg";

    /**
     * Xml tag local name.
     */
    String xmlElement() default "";

    /**
     * FQN of xml namespace schema.
     */
    String xmlns() default "";

    /**
     * Xml namespace alias.
     */
    String xmlnsAlias() default "";

    /**
     * Describes the available place in the Component Hierarchy.
     * By default, components can be located inside layout (or fragment content)
     * or inside a component inherited from {@link HasComponents}
     */
    String availablePlaceRegExp() default "((^(mainView/appLayout)?((/drawerLayout)|(/navigationBar)|(/initialLayout)))$)|(^view/layout$)|(^fragment/content)" +
            "|((^(mainView/appLayout)?((/drawerLayout)|(/navigationBar)|(/initialLayout))|(^view/layout)|(^fragment/content))?(/hasComponents)*$)";

    /**
     * Descriptions of the xml attributes.
     */
    StudioProperty[] properties() default {};

    /**
     * Descriptions of the xml attributes relationship.
     * For example <code>property</code> and <code>dataContainer</code>
     */
    StudioPropertiesBinding[] propertiesBindings() default {};

    /**
     * Additional non-standard handlers.
     */
    StudioSupplyHandler[] supplyHandlers() default {};

    /**
     * Describes the available children information.
     * @see StudioAvailableChildrenInfo
     */
    StudioAvailableChildrenInfo availableChildren() default @StudioAvailableChildrenInfo();

    /**
     * Describes the conversion information.
     * @see StudioConvertStrategy
     */
    StudioConvertStrategy convertStrategy() default @StudioConvertStrategy();

    /**
     * Link to the component documentation.
     * <p>It can be of the following types:</p>
     * <ol>
     *  <li>The full link (starting with https).</li>
     *  <li>Or part of the jmix documentation link (part after /jmix/).</li>
     * </ol>
     *  Studio also can resolve {@code %VERSION%} placeholder.
     */
    String documentationLink() default "";
}
