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

package io.jmix.appsettings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.appsettings")
public class AppSettingsProperties {
    /**
     * Whether to check security permissions for settings entities.
     */
    final Boolean checkPermissionsForAppSettingsEntity;

    public AppSettingsProperties(@DefaultValue("false") Boolean checkPermissionsForAppSettingsEntity) {
        this.checkPermissionsForAppSettingsEntity = checkPermissionsForAppSettingsEntity;
    }

    /**
     * @see #checkPermissionsForAppSettingsEntity
     */
    public Boolean isCheckPermissionsForAppSettingsEntity() {
        return checkPermissionsForAppSettingsEntity;
    }
}