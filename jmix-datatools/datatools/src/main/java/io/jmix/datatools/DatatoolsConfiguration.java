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

package io.jmix.datatools;

import io.jmix.core.Metadata;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.datatools.datamodel.EngineType;
import io.jmix.datatools.datamodel.engine.DiagramEngine;
import io.jmix.datatools.datamodel.engine.plantuml.impl.PlantUmlDiagramEngine;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = DataConfiguration.class)
@EnableTransactionManagement
public class DatatoolsConfiguration {

    @Bean("datatl_DiagramConstructor")
    public DiagramEngine diagramConstructor(Metadata metadata,
                                            DatatoolsProperties datatoolsProperties) {
        EngineType engineType = datatoolsProperties.getDiagramConstructor().getEngineType();

        switch (engineType) {
            case PLANTUML -> {
                return new PlantUmlDiagramEngine(datatoolsProperties, metadata);
            }
            case MERMAID -> {
                throw new IllegalStateException("Failed to create diagram service bean: " +
                        "Mermaid support is not yet implemented");
            }
            default -> throw new IllegalStateException("Failed to create diagram service bean");
        }
    }
}
