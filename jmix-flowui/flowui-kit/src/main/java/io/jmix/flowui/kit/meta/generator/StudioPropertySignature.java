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

package io.jmix.flowui.kit.meta.generator;

import org.jspecify.annotations.Nullable;

import java.util.List;

record StudioPropertySignature(
        String xmlAttribute,
        String type,
        String classFqn,
        @Nullable String category,
        boolean required,
        String defaultValue,
        String defaultValueRef,
        String initialValue,
        List<String> options,
        String setMethod,
        String setParameterFqn,
        String addMethod,
        String addParameterFqn,
        String removeMethod,
        String removeParameterFqn,
        String typeParameter,
        boolean useAsInjectionType,
        List<String> componentRefTags,
        String cdataWrapperTag
) {
    static StudioPropertySignature of(String xmlAttribute,
                                      String type,
                                      @Nullable String category,
                                      boolean required,
                                      String defaultValue,
                                      List<String> options) {
        return new StudioPropertySignature(
                xmlAttribute,
                type,
                "",
                category,
                required,
                defaultValue,
                "",
                "",
                List.copyOf(options),
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                false,
                List.of(),
                ""
        );
    }
}
