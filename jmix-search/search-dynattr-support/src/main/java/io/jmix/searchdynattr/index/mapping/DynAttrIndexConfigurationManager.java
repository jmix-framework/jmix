/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchdynattr.index.mapping;

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor;
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("search_dynattr_support_DynAttrIndexConfigurationManager")
public class DynAttrIndexConfigurationManager extends IndexConfigurationManager {
    public DynAttrIndexConfigurationManager(JmixModulesClasspathScanner classpathScanner,
                                            AnnotatedIndexDefinitionProcessor indexDefinitionProcessor,
                                            InstanceNameProvider instanceNameProvider,
                                            IndexDefinitionDetector indexDefinitionDetector,
                                            MetadataTools metadataTools) {
        super(classpathScanner, indexDefinitionProcessor, instanceNameProvider, indexDefinitionDetector, metadataTools);
    }
}
