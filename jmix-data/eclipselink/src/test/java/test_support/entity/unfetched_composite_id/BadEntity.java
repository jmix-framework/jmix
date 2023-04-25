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

package test_support.entity.unfetched_composite_id;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;

@JmixEntity
@Table(name = "UBADENTITY")
@Entity
public class BadEntity {
    @InstanceName
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "uname", column = @Column(name = "UNAME")),
            @AttributeOverride(name = "bilds", column = @Column(name = "BILDS"))
    })
    private BadEntityCompKey id;

    @Column(name = "BVAL")
    private Boolean bval;

    public Boolean getBval() {
        return bval;
    }

    public void setBval(Boolean aausw) {
        this.bval = aausw;
    }

    public BadEntityCompKey getId() {
        return id;
    }

    public void setId(BadEntityCompKey id) {
        this.id = id;
    }

    //Bad practice. Use @InstanceName method instead (like in BadEntityCompKey).
    @Override
    public String toString() {
        return "Usrm1 [" + id.getUname() + "," + id.getBilds() + "]:" + bval;
    }
}