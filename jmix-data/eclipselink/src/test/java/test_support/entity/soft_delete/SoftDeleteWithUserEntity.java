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

package test_support.entity.soft_delete;


import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "TEST_SOFTDELETE_WITH_USER_ENTITY")
@JmixEntity
@Entity(name = "test_SoftDeleteWithUserEntity")
public class SoftDeleteWithUserEntity extends SoftDeleteEntity {
    private static final long serialVersionUID = 3968966872668194191L;

    @DeletedBy
    @Column(name = "WHO_DELETED", length = 50)
    protected String whoDeleted;

    public String getWhoDeleted() {
        return whoDeleted;
    }

    public void setWhoDeleted(String whoDeleted) {
        this.whoDeleted = whoDeleted;
    }
}
