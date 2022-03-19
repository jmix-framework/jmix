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

package io.jmix.securityui.model;

import com.google.common.base.Strings;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.menu.MenuConfig;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.LookupScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Class is used for getting default policy group for the {@link ResourcePolicy} instance.
 */
@Component("sec_DefaultResourcePolicyGroupResolver")
public class DefaultResourcePolicyGroupResolver {

    @Autowired
    private Metadata metadata;

    @Autowired
    private WindowConfig windowConfig;

    @Autowired
    private MenuConfig menuConfig;

    private final static Logger log = LoggerFactory.getLogger(DefaultResourcePolicyGroupResolver.class);

    /**
     * Methods evaluates default policy group for a given {@link ResourcePolicy}.
     *
     * @param resourcePolicy a {@link ResourcePolicy}
     * @return a group policy or null if the group cannot be resolved
     * @see #resolvePolicyGroup(String, String)
     */
    @Nullable
    public String resolvePolicyGroup(ResourcePolicy resourcePolicy) {
        return resolvePolicyGroup(resourcePolicy.getType(), resourcePolicy.getResource());
    }

    /**
     * Methods evaluates security policy group for a given resource of the {@link ResourcePolicy}.
     * <p>
     * For entity:
     *
     * <ul>
     *     <li>
     *         the entity name is returned
     *     </li>
     * </ul>
     * <p>
     * For entity attributes:
     *
     * <ul>
     *     <li>a policy group for the attribute's entity is returned</li>
     * </ul>
     * <p>
     * For screen:
     *
     * <ul>
     *     <li>
     *         returns a policy group of the related entity (for standard editor and browser). If the screen implements
     *         the {@link LookupScreen} or {@link EditorScreen} interfaces then a generic type is taken
     *         and assuming that a generic type points to the entity class the policy group of the entity is returned
     *     </li>
     * </ul>
     * <p>
     * <p>
     * For menu:
     *
     * <ul>
     *     <li>
     *         policy group for the related screen is returned
     *     </li>
     * </ul>
     *
     * @param resourcePolicyType a {@link ResourcePolicyType}, i.e. screen, entity, menu, etc.
     * @param resource           a resource (screenId, entity name, etc.)
     * @return a policy group or null if policy group cannot be resolved
     * @see ResourcePolicyType
     */
    @Nullable
    public String resolvePolicyGroup(String resourcePolicyType, String resource) {
        switch (resourcePolicyType) {
            case ResourcePolicyType.SCREEN:
                return resolvePolicyGroupForScreenPolicy(resource);
            case ResourcePolicyType.ENTITY:
                return resolvePolicyGroupForEntityPolicy(resource);
            case ResourcePolicyType.ENTITY_ATTRIBUTE:
                return resolvePolicyGroupForEntityAttributePolicy(resource);
            case ResourcePolicyType.MENU:
                return resolvePolicyGroupForMenuPolicy(resource);
            default:
                return null;
        }
    }

    @Nullable
    private String resolvePolicyGroupForEntityAttributePolicy(String resource) {
        String entityName = resource.substring(0, resource.lastIndexOf("."));
        return resolvePolicyGroupForEntityPolicy(entityName);
    }

    @Nullable
    private String resolvePolicyGroupForEntityPolicy(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        if (metaClass != null) {
            return metaClass.getName();
        }
        return null;
    }

    @Nullable
    private String resolvePolicyGroupForScreenPolicy(String screenId) {
        WindowInfo windowInfo = windowConfig.findWindowInfo(screenId);
        if (windowInfo != null) {
            Class<? extends FrameOwner> controllerClass = windowInfo.getControllerClass();
            return resolvePolicyGroupByScreenControllerGenericType(controllerClass);
        }
        return null;
    }

    @Nullable
    private String resolvePolicyGroupByScreenControllerGenericType(Class<? extends FrameOwner> controllerClass) {
        Class<?> typeArgument = null;
        if (EditorScreen.class.isAssignableFrom(controllerClass)) {
            typeArgument = GenericTypeResolver.resolveTypeArgument(controllerClass, EditorScreen.class);
        } else if (LookupScreen.class.isAssignableFrom(controllerClass)) {
            typeArgument = GenericTypeResolver.resolveTypeArgument(controllerClass, LookupScreen.class);
        }
        if (typeArgument != null) {
            if (Entity.class.isAssignableFrom(typeArgument)) {
                MetaClass metaClass = metadata.findClass(typeArgument);
                if (metaClass != null) {
                    return resolvePolicyGroupForEntityPolicy(metaClass.getName());
                }
            }
        }
        return null;
    }

    @Nullable
    private String resolvePolicyGroupForMenuPolicy(String menuId) {
        MenuItem item = null;
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            item = menuConfig.findItem(menuId, rootItem);
            if (item != null) break;
        }

        if (item != null) {
            String screenId = item.getScreen();
            if (!Strings.isNullOrEmpty(screenId)) {
                return resolvePolicyGroupForScreenPolicy(screenId);
            }
        }
        return null;
    }
}