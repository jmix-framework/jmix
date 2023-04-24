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

package io.jmix.flowui.data;

public interface BufferedDataUnit {

    /**
     * Writes all changes since the last time this method is invoked.
     */
    void write();

    /**
     * Discards all changes since the last time {@link #write} is invoked.
     */
    void discard();

    /**
     * @return {@code true} if this data unit in buffered mode, {@code false} otherwise.
     */
    boolean isBuffered();

    /**
     * @return {@code true} if this data unit stores changed data, {@code false} otherwise.
     */
    boolean isModified();
}
