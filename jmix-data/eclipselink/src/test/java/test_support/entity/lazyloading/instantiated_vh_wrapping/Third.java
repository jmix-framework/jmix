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

package test_support.entity.lazyloading.instantiated_vh_wrapping;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JmixEntity
@Table(name = "IVW_THIRD")
@Entity(name = "ivw_Third")
public class Third {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "SECOND_ID", nullable = false)
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Second second;

    @JoinColumn(name = "SECOND_COLLECTION_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Second secondForCollection;


    public final UUID getId() {
        return this.id;
    }

    public final void setId(UUID uuid) {
        this.id = uuid;
    }


    public final Second getSecond() {
        return this.second;
    }

    public final void setSecond(Second second) {
        this.second = second;
    }

    public Second getSecondForCollection() {
        return secondForCollection;
    }

    public void setSecondForCollection(Second secondForCollection) {
        this.secondForCollection = secondForCollection;
    }
}
