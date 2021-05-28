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

package io.jmix.ui.app.propertyfilter.dateinterval;

import io.jmix.core.annotation.Internal;

import java.util.List;

/**
 * Provides enum values for relative date and time moments.
 */
@Internal
public interface RelativeDateTimeMomentProvider {

    /**
     * @return all enum values
     */
    List<Enum> getRelativeDateTimeMoments();

    /**
     * @return enum values that correspond to time types
     */
    List<Enum> getRelativeTimeMoments();

    /**
     * @param name name of enum constant
     * @return enum value
     */
    Enum getByName(String name);
}
