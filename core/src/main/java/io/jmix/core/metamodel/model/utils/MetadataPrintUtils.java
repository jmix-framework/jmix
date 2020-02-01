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
package io.jmix.core.metamodel.model.utils;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Session;

import java.util.Collection;
import java.util.LinkedList;

public final class MetadataPrintUtils {

    private MetadataPrintUtils() {
    }

    public static String printClassHierarchy(Session model) {
        StringBuilder builder = new StringBuilder();

        Collection<MetaClass> topLevelClasses = new LinkedList<>();
        for (MetaClass metaClass : model.getClasses()) {
            if (metaClass.getAncestor() == null) {
                topLevelClasses.add(metaClass);
            }
        }

        for (MetaClass topLevelClass : topLevelClasses) {
            builder.append(printClassHierarchy(topLevelClass));
        }

        return builder.toString();
    }

    public static String printClassHierarchy(MetaClass metaClass) {
        StringBuilder builder = new StringBuilder();

        builder.append(metaClass.getName()).append("\n");
        for (MetaClass descendantClass : metaClass.getDescendants()) {
            builder.append(shift(printClassHierarchy(descendantClass)));
        }

        return builder.toString();
    }

    public static String printClass(MetaClass metaClass) {
        StringBuilder builder = new StringBuilder();

        builder.append(metaClass.getName()).append("\n");
        for (MetaProperty metaProperty : metaClass.getOwnProperties()) {
            builder.append(shift(metaProperty.getName() + ": " + metaProperty.getRange()));
        }

        return builder.toString();
    }

    private static String shift(String string) {
        StringBuilder builder = new StringBuilder();

        final String[] strings = string.split("\n");
        for (String s : strings) {
            builder.append("    ").append(s).append("\n");
        }

        return builder.toString();
    }
}
