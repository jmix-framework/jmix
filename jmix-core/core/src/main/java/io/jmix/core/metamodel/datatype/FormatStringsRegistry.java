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

package io.jmix.core.metamodel.datatype;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Registry for {@link FormatStrings}
 */
public interface FormatStringsRegistry {

    /**
     * Get format strings for a locale. Returns null if not registered.
     */
    @Nullable
    FormatStrings getFormatStringsOrNull(Locale locale);

    /**
     * Get format strings for a locale.
     * @throws IllegalArgumentException if not registered
     */
    FormatStrings getFormatStrings(Locale locale);

    /**
     * Register format strings for a locale.
     */
    void setFormatStrings(Locale locale, FormatStrings formatStrings);
}
