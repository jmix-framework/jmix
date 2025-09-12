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
import io.jmix.reports.entity.ReportGroup;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * Filtering, pagination and sorting options when loading groups from {@link ReportGroupRepository}.
 */
public final class ReportGroupLoadContext {
    public static final String LOCALIZED_TITLE_SORT_KEY = "localizedTitle";

    private final ReportGroupFilter filter;
    @Nullable
    private final Sort sort;
    private final int firstResult;
    private final int maxResults;

    /**
     * @param filter      filter values
     * @param sort        sort keys, must refer to property names of {@link ReportGroup} or custom keys listed above
     * @param firstResult number of items to skip if needed
     * @param maxResults  maximum items to load, if 0 then unlimited
     */
    public ReportGroupLoadContext(
            ReportGroupFilter filter,
            @Nullable Sort sort,
            int firstResult,
            int maxResults
    ) {
        this.filter = filter;
        this.sort = sort;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public ReportGroupFilter getFilter() {
        return filter;
    }

    @Nullable
    public Sort getSort() {
        return sort;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReportGroupLoadContext) obj;
        return Objects.equals(this.filter, that.filter) &&
               Objects.equals(this.sort, that.sort) &&
               this.firstResult == that.firstResult &&
               this.maxResults == that.maxResults;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter, sort, firstResult, maxResults);
    }

    @Override
    public String toString() {
        return "ReportGroupLoadContext[" +
               "filter=" + filter + ", " +
               "sort=" + sort + ", " +
               "firstResult=" + firstResult + ", " +
               "maxResults=" + maxResults + ']';
    }

}
