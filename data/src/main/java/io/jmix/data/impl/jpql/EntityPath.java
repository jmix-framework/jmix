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

package io.jmix.data.impl.jpql;

import io.jmix.data.impl.jpql.pointer.EntityPointer;
import io.jmix.data.impl.jpql.pointer.Pointer;

import java.util.ArrayList;
import java.util.List;

public class EntityPath {
    public String topEntityVariableName;
    public String[] traversedFields;
    public String lastEntityFieldPattern;

    public Pointer resolvePointer(DomainModel model, QueryVariableContext queryVC) {
        Pointer pointer = EntityPointer.create(queryVC, topEntityVariableName);
        for (String traversedField : traversedFields) {
            pointer = pointer.next(model, traversedField);
        }
        return pointer;
    }

    public List<Pointer> resolveTransitionalPointers(DomainModel model, QueryVariableContext queryVC) {
        List<Pointer> pointers = new ArrayList<>();
        Pointer pointer = EntityPointer.create(queryVC, topEntityVariableName);
        pointers.add(pointer);
        for (String traversedField : traversedFields) {
            pointer = pointer.next(model, traversedField);
            pointers.add(pointer);
        }
        return pointers;
    }

    public static EntityPath parseEntityPath(String lastWord) {
        String[] parts = lastWord.split("\\.");
        EntityPath result = new EntityPath();
        if (parts.length > 0) {
            result.topEntityVariableName = parts[0];
            int consumedPartsCount = 1;
            if (lastWord.endsWith(".") || parts.length == 1) {
                result.lastEntityFieldPattern = "";
            } else {
                result.lastEntityFieldPattern = parts[parts.length - 1];
                consumedPartsCount = 2;
            }
            if (parts.length == 1) {
                result.traversedFields = new String[0];
            } else {
                result.traversedFields = new String[parts.length - consumedPartsCount];
                System.arraycopy(parts, 1, result.traversedFields, 0, parts.length - consumedPartsCount);
            }
        } else {
            result.traversedFields = new String[0];
        }
        return result;
    }
}
