/*
 * Copyright 2025 Haulmont.
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

package test_support.entity.dataaware;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import test_support.entity.TestBaseEntity;

@Entity(name = "test_EmptySelectionAllowed")
@JmixEntity
@Table(name = "TEST_EMPTY_SELECTION_ALLOWED")
public class TestEmptySelectionAllowedEntity extends TestBaseEntity {

    @NotNull
    @Column(name = "REQUIRED_ENUM_TYPE", nullable = false)
    private String requiredType;

    @Column(name = "NOT_REQUIRED_ENUM_TYPE")
    private String notRequiredType;

    public EnumType getRequiredType() {
        return requiredType == null ? null : EnumType.fromId(requiredType);
    }

    public void setRequiredType(EnumType type) {
        this.requiredType = type == null ? null : type.getId();
    }

    public EnumType getNotRequiredType() {
        return notRequiredType == null ? null : EnumType.fromId(notRequiredType);
    }

    public void setNotRequiredType(EnumType type) {
        this.notRequiredType = type == null ? null : type.getId();
    }
}
