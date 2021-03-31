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

package io.jmix.search.searching.impl;

public class SearchDetails {

    protected int size;
    protected int offset;

    public int getSize() {
        return size;
    }

    public SearchDetails setSize(int size) {
        this.size = size;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public SearchDetails setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public String toString() {
        return "SearchDetails{" +
                "size=" + size +
                ", offset=" + offset +
                '}';
    }
}
