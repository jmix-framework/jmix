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

import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

@Table(name = "TEST_SOFTDELETE_ENTITY")
@JmixEntity
@Entity(name = "test_SoftDeleteEntity")
public class SoftDeleteEntity extends HardDeleteEntity {
    private static final long serialVersionUID = 7016314126468585951L;

    @DeletedDate
    @Column(name = "TIME_OF_DELETION")
    protected Date timeOfDeletion;


    public Date getTimeOfDeletion() {
        return timeOfDeletion;
    }

    public void setTimeOfDeletion(Date timeOfDeletion) {
        this.timeOfDeletion = timeOfDeletion;
    }


}
