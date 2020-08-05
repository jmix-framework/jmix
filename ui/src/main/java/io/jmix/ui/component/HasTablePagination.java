/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component;

import javax.annotation.Nullable;

/**
 * {@link ListComponent} component with a given interface supports the ability to show data by pages.
 */
public interface HasTablePagination {

    /**
     * @return pagination that is used in the {@link ListComponent}.
     */
    @Nullable
    TablePagination getPagination();

    /**
     * Sets pagination to the {@link ListComponent}.
     *
     * @param pagination pagination component
     */
    void setPagination(@Nullable TablePagination pagination);
}
