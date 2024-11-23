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
package test_support.entity.importexport;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

@JmixEntity
@Entity(name = "testimportexport_Permission")
@Table(name = "TESTIMPORTEXPORT_PERMISSION")
@SystemLevel
public class Permission extends StandardEntity {

    public static final String TARGET_PATH_DELIMETER = ":";

    @Column(name = "PERMISSION_TYPE")
    private Integer type;

    @Column(name = "TARGET", length = 100)
    private String target;

    /**
     * May be 0,1,2 for {@link PermissionType#ENTITY_ATTR} and 0,1 for others
     */
    @Column(name = "VALUE_")
    private Integer value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public PermissionType getType() {
        return type == null ? null : PermissionType.fromId(type);
    }

    public void setType(PermissionType type) {
        this.type = type == null ? null : type.getId();
    }

    /**
     * See {@link #value}
     */
    public Integer getValue() {
        return value;
    }

    /**
     * See {@link #value}
     */
    public void setValue(Integer value) {
        this.value = value;
    }
}