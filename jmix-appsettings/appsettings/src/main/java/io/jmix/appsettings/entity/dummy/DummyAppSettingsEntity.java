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

package io.jmix.appsettings.entity.dummy;

import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Entity;

// TODO: remove after
//  https://github.com/Haulmont/jmix-data/issues/115
//  https://github.com/Haulmont/jmix-gradle-plugin/issues/18
@JmixEntity
@Entity(name = "dummyAppSettingsEntity")
@SystemLevel
public class DummyAppSettingsEntity extends AppSettingsEntity {
}
