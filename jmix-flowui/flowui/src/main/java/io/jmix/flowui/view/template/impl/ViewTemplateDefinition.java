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

package io.jmix.flowui.view.template.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.View;

/**
 * Describes a view generated from an entity template annotation.
 */
public class ViewTemplateDefinition {

    protected String id;
    protected ViewTemplateType type;
    protected MetaClass entityMetaClass;
    protected String descriptorPath;
    protected String routePath;
    protected Class<? extends View<?>> controllerClass;
    protected String title;
    protected String parentMenu;

    /**
     * Creates a template view definition.
     *
     * @param id             view id
     * @param type           template view type
     * @param entityMetaClass entity meta-class
     * @param descriptorPath descriptor path registered in the descriptor registry
     * @param routePath      route path registered for the generated controller
     * @param controllerClass generated controller class
     * @param title          resolved view title
     * @param parentMenu     parent menu item id or an empty string
     */
    public ViewTemplateDefinition(String id,
                                  ViewTemplateType type,
                                  MetaClass entityMetaClass,
                                  String descriptorPath,
                                  String routePath,
                                  Class<? extends View<?>> controllerClass,
                                  String title,
                                  String parentMenu) {
        this.id = id;
        this.type = type;
        this.entityMetaClass = entityMetaClass;
        this.descriptorPath = descriptorPath;
        this.routePath = routePath;
        this.controllerClass = controllerClass;
        this.title = title;
        this.parentMenu = parentMenu;
    }

    /**
     * @return registered view id
     */
    public String getId() {
        return id;
    }

    /**
     * @return template view type
     */
    public ViewTemplateType getType() {
        return type;
    }

    /**
     * @return entity meta-class for which the view was generated
     */
    public MetaClass getEntityMetaClass() {
        return entityMetaClass;
    }

    /**
     * @return descriptor path registered for the generated view
     */
    public String getDescriptorPath() {
        return descriptorPath;
    }

    /**
     * @return route path registered for the generated view
     */
    public String getRoutePath() {
        return routePath;
    }

    /**
     * @return generated controller class
     */
    public Class<? extends View<?>> getControllerClass() {
        return controllerClass;
    }

    /**
     * @return resolved view title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return parent menu item id or an empty string if no menu item should be created
     */
    public String getParentMenu() {
        return parentMenu;
    }
}
