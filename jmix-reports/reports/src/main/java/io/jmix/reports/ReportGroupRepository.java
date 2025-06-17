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

import io.jmix.core.NoResultException;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportGroupInfo;

import java.util.List;

/**
 * Provides unified access to both types of report groups: stored in database and defined in code.
 * <br/>
 * Application should generally should use this interface to work with groups, instead of DataManager.
 */
public interface ReportGroupRepository {

    /**
     * Load all known report groups.
     * @return list of groups
     */
    List<ReportGroupInfo> loadAll();

    /**
     * Load list of known groups, with filtering, pagination and sorting options available.
     * @param loadContext context containing filtering, pagination and sorting options
     * @return list of groups
     */
    List<ReportGroupInfo> loadList(ReportGroupLoadContext loadContext);

    /**
     * Calculate total count of entities that conform to passed filter.
     * @param filter object with filter values
     * @return count of suitable entities
     */
    int getTotalCount(ReportGroupFilter filter);

    /**
     * Convert model object into short info object.
     * @param group group entity (annotated or from database)
     * @return short group info
     */
    ReportGroupInfo convertToInfo(ReportGroup group);

    /**
     * Load full ReportGroup model object by given short info.
     *
     * @param reportGroupInfo short group info
     * @return full model object, either from database or from annotated group storage
     * @throws NoResultException if nothing was loaded
     */
    ReportGroup loadModelObject(ReportGroupInfo reportGroupInfo);

    /**
     * Remove group from database.
     * Applicable only to database-originated groups.
     * @param info group to remove
     */
    void remove(ReportGroupInfo info);
}
