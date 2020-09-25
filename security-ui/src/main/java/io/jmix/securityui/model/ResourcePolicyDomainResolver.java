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
import io.jmix.core.JmixEntity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityui.role.annotation.SecurityDomain;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.menu.MenuConfig;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.LookupScreen;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Class is used for getting security domain name by the {@link ResourcePolicy} instance.
 *
 * @see SecurityDomain
 */
@Component("sec_ResourcePolicyDomainResolver")
public class ResourcePolicyDomainResolver {

    @Autowired
    private Metadata metadata;

    @Autowired
    private WindowConfig windowConfig;

    @Autowired
    private MenuConfig menuConfig;

    private final static Logger log = LoggerFactory.getLogger(ResourcePolicyDomainResolver.class);

    /**
     * Methods evaluates security domain for a given {@link ResourcePolicy}.
     * @param resourcePolicy a {@link ResourcePolicy}
     * @see #resolveDomain(String, String)
     * @return a domain name or null if domain name cannot be resolved
     */
    @Nullable
    public String resolveDomain(ResourcePolicy resourcePolicy) {
        return resolveDomain(resourcePolicy.getType(), resourcePolicy.getResource());
    }

    /**
     * Methods evaluates security domain for a given resource of the {@link ResourcePolicy}.
     * <p>
     * For entity:
     *
     * <ul>
     *     <li>
     *         if the entity class has a {@link SecurityDomain} annotation, then a domain name from annotation is returned
     *     </li>
     *     <li>
     *         otherwise an entity name is returned
     *     </li>
     * </ul>
     * <p>
     * For entity attributes:
     *
     * <ul>
     *     <li>a domain name for the attribute's entity is returned</li>
     * </ul>
     * <p>
     * For screen:
     *
     * <ul>
     *     <li>
     *         if screen controller class has a {@link SecurityDomain} annotation, then a domain name from the
     *         annotation is returned
     *     </li>
     *     <li>
     *         returns a domain of the related entity (for standard editor and browser). If the screen implements
     *         the {@link LookupScreen} or {@link EditorScreen} interfaces then a generic type is taken
     *         and assuming that a generic type points to the entity class the entity domain is returned
     *     </li>
     * </ul>
     * <p>
     * <p>
     * For menu:
     *
     * <ul>
     *     <li>
     *         if menu item in menu.xml file has a {@code securityDomain} attribute, the attribute value is returned
     *     </li>
     *     <li>
     *         otherwise a domain for the related screen is returned
     *     </li>
     * </ul>
     *
     * @param resourcePolicyType a {@link ResourcePolicyType}, i.e. screen, entity, menu, etc.
     * @param resource a resource (screenId, entity name, etc.)
     * @return a security domain name or null if domain name cannot be resolved
     * @see ResourcePolicyType
     */
    @Nullable
    public String resolveDomain(String resourcePolicyType, String resource) {
        switch (resourcePolicyType) {
            case ResourcePolicyType.SCREEN:
                return resolveDomainForScreenPolicy(resource);
            case ResourcePolicyType.ENTITY:
                return resolveDomainForEntityPolicy(resource);
            case ResourcePolicyType.ENTITY_ATTRIBUTE:
                return resolveDomainForEntityAttributePolicy(resource);
            case ResourcePolicyType.MENU:
                return resolveDomainForMenuPolicy(resource);
            default:
                return null;
        }
    }

    @Nullable
    private String resolveDomainForEntityAttributePolicy(String resource) {
        String entityName = resource.substring(0, resource.lastIndexOf("."));
        return resolveDomainForEntityPolicy(entityName);
    }

    @Nullable
    private String resolveDomainForEntityPolicy(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        if (metaClass != null) {
            Class<Object> entityJavaClass = metaClass.getJavaClass();
            if (entityJavaClass.isAnnotationPresent(SecurityDomain.class)) {
                SecurityDomain domainAnnotation = entityJavaClass.getAnnotation(SecurityDomain.class);
                return domainAnnotation.name();
            } else {
                return metaClass.getName();
            }
        }
        return null;
    }

    @Nullable
    private String resolveDomainForScreenPolicy(String screenId) {
        WindowInfo windowInfo = windowConfig.findWindowInfo(screenId);
        if (windowInfo != null) {
            Class<? extends FrameOwner> controllerClass = windowInfo.getControllerClass();
            SecurityDomain domainAnnotation = AnnotationUtils.findAnnotation(controllerClass, SecurityDomain.class);
            if (domainAnnotation != null) {
                return domainAnnotation.name();
            } else {
                return resolveDomainByScreenControllerGenericType(controllerClass);
            }
        }
        return null;
    }

    @Nullable
    private String resolveDomainByScreenControllerGenericType(Class<? extends FrameOwner> controllerClass) {
        Class<?> typeArgument = null;
        if (EditorScreen.class.isAssignableFrom(controllerClass)) {
            typeArgument = GenericTypeResolver.resolveTypeArgument(controllerClass, EditorScreen.class);
        } else if (LookupScreen.class.isAssignableFrom(controllerClass)) {
            typeArgument = GenericTypeResolver.resolveTypeArgument(controllerClass, LookupScreen.class);
        }
        if (typeArgument != null) {
            if (JmixEntity.class.isAssignableFrom(typeArgument)) {
                MetaClass metaClass = metadata.findClass(typeArgument);
                if (metaClass != null) {
                    return resolveDomainForEntityPolicy(metaClass.getName());
                }
            }
        }
        return null;
    }

    @Nullable
    private String resolveDomainForMenuPolicy(String menuId) {
        MenuItem item = null;
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            item = menuConfig.findItem(menuId, rootItem);
            if (item != null) break;
        }

        if (item != null) {
            Element descriptor = item.getDescriptor();
            String domain = descriptor.attributeValue("securityDomain");
            if (!Strings.isNullOrEmpty(domain)) {
                return domain;
            } else {
                String screenId = item.getScreen();
                if (!Strings.isNullOrEmpty(screenId)) {
                    return resolveDomainForScreenPolicy(screenId);
                }
            }
        }
        return null;
    }
}