/*
 * Copyright (c) 2008-2020 Haulmont.
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

package com.haulmont.cuba.core.model;

import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.core.metamodel.annotation.ModelObject;

import javax.persistence.Transient;

@ModelObject(name = "test$SettersEntity", annotatedPropertiesOnly = true)
public class SettersEntity extends BaseUuidEntity {
    private static boolean staticFlag = true;

    @Transient
    private String stringField;

    public static void setStaticFlag(boolean staticFlag) {
        SettersEntity.staticFlag = staticFlag;
    }

    public static boolean getStaticFlag() {
        return staticFlag;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public void setStringField(Number stringField) {
        this.stringField = stringField.toString();
    }

    public String getStringField() {
        return stringField;
    }
}
