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

package io.jmix.securityflowui.component.rolefilter;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.RoleSourceType;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

public class RoleFilterChangeEvent extends ComponentEvent<RoleFilter> {

    private String name;
    private String code;
    private RoleSourceType source;

    public RoleFilterChangeEvent(RoleFilter filter) {
        this(filter, null, null, null);
    }

    public RoleFilterChangeEvent(RoleFilter filter,
                                 @Nullable String name, @Nullable String code, @Nullable RoleSourceType source) {
        super(filter, true);

        this.name = name;
        this.code = code;
        this.source = source;
    }

    @Nullable
    public String getNameValue() {
        return name;
    }

    @Nullable
    public String getCodeValue() {
        return code;
    }

    @Nullable
    public RoleSourceType getSourceValue() {
        return source;
    }

    @Nullable
    protected String getSourceValueAsString() {
        return source != null ? source.getId() : null;
    }

    public boolean matches(BaseRole role) {
        return (name == null || StringUtils.containsIgnoreCase(role.getName(), name))
                && (code == null || StringUtils.containsIgnoreCase(role.getCode(), code))
                && (source == null || StringUtils.containsIgnoreCase(role.getSource(), getSourceValueAsString()));
    }
}
