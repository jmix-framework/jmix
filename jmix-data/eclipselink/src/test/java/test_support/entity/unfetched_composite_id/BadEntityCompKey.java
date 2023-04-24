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

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@JmixEntity
@Embeddable
public class BadEntityCompKey implements Serializable {

    public BadEntityCompKey() {
    }

    public BadEntityCompKey(String bilds, String uname) {
        this.bilds = bilds;
        this.uname = uname;
    }

    @Column(name = "BILDS", nullable = false, unique = true, length = 2)
    private String bilds;

    @Column(name = "UNAME", nullable = false, unique = true, length = 12)
    private String uname;

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getBilds() {
        return bilds;
    }

    public void setBilds(String bilds) {
        this.bilds = bilds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bilds, uname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BadEntityCompKey entity = (BadEntityCompKey) o;
        return Objects.equals(this.bilds, entity.bilds) &&
                Objects.equals(this.uname, entity.uname);
    }

    @InstanceName
    @DependsOnProperties({"bilds", "uname"})
    public String getInstanceName() {
        return String.format("%s %s", bilds, uname);
    }
}