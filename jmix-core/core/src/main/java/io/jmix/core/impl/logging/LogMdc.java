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

package io.jmix.core.impl.logging;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;

import jakarta.annotation.Nullable;

public class LogMdc {

    public static final String USER = "jmixUser";

    public static void setup(@Nullable Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            MDC.put(USER, username);
        } else {
            MDC.remove(USER);
        }
    }
}