/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.core.global;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that indicates that annotated element is secret. For example, if the Configuration interface property is
 * annotated with @Secret then a PasswordField will be used for this property in the UI.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.annotation.Secret}.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface Secret {
}
