/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the beans of the data-load subsystem: its services, validators and AI tools.
 * <p>
 * The {@code io.jmix.aitools.dataload} package is excluded from the {@link AiToolsConfiguration}
 * component scan, and this configuration — which scans that package explicitly — is imported by the
 * starter autoconfiguration only when the {@code jmix.aitools.dataload.enabled} property is not set
 * to {@code false}, so the whole feature can be switched off as a single unit.
 */
@Configuration
@ComponentScan("io.jmix.aitools.dataload")
public class AiToolsDataLoadConfiguration {
}
