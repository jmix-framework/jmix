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

package io.jmix.flowui.xml.layout.support;

import io.jmix.core.security.EntityOp;
import io.jmix.flowui.Actions;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.SecurityConstraintAction;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.loader.ActionCustomPropertyLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.jmix.flowui.kit.component.ComponentUtils.parseIcon;

@Component("flowui_ActionLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ActionLoaderSupport implements ApplicationContextAware {

    protected Context context;
    protected ApplicationContext applicationContext;
    protected LoaderSupport loaderSupport;
    protected ActionCustomPropertyLoader propertyLoader;
    protected Actions actions;
    protected ComponentLoaderSupport componentLoaderSupport;

    public ActionLoaderSupport(Context context) {
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Action loadDeclarativeAction(Element element) {
        return loadDeclarativeActionDefault(element);
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

    public void loadActions(HasActions component, Element element) {
        Element actions = element.element("actions");
        if (actions == null) {
            return;
        }

        for (Element action : actions.elements("action")) {
            Action actionToSet = loadDeclarativeActionByType(action)
                    .orElseGet(() ->
                            loadDeclarativeAction(action));
            component.addAction(actionToSet);
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

        Action targetAction = new SecuredBaseAction(id);

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

    protected void initAction(Element element, Action targetAction) {
        loaderSupport.loadResourceString(element, "text",
                context.getMessageGroup(), targetAction::setText);
        loaderSupport.loadResourceString(element, "description",
                context.getMessageGroup(), targetAction::setDescription);
        loaderSupport.loadBoolean(element, "enabled", targetAction::setEnabled);
        loaderSupport.loadBoolean(element, "visible", targetAction::setVisible);

        loaderSupport.loadEnum(element, ActionVariant.class, "actionVariant",
                ((Action) targetAction)::setVariant);

        loaderSupport.loadString(element, "icon")
                .ifPresent(iconString ->
                        targetAction.setIcon(parseIcon(iconString)));

        componentLoader().loadShortcutCombination(element).ifPresent(shortcutCombination ->
                targetAction.setShortcutCombination(KeyCombination.create(shortcutCombination)));

        Element propertiesEl = element.element("properties");
        if (propertiesEl != null) {
            for (Element propertyEl : propertiesEl.elements("property")) {
                loaderSupport.loadString(propertyEl, "name",
                        name -> propertyLoader.load(targetAction, name, propertyEl.attributeValue("value")));
            }
        }
    }

    protected ComponentLoaderSupport componentLoader() {
        if (componentLoaderSupport == null) {
            componentLoaderSupport = applicationContext.getBean(ComponentLoaderSupport.class, context);
        }
        return componentLoaderSupport;
    }
}
