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

package io.jmix.chartsflowui.kit.component.model.shared;

/**
 * Common space around content.
 */
public class Padding {

    protected Integer top;

    protected Integer right;

    protected Integer bottom;

    protected Integer left;

    public Padding(Integer padding) {
        top = padding;
        right = padding;
        bottom = padding;
        left = padding;
    }

    public Padding(Integer vertical, Integer horizontal) {
        top = vertical;
        bottom = vertical;
        right = horizontal;
        left = horizontal;
    }

    public Padding(Integer top, Integer right, Integer bottom, Integer left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public Integer getTop() {
        return top;
    }

    public Integer getRight() {
        return right;
    }

    public Integer getBottom() {
        return bottom;
    }

    public Integer getLeft() {
        return left;
    }
}
