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
import io.jmix.securityui.role.annotation.ScreenPolicy;
import io.jmix.ui.screen.UiController;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component(ScreenPolicyExtractor.NAME)
public class ScreenPolicyExtractor implements ResourcePolicyExtractor {

    public static final String NAME = "sec_ScreenPolicyExtractor";

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        ScreenPolicy[] screenPolicyAnnotations = method.getAnnotationsByType(ScreenPolicy.class);
        for (ScreenPolicy screenPolicyAnnotation : screenPolicyAnnotations) {
            for (String screenId : screenPolicyAnnotation.screenIds()) {
                ResourcePolicy resourcePolicy = new ResourcePolicy(ResourcePolicyType.SCREEN, screenId);
                resourcePolicies.add(resourcePolicy);
            }
            for (Class screenClass : screenPolicyAnnotation.screenClasses()) {
                UiController uiControllerAnnotation = (UiController) screenClass.getAnnotation(UiController.class);
                String screenId = uiControllerAnnotation.value();
                ResourcePolicy resourcePolicy = new ResourcePolicy(ResourcePolicyType.SCREEN, screenId);
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
