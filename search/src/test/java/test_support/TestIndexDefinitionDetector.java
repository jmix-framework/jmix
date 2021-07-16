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

package test_support;

import io.jmix.search.index.mapping.processor.IndexDefinitionDetector;
import org.springframework.core.type.classreading.MetadataReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Detects only specified Index Definition classes/packages
 */
public class TestIndexDefinitionDetector extends IndexDefinitionDetector {

    protected final TestAutoDetectableIndexDefinitionScope scope;

    public TestIndexDefinitionDetector(@Nullable TestAutoDetectableIndexDefinitionScope scope) {
        this.scope = scope == null ? TestAutoDetectableIndexDefinitionScope.builder().build() : scope;
    }

    @Override
    public boolean isCandidate(@Nonnull MetadataReader metadataReader) {
        return super.isCandidate(metadataReader) && isShouldBeDetected(metadataReader);
    }

    protected boolean isShouldBeDetected(MetadataReader metadataReader) {
        boolean matchClass = scope.getClasses().stream()
                .map(Class::getName)
                .anyMatch((c) -> c.equals(metadataReader.getClassMetadata().getClassName()));
        if (matchClass) {
            return true;
        }
        return scope.getPackages().stream().anyMatch((p) -> metadataReader.getClassMetadata().getClassName().startsWith(p));
    }
}
