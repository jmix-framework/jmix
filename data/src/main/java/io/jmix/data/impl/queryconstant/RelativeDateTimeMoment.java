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

package io.jmix.data.impl.queryconstant;

public enum RelativeDateTimeMoment {
    FIRST_DAY_OF_CURRENT_YEAR,
    LAST_DAY_OF_CURRENT_YEAR,
    FIRST_DAY_OF_CURRENT_MONTH,
    LAST_DAY_OF_CURRENT_MONTH,
    FIRST_DAY_OF_CURRENT_WEEK,
    LAST_DAY_OF_CURRENT_WEEK,
    START_OF_CURRENT_DAY,
    END_OF_CURRENT_DAY,
    START_OF_YESTERDAY,
    START_OF_TOMORROW,
    START_OF_CURRENT_HOUR,
    END_OF_CURRENT_HOUR,
    START_OF_CURRENT_MINUTE,
    END_OF_CURRENT_MINUTE
}
