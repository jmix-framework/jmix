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

package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@JmixEntity(name = "test_EmbeddableEntity")
@Embeddable
public class TestEmbeddableEntity {

    @Column(name = "TEXT_VALUE")
    private String textValue;

    @Column(name = "ENUM_VALUE")
    private Integer enumValue;

    @Column(name = "INT_VALUE")
    private Integer intValue;

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public TestEnum getEnumValue() {
        return enumValue == null ? null : TestEnum.fromId(enumValue);
    }

    public void setEnumValue(TestEnum enumValue) {
        this.enumValue = enumValue == null ? null : enumValue.getId();
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }
}
