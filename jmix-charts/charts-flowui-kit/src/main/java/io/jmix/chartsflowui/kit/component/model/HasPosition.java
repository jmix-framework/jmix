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

package io.jmix.chartsflowui.kit.component.model;

public interface HasPosition<T> {

    String getLeft();

    void setLeft(String left);

    @SuppressWarnings("unchecked")
    default T withLeft(String left) {
        setLeft(left);
        return (T) this;
    }

    String getTop();

    void setTop(String top);

    @SuppressWarnings("unchecked")
    default T withTop(String top) {
        setTop(top);
        return (T) this;
    }

    String getRight();

    void setRight(String right);

    @SuppressWarnings("unchecked")
    default T withRight(String right) {
        setRight(right);
        return (T) this;
    }

    String getBottom();

    void setBottom(String bottom);

    @SuppressWarnings("unchecked")
    default T withBottom(String bottom) {
        setBottom(bottom);
        return (T) this;
    }
}
