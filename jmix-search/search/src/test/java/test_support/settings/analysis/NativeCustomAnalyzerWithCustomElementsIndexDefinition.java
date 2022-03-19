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

package test_support.settings.analysis;

import io.jmix.search.index.annotation.AutoMappedField;
import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import test_support.entity.TestSimpleRootEntity;

@JmixEntitySearchIndex(entity = TestSimpleRootEntity.class)
public interface NativeCustomAnalyzerWithCustomElementsIndexDefinition {

    @AutoMappedField(includeProperties = "name", analyzer = "native_custom_analyzer_custom_elements")
    void mapping();
}
