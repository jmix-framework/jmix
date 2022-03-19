/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

public class TimestampClass {
    protected Class clazz;
    protected Date timestamp;
    protected Collection<String> dependencies = new HashSet<>();
    protected Collection<String> dependent= new HashSet<>();

    public TimestampClass(Class clazz, Date timestamp) {
        this.clazz = clazz;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimestampClass that = (TimestampClass) o;

        if (!Objects.equals(clazz, that.clazz)) return false;
        if (!Objects.equals(timestamp, that.timestamp)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
