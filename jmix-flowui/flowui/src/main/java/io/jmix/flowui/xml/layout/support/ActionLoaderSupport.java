/*
 * Copyright (c) 2008-2022 Haulmont.
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

package io.jmix.flowui.xml.layout.support;

import io.jmix.core.security.EntityOp;
import io.jmix.flowui.Actions;
import io.jmix.flowui.action.SecurityConstraintAction;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.loader.ActionCustomPropertyLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("flowui_ActionLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ActionLoaderSupport {

    protected Context context;
    protected LoaderSupport loaderSupport;
    protected ActionCustomPropertyLoader propertyLoader;
    protected Actions actions;
    protected ComponentLoaderSupport componentLoaderSupport;

    ActionLoaderSupport(Context context) {
        this.context = context;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    @Autowired
    public void setActionCustomPropertyLoader(ActionCustomPropertyLoader actionCustomPropertyLoader) {
        this.propertyLoader = actionCustomPropertyLoader;
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setComponentLoaderSupport(ComponentLoaderSupport componentLoaderSupport) {
        this.componentLoaderSupport = componentLoaderSupport;
    }

    public Action loadDeclarativeAction(Element element) {
        return loadDeclarativeActionDefault(element);
    }

    protected void initAction(Element element, Action targetAction) {
        loaderSupport.loadString(element, "text", targetAction::setText);
        loaderSupport.loadBoolean(element, "enable", targetAction::setEnabled);
        loaderSupport.loadBoolean(element, "visible", targetAction::setVisible);

        loaderSupport.loadEnum(element, ActionVariant.class, "actionVariant",
                ((Action) targetAction)::setVariant);

        //todo gd refactor icon loading mechanism
        loaderSupport.loadString(element, "icon", targetAction::setIcon);

        componentLoaderSupport.loadShortcut(element).ifPresent(shortcut ->
                targetAction.setShortcutCombination(KeyCombination.create(shortcut)));

        Element propertiesEl = element.element("properties");
        if (propertiesEl != null) {
            for (Element propertyEl : propertiesEl.elements("property")) {
                loaderSupport.loadString(propertiesEl, "name",
                        name -> propertyLoader.load(targetAction, name, propertyEl.attributeValue("value")));
            }
        }
    }

    protected void loadActionConstraint(Action action, Element element) {
        if (action instanceof SecurityConstraintAction) {
            SecurityConstraintAction hasSecurityConstraint = (SecurityConstraintAction) action;
            loaderSupport.loadEnum(
                    element,
                    EntityOp.class,
                    "constraintEntityOp",
                    hasSecurityConstraint::setConstraintEntityOp
            );
        }
    }

    public Optional<Action> loadDeclarativeActionByType(Element element) {
        String id = loadActionId(element);

        return loaderSupport.loadString(element, "type")
                .map(typeId -> {
                    Action instance = actions.create(typeId, id);
                    initAction(element, instance);
                    loadActionConstraint(instance, element);
                    return instance;
                });
    }

    protected Action loadDeclarativeActionDefault(Element element) {
        String id = loadActionId(element);

        //String trackSelection = element.attributeValue("trackSelection");
        //boolean shouldTrackSelection = Boolean.parseBoolean(trackSelection);

        Action targetAction;

        //if (shouldTrackSelection) {
        //    Actions actions = getActions();
        //    targetAction = actions.create(ItemTrackingAction.ID, id);
        //loadActionConstraint(targetAction, element);
        //} else {
        targetAction = new BaseAction(id);
        // }

        initAction(element, targetAction);

        return targetAction;
    }

    protected String loadActionId(Element element) {
        return loaderSupport.loadString(element, "id").orElseThrow(() -> {
            {
                Element component = element;
                for (int i = 0; i < 2; i++) {
                    if (component.getParent() != null)
                        component = component.getParent();
                    else
                        throw new GuiDevelopmentException("No action ID provided", context);
                }
                throw new GuiDevelopmentException("No action ID provided", context,
                        "Component ID", component.attributeValue("id"));
            }
        });
    }
}
