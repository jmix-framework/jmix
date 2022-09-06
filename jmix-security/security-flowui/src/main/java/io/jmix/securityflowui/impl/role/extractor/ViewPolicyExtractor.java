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

package io.jmix.securityflowui.impl.role.extractor;

import io.jmix.flowui.view.ViewController;
import io.jmix.security.impl.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityflowui.role.annotation.ViewPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicyContainer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("sec_FlowuiViewPolicyExtractor")
public class ViewPolicyExtractor implements ResourcePolicyExtractor {

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        Set<ViewPolicy> annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method,
                ViewPolicy.class, ViewPolicyContainer.class);
        for (ViewPolicy viewPolicyAnnotation : annotations) {
            for (String viewId : viewPolicyAnnotation.viewIds()) {
                ResourcePolicy resourcePolicy = ResourcePolicy.builder(ResourcePolicyType.SCREEN, viewId)
                        .withPolicyGroup(method.getName())
                        .build();
                resourcePolicies.add(resourcePolicy);
            }
            for (Class<?> viewClass : viewPolicyAnnotation.viewClasses()) {
                ViewController viewControllerAnnotation = viewClass.getAnnotation(ViewController.class);
                String viewId = viewControllerAnnotation.value();
                ResourcePolicy resourcePolicy = ResourcePolicy.builder(ResourcePolicyType.SCREEN, viewId)
                        .withPolicyGroup(method.getName())
                        .build();
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
