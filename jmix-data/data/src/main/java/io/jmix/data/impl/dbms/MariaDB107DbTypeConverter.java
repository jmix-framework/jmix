/*
 * Copyright 2025 Haulmont.
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

package io.jmix.data.impl.dbms;

import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.UUID;

@Component("mariadb107DbTypeConverter")
public class MariaDB107DbTypeConverter extends MariaDBDbTypeConverter {

    @Override
    public Object getSqlObject(Object value) {
        if (value instanceof UUID)
            return value.toString();
        return super.getSqlObject(value);
    }

    @Override
    public int getSqlType(Class<?> javaClass) {
        if (javaClass == UUID.class)
            return Types.OTHER;
        return super.getSqlType(javaClass);
    }

    @Override
    public String getTypeAndVersion() {
        return "mariadb107";
    }
}
