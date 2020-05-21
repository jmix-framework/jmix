/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.metamodel.model.impl;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Range;

public class EnumerationRange extends AbstractRange implements Range {

    private Enumeration enumeration;

    public EnumerationRange(Enumeration enumeration) {
        super();
        this.enumeration = enumeration;
    }

    @Override
    public MetaClass asClass() {
        throw new IllegalStateException("Range is enumeration");
    }

    @Override
    public <T> Datatype<T> asDatatype() {
        throw new IllegalStateException("Range is enumeration");
    }

    @Override
    public Enumeration asEnumeration() {
        return enumeration;
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean isDatatype() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    @Override
    public String toString() {
        return "Range{enum=" + enumeration + "}";
    }
}