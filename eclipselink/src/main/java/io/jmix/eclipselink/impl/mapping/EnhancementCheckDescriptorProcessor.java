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

package io.jmix.eclipselink.impl.mapping;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.Entity;
import io.jmix.core.entity.JmixSettersEnhanced;
import io.jmix.core.entity.annotation.DisableEnhancing;
import io.jmix.eclipselink.persistence.DescriptorProcessor;
import io.jmix.eclipselink.persistence.DescriptorProcessorContext;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.descriptors.PersistenceObject;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedFetchGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component("eclipselink_EnhancementCheckDescriptorProcessor")
public class EnhancementCheckDescriptorProcessor implements DescriptorProcessor {
    @Autowired
    protected Environment environment;

    private static final Logger log = LoggerFactory.getLogger(EnhancementCheckDescriptorProcessor.class);

    @SuppressWarnings("rawtypes")
    protected final Map<Class, Predicate<Class>> enhancingChecks = ImmutableMap.<Class, Predicate<Class>>builder()
            .put(JmixSettersEnhanced.class, EnhancementCheckDescriptorProcessor::isJmixEnhanced)
            .put(PersistenceObject.class, EnhancementCheckDescriptorProcessor::isPersistenceObject)
            .put(PersistenceWeaved.class, EnhancementCheckDescriptorProcessor::isPersistenceWeaved)
            .put(PersistenceWeavedFetchGroups.class, EnhancementCheckDescriptorProcessor::isPersistenceWeavedFetchGroups)
            .build();

    @Override
    public void process(DescriptorProcessorContext context) {
        ClassDescriptor descriptor = context.getDescriptor();

        List<String> missingInterfaces = enhancingChecks.entrySet().stream()
                .filter(entry -> !entry.getValue().test(descriptor.getJavaClass()))
                .map(Map.Entry::getKey)
                .map(Class::getSimpleName)
                .collect(Collectors.toList());

        if (!missingInterfaces.isEmpty()) {
            String message = String.format("Entity class %s is missing some of enhancing interfaces:%s",
                    descriptor.getJavaClass().getSimpleName(), Joiner.on(",").join(missingInterfaces));
            log.error("\n=================================================================" +
                    "\nProblems with entity enhancement detected:\n{}" +
                    "\n=================================================================", message);

            if (!Boolean.parseBoolean(environment.getProperty("jmix.data.disable-entity-enhancement-check"))) {
                throw new EntityNotEnhancedException(message);
            }
        }
    }

    protected static boolean isJmixEnhanced(Class<?> entityClass) {
        return ArrayUtils.contains(entityClass.getInterfaces(), JmixSettersEnhanced.class)
                || !(Entity.class.isAssignableFrom(entityClass))
                || ArrayUtils.contains(entityClass.getDeclaredAnnotations(), DisableEnhancing.class);
    }

    protected static boolean isPersistenceObject(Class<?> entityClass) {
        return ArrayUtils.contains(entityClass.getInterfaces(), PersistenceObject.class);
    }

    protected static boolean isPersistenceWeaved(Class<?> entityClass) {
        return ArrayUtils.contains(entityClass.getInterfaces(), PersistenceWeaved.class);
    }

    protected static boolean isPersistenceWeavedFetchGroups(Class<?> entityClass) {
        return ArrayUtils.contains(entityClass.getInterfaces(), PersistenceWeavedFetchGroups.class);
    }
}
