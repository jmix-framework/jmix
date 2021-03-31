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

import com.google.common.collect.EvictingQueue;
import io.jmix.core.*;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchApplicationProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.impl.SearchDetails;
import io.jmix.search.searching.impl.FieldHit;
import io.jmix.search.searching.impl.SearchResultEntry;
import io.jmix.search.utils.PropertyTools;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.jmix.ui.Notifications.NotificationType.HUMANIZED;
import static io.jmix.ui.component.Component.Alignment.MIDDLE_LEFT;

@UiController(SearchResultsScreen.SCREEN_ID)
@UiDescriptor("search-results.xml")
public class SearchResultsScreen extends Screen {

    public static final String SCREEN_ID = "searchResultsScreen";

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected SearchApplicationProperties searchApplicationProperties;
    @Autowired
    protected PropertyTools propertyTools;
    @Autowired
    protected EntitySearcher entitySearcher;
    @Autowired
    protected Notifications notifications;

    @Autowired
    protected ScrollBoxLayout contentBox;
    @Autowired
    protected HBoxLayout navigationBox;


    protected SearchResultsScreen.Page currentPage;
    protected Queue<SearchResultsScreen.Page> pages;

    @Subscribe
    public void onAfterInit(AfterInitEvent event) {
        Map<String, Object> params = new HashMap<>(0);
        ScreenOptions options = event.getOptions();
        if (options instanceof MapScreenOptions) {
            params = ((MapScreenOptions) options).getParams();
        }

        SearchResult searchResult = (SearchResult) params.get("searchResult");
        if (searchResult == null) {
            throw new RuntimeException("Search result not found");
        }

        initScreenCaption(searchResult);
        handleSearchResult(searchResult);
    }

    protected static class Page {
        protected int pageNumber;
        protected boolean lastPage;
        protected SearchResult searchResult;

        public Page(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public void setSearchResult(SearchResult searchResult) {
            this.searchResult = searchResult;
        }

        public SearchResult getSearchResult() {
            return searchResult;
        }

        public boolean isLastPage() {
            return lastPage;
        }

        public void setLastPage(boolean lastPage) {
            this.lastPage = lastPage;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public String getDisplayedPageNumber() {
            return String.valueOf(pageNumber + 1);
        }
    }


    protected void initScreenCaption(SearchResult searchResult) {
        String caption = messages.getMessage(SearchResultsScreen.class, "resultScreenCaption");
        String searchTerm = searchResult.getSearchTerm();
        if (StringUtils.isNotBlank(searchTerm)) {
            caption = caption + ": " + searchTerm;
        }
        getWindow().setCaption(caption);
    }

    protected void handleSearchResult(SearchResult searchResult) {
        String searchTerm = searchResult.getSearchTerm();
        if (StringUtils.isBlank(searchTerm)) {
            handleNoSearchTerm();
            return;
        }

        //noinspection UnstableApiUsage
        pages = EvictingQueue.create(searchApplicationProperties.getMaxSearchPageCount());
        currentPage = new SearchResultsScreen.Page(0);
        currentPage.setSearchResult(searchResult);
        pages.add(currentPage);

        renderResult(currentPage);
        renderNavigationControls(pages);
    }

    protected void handleNoSearchTerm() {
        Label<String> label = createNoSearchTermLabel();
        contentBox.add(label);
    }

    protected void renderResult(SearchResultsScreen.Page page) {
        contentBox.removeAll();
        SearchResult searchResult = page.getSearchResult();
        if (searchResult.isEmpty()) {
            contentBox.add(createNotFoundLabel());
        } else {
            List<Pair<String, String>> entityGroups = new ArrayList<>();
            for (String entityName : searchResult.getEntityNames()) {
                entityGroups.add(new Pair<>(
                        entityName,
                        messageTools.getEntityCaption(metadata.getClass(entityName))
                ));
            }
            entityGroups.sort(Comparator.comparing(Pair::getSecond));

            for (Pair<String, String> entityPair : entityGroups) {
                String entityName = entityPair.getFirst();
                String entityCaption = entityPair.getSecond();

                CssLayout container = createCssLayout();
                container.setStyleName("c-fts-entities-container");
                container.setWidth("100%");

                CssLayout entityLabelLayout = createCssLayout();
                entityLabelLayout.setStyleName("c-fts-entities-type");
                entityLabelLayout.add(createEntityLabel(entityCaption));

                container.add(entityLabelLayout);

                CssLayout instancesLayout = createCssLayout();
                instancesLayout.setWidth("100%");
                displayInstances(searchResult, entityName, instancesLayout);
                container.add(instancesLayout);

                contentBox.add(container);
            }
        }
    }

    protected void renderNavigationControls(Queue<Page> pages) {
        navigationBox.removeAll();
        for (Page page : pages) {
            LinkButton pageButton = uiComponents.create(LinkButton.class);
            BaseAction action = new BaseAction("page_" + page.getPageNumber())
                    .withCaption(page.getDisplayedPageNumber())
                    .withHandler(e -> openPage(page));
            pageButton.setAction(action);
            if (page == currentPage) {
                pageButton.setStyleName("c-fts-current-page");
            } else {
                pageButton.setStyleName("c-fts-page");
            }
            navigationBox.add(pageButton);
        }

        boolean showNextPage = true;
        Page lastPage = getLastPage();
        if (lastPage != null && lastPage.getSearchResult() != null) {
            SearchResult lastSearchResult = lastPage.getSearchResult();
            showNextPage = lastSearchResult.isMoreDataAvailable();
        }

        if (showNextPage) {
            LinkButton nextPageButton = uiComponents.create(LinkButton.class);
            BaseAction action = new BaseAction("nextPage")
                    .withCaption(messages.getMessage(SearchResultsScreen.class, "nextPage"))
                    .withHandler(e -> openNextPage());
            nextPageButton.setAction(action);
            nextPageButton.setStyleName("c-fts-page");
            navigationBox.add(nextPageButton);
        }
    }

    protected void openPage(Page page) {
        currentPage = page;
        renderResult(page);
        renderNavigationControls(pages);
    }

    protected void openNextPage() {
        Page lastPage = getLastPage();
        if (lastPage != null) {
            SearchResult lastSearchResult = lastPage.getSearchResult();
            SearchResult searchResult = entitySearcher.search(
                    lastSearchResult.getSearchTerm(),
                    createNextSearchDetails(lastSearchResult)
            );
            if (searchResult.getSize() == 0) {
                currentPage.setLastPage(true);
                renderNavigationControls(pages);
                notifications.create(HUMANIZED)
                        .withCaption(messages.getMessage("io.jmix.searchui.noResults"))
                        .show();
            } else {
                currentPage = new Page(lastPage.getPageNumber() + 1);
                currentPage.setSearchResult(searchResult);
                pages.add(currentPage);
                renderResult(currentPage);
                renderNavigationControls(pages);
            }
        }
    }

    protected SearchDetails createNextSearchDetails(SearchResult searchResult) {
        SearchDetails currentSearchDetails = searchResult.getSearchDetails();
        return new SearchDetails()
                .setSize(currentSearchDetails.getSize())
                .setOffset(searchResult.getEffectiveOffset());
    }

    protected Page getLastPage() {
        Page lastPage = null;
        for (Page page : pages) {
            if (lastPage == null) {
                lastPage = page;
            } else {
                if (page.getPageNumber() > lastPage.getPageNumber()) {
                    lastPage = page;
                }
            }
        }
        return lastPage;
    }

    protected CssLayout createCssLayout() {
        return uiComponents.create(CssLayout.class);
    }

    protected Label<String> createNoSearchTermLabel() {
        Label<String> label = uiComponents.create(Label.of(String.class));
        label.setValue(messages.getMessage("io.jmix.searchui.noSearchTerm"));
        label.setStyleName("h2");
        return label;
    }

    protected Label<String> createNotFoundLabel() {
        Label<String> label = uiComponents.create(Label.of(String.class));
        label.setValue(messages.getMessage("io.jmix.searchui.noResults"));
        label.setStyleName("h2");
        return label;
    }

    protected Label<String> createEntityLabel(String caption) {
        Label<String> entityLabel = uiComponents.create(Label.of(String.class));
        entityLabel.setValue(caption);
        entityLabel.setStyleName("h2");
        entityLabel.setWidth("200px");
        return entityLabel;
    }

    protected void displayInstances(SearchResult searchResult, String entityName, CssLayout instancesLayout) {
        Set<SearchResultEntry> entries = searchResult.getEntriesByEntityName(entityName);

        for (SearchResultEntry entry : entries) {
            Button instanceBtn = createInstanceButton(entityName, entry);
            instancesLayout.add(instanceBtn);

            List<String> list = new ArrayList<>(entry.getFieldHits().size());
            for (FieldHit fieldHit : entry.getFieldHits()) {
                list.add(fieldHit.getFieldName() + " : " + fieldHit.getHighlights());
            }
            Collections.sort(list);

            for (String caption : list) {
                Label<String> hitLabel = createHitLabel(caption);
                instancesLayout.add(hitLabel);
            }
        }
    }

    protected Button createInstanceButton(String entityName, SearchResultEntry entry) {
        LinkButton instanceBtn = uiComponents.create(LinkButton.class);
        instanceBtn.setStyleName("fts-found-instance");
        instanceBtn.setAlignment(MIDDLE_LEFT);
        instanceBtn.addStyleName("c-fts-entity");

        BaseAction action = new BaseAction("instanceButton");
        action.withCaption(entry.getInstanceName());
        action.withHandler(e -> onInstanceClick(entityName, entry));

        instanceBtn.setAction(action);

        return instanceBtn;
    }

    protected Label<String> createHitLabel(String caption) {
        Label<String> hitLabel = uiComponents.create(Label.of(String.class));
        hitLabel.setValue(caption);
        hitLabel.setHtmlEnabled(true);
        hitLabel.addStyleName("c-fts-hit fts-hit"); //todo do we need both?
        hitLabel.setAlignment(MIDDLE_LEFT);
        return hitLabel;
    }

    protected void onInstanceClick(String entityName, SearchResultEntry entry) {
        Screen appWindow = Optional.ofNullable(AppUI.getCurrent())
                .map(AppUI::getTopLevelWindow)
                .map(Window::getFrameOwner)
                .orElse(null);

        if (appWindow instanceof Window.HasWorkArea) {
            AppWorkArea workArea = ((Window.HasWorkArea) appWindow).getWorkArea();

            if (workArea != null) {
                OpenMode openMode = AppWorkArea.Mode.TABBED == workArea.getMode()
                        ? OpenMode.NEW_TAB
                        : OpenMode.THIS_TAB;

                openEntityWindow(entry, entityName, openMode, appWindow);
            } else {
                throw new IllegalStateException("Application does not have any configured work area");
            }
        }
    }

    protected void openEntityWindow(SearchResultEntry entry, String entityName, OpenMode openMode, FrameOwner origin) {
        MetaClass metaClass = metadata.getSession().getClass(entityName);
        Object entity = reloadEntity(metaClass, entry.getDocId());
        screenBuilders.editor(metaClass.getJavaClass(), origin)
                .withOpenMode(openMode)
                .editEntity(entity)
                .show();
    }

    protected Object reloadEntity(MetaClass metaClass, Object entityId) {
        String primaryKeyProperty = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);
        return dataManager
                .load(metaClass.getJavaClass())
                .query(String.format("select e from %s e where e.%s in :id", metaClass.getName(), primaryKeyProperty))
                .parameter("id", Collections.singleton(entityId))
                .fetchPlan(FetchPlan.LOCAL)
                .one();
    }
}
