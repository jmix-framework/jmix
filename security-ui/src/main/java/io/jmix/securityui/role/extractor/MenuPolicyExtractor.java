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

package io.jmix.securityui.role.extractor;

import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.securityui.role.annotation.MenuPolicy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component(MenuPolicyExtractor.NAME)
public class MenuPolicyExtractor implements ResourcePolicyExtractor {

    public static final String NAME = "sec_MenuPolicyExtractor";

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        MenuPolicy[] menuPolicyAnnotations = method.getAnnotationsByType(MenuPolicy.class);
        for (MenuPolicy menuPolicyAnnotation : menuPolicyAnnotations) {
            for (String menuId : menuPolicyAnnotation.menuIds()) {
                ResourcePolicy resourcePolicy = new ResourcePolicy(ResourcePolicyType.MENU, menuId);
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
