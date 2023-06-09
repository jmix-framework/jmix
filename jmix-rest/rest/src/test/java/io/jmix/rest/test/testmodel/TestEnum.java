/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.test.testmodel;


import io.jmix.core.metamodel.datatype.EnumClass;

/**
 */
public enum TestEnum implements EnumClass<String> {

    ENUM_VALUE_1("1"),
    ENUM_VALUE_2("1");

    private String id;

    TestEnum(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static TestEnum fromId(String id) {
        if (id == null) return null;
        switch (id) {
            case "1":
                return ENUM_VALUE_1;
            case "2":
                return ENUM_VALUE_2;
            default:
                return null;
        }
    }
}
