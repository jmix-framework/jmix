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

import io.jmix.flowui.screen.UiController;
import io.jmix.security.impl.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityflowui.role.annotation.ScreenPolicy;
import io.jmix.securityflowui.role.annotation.ScreenPolicyContainer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("sec_ScreenPolicyExtractor")
public class ScreenPolicyExtractor implements ResourcePolicyExtractor {

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        Set<ScreenPolicy> annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method,
                ScreenPolicy.class, ScreenPolicyContainer.class);
        for (ScreenPolicy screenPolicyAnnotation : annotations) {
            for (String screenId : screenPolicyAnnotation.screenIds()) {
                ResourcePolicy resourcePolicy = ResourcePolicy.builder(ResourcePolicyType.SCREEN, screenId)
                        .withPolicyGroup(method.getName())
                        .build();
                resourcePolicies.add(resourcePolicy);
            }
            for (Class<?> screenClass : screenPolicyAnnotation.screenClasses()) {
                UiController uiControllerAnnotation = screenClass.getAnnotation(UiController.class);
                String screenId = uiControllerAnnotation.value();
                ResourcePolicy resourcePolicy = ResourcePolicy.builder(ResourcePolicyType.SCREEN, screenId)
                        .withPolicyGroup(method.getName())
                        .build();
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
