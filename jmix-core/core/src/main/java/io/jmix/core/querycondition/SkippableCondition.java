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

package io.jmix.core.querycondition;

/**
 * Abstract superclass for conditions which have parameters and may be skipped in case of parameter is absent, null
 * or empty.
 * @param <T> class of inheritor.
 */
public abstract class SkippableCondition<T extends SkippableCondition<T>> implements Condition {

    /**
     * Whether to skip this condition if one or more parameters are absent, null or empty
     */
    protected Boolean skipNullOrEmpty;

    /**
     * @param skipNullOrEmpty whether to skip this condition if parameter is absent, null or empty
     */
    public void setSkipNullOrEmpty(Boolean skipNullOrEmpty) {
        this.skipNullOrEmpty = skipNullOrEmpty;
    }

    /**
     * @return whether to skip this condition if parameter is absent, null or empty
     */
    public boolean isSkipNullOrEmpty() {
        return skipNullOrEmpty;
    }

    /**
     * Make this condition to be skipped if parameter is absent, null or empty.
     * Allows to skip filtering if no value specified in UI component.
     */
    public T skipNullOrEmpty() {
        skipNullOrEmpty = true;
        //noinspection unchecked
        return (T) this;
    }


    protected void applyDefaultSkipNullOrEmpty(boolean defaultSkipNullOrEmpty) {
        if (skipNullOrEmpty == null) {
            skipNullOrEmpty = defaultSkipNullOrEmpty;
        }
    }
}
