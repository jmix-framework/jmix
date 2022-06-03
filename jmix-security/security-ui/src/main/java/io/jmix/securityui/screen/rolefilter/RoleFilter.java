/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityui.screen.rolefilter;

import io.jmix.security.model.BaseRole;
import org.apache.commons.lang3.StringUtils;

public class RoleFilter {

    private String name;
    private String code;
    private String source;

    public RoleFilter(String name, String code, String source) {
        this.name = name;
        this.code = code;
        this.source = source;
    }

    public RoleFilter() {
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSource() {
        return source;
    }

    public boolean matches(BaseRole role) {
        return (name == null || StringUtils.containsIgnoreCase(role.getName(), name))
                && (code == null || StringUtils.containsIgnoreCase(role.getCode(), code))
                && (source == null || StringUtils.containsIgnoreCase(role.getSource(), source));
    }
}
