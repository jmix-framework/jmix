/*
 * Copyright 2021 Haulmont.
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

package test_support.entity.indexing;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@JmixEntity
@Embeddable
public class TestCompositeKey {
    @Column(name = "PK_NAME")
    private String pkName;

    @Column(name = "PK_VERSION")
    private Long pkVersion;

    @Override
    public int hashCode() {
        return Objects.hash(pkName, pkVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCompositeKey entity = (TestCompositeKey) o;
        return Objects.equals(this.pkName, entity.pkName) &&
                Objects.equals(this.pkVersion, entity.pkVersion);
    }

    public Long getPkVersion() {
        return pkVersion;
    }

    public void setPkVersion(Long pkVersion) {
        this.pkVersion = pkVersion;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }
}