/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.sys;

import com.google.common.collect.ImmutableMap;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import io.jmix.core.Entity;
import io.jmix.core.LoadContext;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Window;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.menu.MenuItemCommand;
import io.jmix.ui.menu.MenuItemCommands;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.sys.UiControllerProperty;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

public class CubaMenuItemCommands extends MenuItemCommands {

    @Autowired
    protected Environment environment;

    protected Map<String, Object> loadParams(MenuItem item) {
        Element descriptor = item.getDescriptor();
        if (descriptor == null) {
            return Collections.emptyMap();
        }

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (Element element : descriptor.elements("param")) {
            String value = element.attributeValue("value");
            EntityLoadInfo info = EntityLoadInfo.parse(value);
            if (info == null) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    Boolean booleanValue = Boolean.valueOf(value);
                    builder.put(element.attributeValue("name"), booleanValue);
                } else {
                    if (value.startsWith("${") && value.endsWith("}")) {
                        String property = environment.getProperty(value.substring(2, value.length() - 1));
                        if (!StringUtils.isEmpty(property)) {
                            value = property;
                        }
                    }
                    builder.put(element.attributeValue("name"), value);
                }
            } else {
                builder.put(element.attributeValue("name"), loadEntityInstance(info));
            }
        }

        String screen = item.getScreen();

        if (StringUtils.isNotEmpty(screen)) {
            WindowInfo windowInfo = windowConfig.getWindowInfo(screen);
            // caption is passed only for legacy screens
            if (windowInfo.getDescriptor() != null) {
                String caption = menuConfig.getItemCaption(item);

                builder.put("caption", caption);
            }
        }

        return builder.build();
    }

    protected Entity loadEntityInstance(EntityLoadInfo info) {
        LoadContext ctx = new LoadContext(info.getMetaClass()).setId(info.getId());
        if (info.getFetchPlanName() != null) {
            ctx.setFetchPlan(fetchPlanRepository.getFetchPlan(info.getMetaClass(), info.getFetchPlanName()));
        }

        //noinspection unchecked
        return (Entity) dataManager.load(ctx);
    }

    @Override
    protected MenuItemCommand createScreenCommand(FrameOwner origin, MenuItem item, Map<String, Object> params, List<UiControllerProperty> properties) {
        return new CubaScreenCommand(origin, item, item.getScreen(), item.getDescriptor(), params, properties);
    }

    protected class CubaScreenCommand extends ScreenCommand {

        protected CubaScreenCommand(FrameOwner origin, MenuItem item, String screen, Element descriptor, Map<String, Object> params, List<UiControllerProperty> controllerProperties) {
            super(origin, item, screen, descriptor, params, controllerProperties);
        }

        @Nullable
        @Override
        protected Screen createScreen(WindowInfo windowInfo, String screenId) {
            Screens screens = getScreenContext(origin).getScreens();
            if (windowInfo.getDescriptor() != null) {
                // legacy screens

                Map<String, Object> paramsMap = parseLegacyScreenParams(windowInfo.getDescriptor());
                paramsMap.putAll(params);

                if (screenId.endsWith(Window.CREATE_WINDOW_SUFFIX)
                        || screenId.endsWith(Window.EDITOR_WINDOW_SUFFIX)) {
                    return ((WindowManager) screens).createEditor(windowInfo, (Entity) getEntityToEdit(screenId), getOpenType(descriptor), paramsMap);
                } else {
                    return screens.create(screenId, getOpenMode(descriptor), new MapScreenOptions(paramsMap));
                }
            } else {
                return super.createScreen(windowInfo, screenId);
            }
        }

        protected OpenType getOpenType(Element descriptor) {
            OpenType openType = OpenType.NEW_TAB;

            String openTypeStr = descriptor.attributeValue("openType");
            if (StringUtils.isNotEmpty(openTypeStr)) {
                openType = OpenType.valueOf(openTypeStr);
            }

            if (openType.getOpenMode() == OpenMode.DIALOG) {
                Boolean resizable = getResizable(descriptor);
                if (resizable != null) {
                    openType = openType.resizable(resizable);
                }
            }

            return openType;
        }

        // CAUTION copied from com.haulmont.cuba.web.sys.WebScreens#createParametersMap
        protected Map<String, Object> parseLegacyScreenParams(Element descriptor) {
            Map<String, Object> map = new HashMap<>();

            Element paramsElement = descriptor.element("params") != null ? descriptor.element("params") : descriptor;
            if (paramsElement != null) {
                List<Element> paramElements = paramsElement.elements("param");
                for (Element paramElement : paramElements) {
                    String name = paramElement.attributeValue("name");
                    String value = paramElement.attributeValue("value");
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                        Boolean booleanValue = Boolean.valueOf(value);
                        map.put(name, booleanValue);
                    } else {
                        map.put(name, value);
                    }
                }
            }

            return map;
        }
    }
}
