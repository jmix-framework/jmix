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

import io.jmix.reports.entity.ReportGroup;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides unified access to both types of report groups: stored in database and defined in code.
 * Loading and modification operations apply registered security constraints.
 * <br/>
 * Application should generally should use this interface to work with groups, instead of DataManager.
 */
public interface ReportGroupRepository {

    /**
     * Load all report groups, sorted by localized title.
     * @return list of groups
     */
    List<ReportGroup> loadAll();

    /**
     * Load list of report groups, with filtering, pagination and sorting options available.
     * @param loadContext context containing filtering, pagination and sorting options
     * @return list of groups
     */
    List<ReportGroup> loadList(ReportGroupLoadContext loadContext);

    /**
     * Calculate total count of entities that conform to passed filter.
     * @param filter object with filter values
     * @return count of suitable entities
     */
    int getTotalCount(ReportGroupFilter filter);

    /**
     * Checks if there are any report groups with the given code.
     *
     * @param code report group code
     * @return true if a report group with the given code exists, false otherwise
     */
    boolean existsGroupByCode(String code);

    /**
     * Loads report group id by its code.
     *
     * @param code report group code
     * @return {@link Optional} id for report group.
     */
    Optional<UUID> loadGroupIdByCode(String code);
}
