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

package test_support.entity.lazyloading.eager_mapped_by;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import test_support.entity.BaseEntity;

@Table(name = "TEST_EAGER_MAPPED_BY_VISIT")
@JmixEntity
@Entity(name = "test_EagerMappedByVisit")
public class EagerMappedByVisit extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PASSPORT_ID")
    protected EagerMappedByPassport passport;

    public EagerMappedByPassport getPassport() {
        return passport;
    }

    public void setPassport(EagerMappedByPassport passport) {
        this.passport = passport;
    }
}
