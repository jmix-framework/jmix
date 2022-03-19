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

package io.jmix.search.index.mapping.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("search_MappingFieldAnnotationProcessorsRegistry")
public class MappingFieldAnnotationProcessorsRegistry {

    private static final Logger log = LoggerFactory.getLogger(MappingFieldAnnotationProcessorsRegistry.class);

    private final Map<Class<? extends Annotation>, FieldAnnotationProcessor<?>> registry;

    @Autowired
    public MappingFieldAnnotationProcessorsRegistry(List<FieldAnnotationProcessor<?>> processors) {
        Map<Class<? extends Annotation>, FieldAnnotationProcessor<?>> tmpRegistry = new HashMap<>();
        for (FieldAnnotationProcessor<?> processor : processors) {
            log.debug("Register processor '{}' for annotation '{}'", processor, processor.getAnnotationClass());
            tmpRegistry.put(processor.getAnnotationClass(), processor);
        }
        this.registry = tmpRegistry;
    }

    public Optional<FieldAnnotationProcessor<? extends Annotation>> getProcessorForAnnotationClass(Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(registry.get(annotationClass));
    }
}
