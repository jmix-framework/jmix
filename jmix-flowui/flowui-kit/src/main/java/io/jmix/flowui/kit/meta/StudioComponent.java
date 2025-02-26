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
     * XML attribute that should be used as identifier for injection or handler target.
     * <p></p>
     * Default value is <code>id</code>.
     */
    String injectionIdentifier() default "id";

    /**
     * Determines whether a component can be injected into controller.
     * Default value is <code>true</code>.
     */
    boolean isInjectable() default true;

    /**
     * Path to the icon that Studio will use in palette and inspector.
     */
    String icon() default "io/jmix/flowui/kit/meta/icon/unknownComponent.svg";

    /**
     * XML tag local name.
     */
    String xmlElement() default "";

    /**
     * FQN of XML namespace schema.
     */
    String xmlns() default "";

    /**
     * XML namespace alias.
     */
    String xmlnsAlias() default "";

    /**
     * Describes the available place in the Component Hierarchy.
     * By default, components can be located inside:
     * <ul>
     * <li>{@code view/layout/hasComponents}</li>
     * <li>{@code mainView/appLayout/navigationBar/[hasComponents]}</li>
     * <li>{@code mainView/appLayout/drawerLayout/[hasComponents]}</li>
     * <li>{@code mainView/appLayout/initialLayout/[hasComponents]}</li>
     * <li>{@code mainView/appLayout/workArea/initialLayout/[hasComponents]} - if mainView from tabbed mode</li>
     * <li>{@code fragment/content}</li>
     * </ul>
     *
     * <h4>Notes:</h4>
     * <p>
     *    {@code hasComponents} – placeholder for a component inherited from {@link HasComponents}.
     * </p>
     * <p>
     *     {@code [hasComponents]} - means that {@code hasComponents} is an <b>optional</b> part
     * </p>
     */
    String availablePlaceRegExp() default "(^(mainView/appLayout)?((/drawerLayout)|(/navigationBar)" +
            "|(/workArea/initialLayout)|(/initialLayout))$)|(^view/layout$)|(^fragment/content)" +
            "|((^(mainView/appLayout)?((/drawerLayout)|(/navigationBar)|(/workArea/initialLayout)|(/initialLayout))" +
            "|(^view/layout)|(^fragment/content))?(/hasComponents)*$)";

    /**
     * Descriptions of the XML attributes.
     */
    StudioProperty[] properties() default {};

    /**
     * Descriptions of the XML attributes relationship.
     * For example <code>property</code> and <code>dataContainer</code>
     */
    StudioPropertiesBinding[] propertiesBindings() default {};

    /**
     * Additional non-standard handlers.
     */
    StudioSupplyHandler[] supplyHandlers() default {};

    /**
     * Describes the available children information.
     *
     * @see StudioAvailableChildrenInfo
     */
    StudioAvailableChildrenInfo availableChildren() default @StudioAvailableChildrenInfo();

    /**
     * Describes the conversion information.
     *
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
