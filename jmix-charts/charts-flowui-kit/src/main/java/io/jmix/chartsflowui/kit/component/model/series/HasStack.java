/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

/**
 * A series that has stacked values. If stack the value. On the same category axis, the series with
 * the same stack name would be put on top of each other.<br/><br/>
 * <b>Notice</b>: {@code stack} only supports stacking on {@code value} and {@code log} axis for now. {@code time} and
 * {@code category} axis are not supported.
 *
 * @param <T> origin class type
 */
public interface HasStack<T> {

    /**
     * @return stack name for the series
     */
    String getStack();

    /**
     * Sets the stack name for the series or replaces an existing one.
     *
     * @param stack stack name to set
     */
    void setStack(String stack);

    /**
     * @param stack stack name to set
     * @return this
     * @see HasStack#setStack(String)
     */
    @SuppressWarnings("unchecked")
    default T withStack(String stack) {
        setStack(stack);
        return (T) this;
    }

    /**
     * @return type of strategy for stacking values
     */
    StackStrategy getStackStrategy();

    /**
     * Sets the stack strategy type or replaces an existing one for stack values if the stack name was specified.
     *
     * @param stackStrategy stack strategy type to set
     */
    void setStackStrategy(StackStrategy stackStrategy);

    /**
     * @param stackStrategy stack strategy type to set
     * @return this
     * @see HasStack#setStackStrategy(StackStrategy)
     */
    @SuppressWarnings("unchecked")
    default T withStackStrategy(StackStrategy stackStrategy) {
        setStackStrategy(stackStrategy);
        return (T) this;
    }

    /**
     * The stack strategy type. More detailed information is provided in the documentation.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-line.stackStrategy">StackStrategy documentation</a>
     */
    enum StackStrategy implements HasEnumId {
        SAME_SIGN("samesign"),
        ALL("all"),
        POSITIVE("positive"),
        NEGATIVE("negative");

        private final String id;

        StackStrategy(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static StackStrategy fromId(String id) {
            for (StackStrategy at : StackStrategy.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }
}
