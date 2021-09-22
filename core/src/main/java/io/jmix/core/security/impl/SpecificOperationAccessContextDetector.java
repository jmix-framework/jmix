/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.security.impl;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.impl.scanning.ClasspathScanCandidateDetector;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

@Component("core_SpecificOperationAccessContextDetector")
public class SpecificOperationAccessContextDetector implements ClasspathScanCandidateDetector {

    @Override
    public boolean isCandidate(MetadataReader metadataReader) {
        return SpecificOperationAccessContext.class.getName().equals(metadataReader.getClassMetadata().getSuperClassName());
    }
}
