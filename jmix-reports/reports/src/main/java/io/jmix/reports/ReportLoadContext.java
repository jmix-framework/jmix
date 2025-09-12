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
import io.jmix.reports.entity.Report;
import org.springframework.lang.Nullable;

/**
 * Filtering, pagination and sorting options when loading reports from {@link ReportRepository}.
 */
public final class ReportLoadContext {

    /**
     * Custom sort keys.
     */
    public static final String GROUP_SORT_KEY = "group";
    public static final String LOCALIZED_NAME_SORT_KEY = "localizedName";

    private final ReportFilter filter;
    @Nullable
    private final Sort sort;
    private final int firstResult;
    private final int maxResults;

    /**
     * Create context to load report list.
     *
     * @param filter      filter values
     * @param sort        sort clauses, whose properties must refer to property names of {@link Report}
     *                      or be one of above-mentioned custom sort keys
     * @param firstResult number of items to skip if needed
     * @param maxResults  maximum items to load, if 0 then unlimited
     */
    public ReportLoadContext(ReportFilter filter, @Nullable Sort sort, int firstResult, int maxResults
    ) {
        this.filter = filter;
        this.sort = sort;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    /**
     * Create context to load report list.
     *
     * @param filter      filter values
     * @param sort        sort clauses, whose properties must refer to property names of {@link Report}
     *                      or be one of above-mentioned custom sort keys
     */
    public ReportLoadContext(ReportFilter filter, Sort sort) {
        this(filter, sort, 0, 0);
    }


    /**
     * Create context to load report list.
     * @param filter      filter values
     */
    public ReportLoadContext(ReportFilter filter) {
        this(filter, null, 0, 0);
    }

    @Override
    public String toString() {
        return "ReportLoadContext[" +
               "filter=" + filter + ", " +
               "sort=" + sort + ", " +
               "firstResult=" + firstResult + ", " +
               "maxResults=" + maxResults + ']';
    }

    public ReportFilter getFilter() {
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
}
