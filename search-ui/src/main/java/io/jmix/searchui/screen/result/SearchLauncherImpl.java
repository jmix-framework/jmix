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

package io.jmix.searchui.screen.result;

import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.search.SearchService;
import io.jmix.searchui.SearchLauncher;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.ScreenContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.Notifications.NotificationType.HUMANIZED;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@Component(SearchLauncher.NAME)
public class SearchLauncherImpl implements SearchLauncher {

    @Autowired
    protected Messages messages;
    @Autowired
    protected SearchService searchService;
    @Autowired
    protected ScreenBuilders screenBuilders;

    @Override
    public void search(FrameOwner origin, String searchTerm) {
        checkNotNullArgument(origin);

        ScreenContext screenContext = getScreenContext(origin);
        if (StringUtils.isBlank(searchTerm)) {
            Notifications notifications = screenContext.getNotifications();
            notifications.create(HUMANIZED)
                    .withCaption(messages.getMessage(SearchLauncherImpl.class, "noSearchTerm"))
                    .show();
        } else {
            String preparedSearchTerm = searchTerm.trim();

            SearchService.SearchResult searchResult = searchService.search(preparedSearchTerm);

            if (searchResult.isEmpty()) {
                Notifications notifications = screenContext.getNotifications();

                notifications.create(HUMANIZED)
                        .withCaption(messages.getMessage(SearchLauncherImpl.class, "noResults"))
                        .show();
            } else {
                screenBuilders.screen(origin)
                        .withScreenId(SearchResultsScreen.SCREEN_ID)
                        .withOpenMode(OpenMode.NEW_TAB)
                        .withOptions(
                                new MapScreenOptions(
                                        ParamsMap.of(
                                                "searchResult", searchResult
                                        )
                                )
                        )
                        .build()
                        .show();
            }
        }
    }
}
