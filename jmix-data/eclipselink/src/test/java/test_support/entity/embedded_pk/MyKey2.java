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

package test_support.entity.embedded_pk;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@JmixEntity
@Embeddable
public class MyKey2 {
    @Column(name = "CODE1_2", nullable = false)
    @NotNull
    private String code12;

    @Column(name = "CODE2_2", nullable = false)
    @NotNull
    private Integer code22;

    public Integer getCode22() {
        return code22;
    }

    public void setCode22(Integer code22) {
        this.code22 = code22;
    }

    public String getCode12() {
        return code12;
    }

    public void setCode12(String code12) {
        this.code12 = code12;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code22, code12);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyKey2 entity = (MyKey2) o;
        return Objects.equals(this.code22, entity.code22) &&
                Objects.equals(this.code12, entity.code12);
    }
}