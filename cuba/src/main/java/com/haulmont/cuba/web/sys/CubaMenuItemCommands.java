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
import io.jmix.core.Entity;
import io.jmix.core.LoadContext;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.menu.MenuItemCommands;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.core.env.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;
import java.util.Map;

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
        return dataManager.load(ctx);
    }

}
