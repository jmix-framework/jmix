/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.validation.time;

/**
 * Base interface for date/time validators which contains all restrictions.
 */
public interface TimeValidator {

    /**
     * @return true if date or time in the past
     */
    boolean isPast();

    /**
     * @return true if date or time in the past or present
     */
    boolean isPastOrPresent();

    /**
     * @return true if date or time in the future
     */
    boolean isFuture();

    /**
     * @return true if date or time in the future or present
     */
    boolean isFutureOrPresent();

    /**
     * Sets check seconds to validator. Set true if validator should check seconds and nanos while it comparing dates
     * or times.
     *
     * @param checkSeconds check seconds and nanos option, false by default
     */
    void setCheckSeconds(boolean checkSeconds);
}
