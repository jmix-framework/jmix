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

package io.jmix.securityflowui.model;

import com.google.common.base.Strings;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.view.DetailView;
import io.jmix.flowui.view.LookupView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Class is used for getting default policy group for the {@link ResourcePolicy} instance.
 */
@Component("sec_FlowuiDefaultResourcePolicyGroupResolver")
public class DefaultResourcePolicyGroupResolver {

    private final static Logger log = LoggerFactory.getLogger(DefaultResourcePolicyGroupResolver.class);

    protected ViewRegistry viewRegistry;
    protected MenuConfig menuConfig;
    protected Metadata metadata;

    public DefaultResourcePolicyGroupResolver(ViewRegistry viewRegistry,
                                              MenuConfig menuConfig,
                                              Metadata metadata) {
        this.viewRegistry = viewRegistry;
        this.menuConfig = menuConfig;
        this.metadata = metadata;
    }

    /**
     * Methods evaluates default policy group for a given {@link ResourcePolicy}.
     *
     * @param resourcePolicy a {@link ResourcePolicy}
     * @return a group policy or {@code null} if the group cannot be resolved
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
     * For view:
     *
     * <ul>
     *     <li>
     *         returns a policy group of the related entity (for standard detail and list views).
     *         If the view implements the {@link LookupView} or {@link DetailView} interfaces
     *         then a generic type is taken and assuming that a generic type points to the entity
     *         class the policy group of the entity is returned
     *     </li>
     * </ul>
     * <p>
     * For menu:
     *
     * <ul>
     *     <li>
     *         policy group for the related screen is returned
     *     </li>
     * </ul>
     *
     * @param resourcePolicyType a {@link ResourcePolicyType}, i.e. view, entity, menu, etc.
     * @param resource           a resource (viewId, entity name, etc.)
     * @return a policy group or {@code null} if policy group cannot be resolved
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
    protected String resolvePolicyGroupForEntityAttributePolicy(String resource) {
        String entityName = resource.substring(0, resource.lastIndexOf("."));
        return resolvePolicyGroupForEntityPolicy(entityName);
    }

    @Nullable
    protected String resolvePolicyGroupForEntityPolicy(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        return metaClass != null ? metaClass.getName() : null;
    }

    @Nullable
    protected String resolvePolicyGroupForScreenPolicy(String viewId) {
        return viewRegistry.findViewInfo(viewId).map(viewInfo -> {
            Class<? extends View<?>> controllerClass = viewInfo.getControllerClass();
            return resolvePolicyGroupByScreenControllerGenericType(controllerClass);
        }).orElse(null);
    }

    @Nullable
    protected String resolvePolicyGroupByScreenControllerGenericType(Class<? extends View<?>> controllerClass) {
        Class<?> typeArgument = null;
        if (DetailView.class.isAssignableFrom(controllerClass)) {
            typeArgument = GenericTypeResolver.resolveTypeArgument(controllerClass, DetailView.class);
        } else if (LookupView.class.isAssignableFrom(controllerClass)) {
            typeArgument = GenericTypeResolver.resolveTypeArgument(controllerClass, LookupView.class);
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
    protected String resolvePolicyGroupForMenuPolicy(String menuId) {
        MenuItem item = null;
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            item = menuConfig.findItem(menuId, rootItem);
            if (item != null) break;
        }

        if (item != null) {
            String viewId = item.getView();
            if (!Strings.isNullOrEmpty(viewId)) {
                return resolvePolicyGroupForScreenPolicy(viewId);
            }
        }
        return null;
    }
}