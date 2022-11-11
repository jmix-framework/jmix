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

package io.jmix.flowui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.flowui")
@ConstructorBinding
public class FlowuiProperties {

    /**
     * View that will be used as Login view.
     */
    String loginViewId;

    /**
     * View that will be used as Main view.
     */
    String mainViewId;

    /**
     * Defines whether menu should be built with menu items from add-ons. {@code true} means using menu items from
     * add-ons, {@code false} - using only menu configuration from the application. The default value is {@code true}.
     */
    boolean compositeMenu;

    Integer defaultMaxFetchSize;
    Map<String, Integer> entityMaxFetchSize;

    Integer defaultPageSize;
    Map<String, Integer> entityPageSize;

    public FlowuiProperties(@DefaultValue("login") String loginViewId,
                            @DefaultValue("main") String mainViewId,
                            @DefaultValue("true") boolean compositeMenu,
                            @DefaultValue("10000") Integer defaultMaxFetchSize,
                            @Nullable Map<String, Integer> entityMaxFetchSize,
                            @DefaultValue("50") Integer defaultPageSize,
                            @Nullable Map<String, Integer> entityPageSize) {
        this.loginViewId = loginViewId;
        this.mainViewId = mainViewId;
        this.compositeMenu = compositeMenu;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
        this.defaultPageSize = defaultPageSize;
        this.entityPageSize = entityPageSize == null ? Collections.emptyMap() : entityPageSize;
    }

    /**
     * @see #loginViewId
     */
    public String getLoginViewId() {
        return loginViewId;
    }

    /**
     * @see #mainViewId
     */
    public String getMainViewId() {
        return mainViewId;
    }

    /**
     * @see #compositeMenu
     */
    public boolean isCompositeMenu() {
        return compositeMenu;
    }

    public int getDefaultMaxFetchSize() {
        return defaultMaxFetchSize;
    }

    public int getEntityMaxFetchSize(String entityName) {
        Integer forEntity = entityMaxFetchSize.get(entityName);
        if (forEntity != null)
            return forEntity;
        return defaultMaxFetchSize;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public int getEntityPageSize(String entityName) {
        Integer forEntity = entityPageSize.get(entityName);
        if (forEntity != null)
            return forEntity;
        return defaultPageSize;
    }
}
