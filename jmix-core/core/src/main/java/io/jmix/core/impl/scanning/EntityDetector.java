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

package io.jmix.core.impl.scanning;

import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

@Internal
@Component("core_EntityDetector")
public class EntityDetector implements ClasspathScanCandidateDetector {

    @Override
    public boolean isCandidate(MetadataReader metadataReader) {
        return metadataReader.getAnnotationMetadata().hasAnnotation(JmixEntity.class.getName());
    }
}
