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

import org.springframework.lang.Nullable;

/**
 * Contains possible filtering conditions when loading groups from {@link ReportGroupRepository}.
 */
public class ReportGroupFilter {
    /**
     * filter by "localized title contains", case-insensitive
     */
    @Nullable
    protected String titleContains;

    /**
     * filter by "code contains", case-insensitive
     */
    @Nullable
    protected String codeContains;

    @Nullable
    public String getTitleContains() {
        return titleContains;
    }

    public void setTitleContains(@Nullable String titleContains) {
        this.titleContains = titleContains;
    }

    @Nullable
    public String getCodeContains() {
        return codeContains;
    }

    public void setCodeContains(@Nullable String codeContains) {
        this.codeContains = codeContains;
    }

    @Override
    public String toString() {
        return "ReportGroupFilter[" +
               "titleContains=" + titleContains + ", " +
               "codeContains=" + codeContains + ']';
    }

}
