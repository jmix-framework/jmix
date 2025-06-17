/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports;

import io.jmix.core.Sort;
import org.springframework.lang.Nullable;

/**
 * Filtering, pagination and sorting options when loading groups from {@link ReportGroupRepository}.
 *
 * @param filter filter values
 * @param sort sort properties, must refer to property names of {@link io.jmix.reports.entity.ReportGroupInfo}
 * @param firstResult number of items to skip if needed
 * @param maxResults maximum items to load, if 0 then unlimited
 */
public record ReportGroupLoadContext(
        ReportGroupFilter filter,
        @Nullable Sort sort,
        int firstResult,
        int maxResults
) {
}
