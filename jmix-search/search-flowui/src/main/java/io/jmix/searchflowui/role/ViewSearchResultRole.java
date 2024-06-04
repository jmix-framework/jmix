/*
 * Copyright 2022 Haulmont.
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

package io.jmix.searchflowui.role;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Grants permissions to view Search Results screen
 */
@ResourceRole(name = "Search: view search results", code = ViewSearchResultRole.CODE, scope = SecurityScope.UI)
public interface ViewSearchResultRole {
    String CODE = "search-view-search-results";

    @ViewPolicy(viewIds = "search_SearchResultsView")
    void viewSearchResults();
}
