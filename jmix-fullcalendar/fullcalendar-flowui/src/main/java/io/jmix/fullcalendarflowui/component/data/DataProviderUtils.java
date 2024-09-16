/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.component.data;

import io.jmix.core.annotation.Internal;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * INTERNAL.
 * <p>
 * Utility class for data providers.
 */
@Internal
public final class DataProviderUtils {

    private DataProviderUtils() {
    }

    /**
     * Generates an ID for data providers
     *
     * @return a string ID
     */
    public static String generateId() {
        return RandomStringUtils.randomAlphabetic(6);
    }
}
