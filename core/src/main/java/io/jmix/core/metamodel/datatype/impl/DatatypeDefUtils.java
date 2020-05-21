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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;

public class DatatypeDefUtils {

    public static boolean isDefaultForClass(Datatype datatype) {
        DatatypeDef annotation = getAnnotation(datatype);
        return annotation.defaultForClass();
    }

    public static String getId(Datatype datatype) {
        DatatypeDef annotation = getAnnotation(datatype);
        return annotation.id();
    }

    public static Class getJavaClass(Datatype datatype) {
        DatatypeDef annotation = getAnnotation(datatype);
        return annotation.javaClass();
    }

    private static DatatypeDef getAnnotation(Datatype datatype) {
        DatatypeDef annotation = datatype.getClass().getAnnotation(DatatypeDef.class);
        if (annotation == null)
            throw new IllegalStateException("Datatype " + datatype + " is not annotated with @DatatypeDef");
        return annotation;
    }
}
