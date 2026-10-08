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

package io.jmix.security.impl.role.provider;

import io.jmix.security.model.BaseRole;
import io.jmix.security.role.DuplicateRoleCodeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Builds a code-keyed map of annotated roles, failing with a {@link DuplicateRoleCodeException}
 * if two annotated classes declare the same role code.
 */
public final class AnnotatedRoleCodeMapBuilder {

    private AnnotatedRoleCodeMapBuilder() {
    }

    public static <T extends BaseRole> Map<String, T> build(Set<String> classNames,
                                                            Function<String, T> roleFactory,
                                                            String roleType) {
        Map<String, T> result = new HashMap<>();
        Map<String, String> codeToClassName = new HashMap<>();
        for (String className : classNames) {
            T role = roleFactory.apply(className);
            String existingClassName = codeToClassName.putIfAbsent(role.getCode(), className);
            if (existingClassName != null) {
                throw new DuplicateRoleCodeException(
                        String.format("Duplicate %s role code '%s' in annotated classes '%s' and '%s'",
                                roleType, role.getCode(), existingClassName, className),
                        role.getCode());
            }
            result.put(role.getCode(), role);
        }
        return result;
    }
}
