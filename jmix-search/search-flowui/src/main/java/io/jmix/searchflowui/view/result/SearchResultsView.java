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

package io.jmix.searchflowui.view.result;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.*;
import io.jmix.searchflowui.component.SearchField;
import io.jmix.searchflowui.component.SearchFieldContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "search/results", layout = DefaultMainViewParent.class)
@ViewController("search_SearchResultsView")
@ViewDescriptor("search-results-view.xml")
@DialogMode(width = "50em", height = "42.5em", resizable = true)
public class SearchResultsView extends StandardView {


    public static final String QUERY_PARAM_VALUE = "value";
    public static final String QUERY_PARAM_ENTITIES = "entities";
    public static final String QUERY_PARAM_STRATEGY = "strategy";
    public static final String QUERY_PARAM_NEED_RELOAD = "needReload";

    protected static final Map<String, String> systemFieldLabels = ImmutableMap.<String, String>builder()
            .put("_file_name", "fileName")
            .put("_content", "content")
            .build();

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected EntitySearcher entitySearcher;
    @Autowired
    protected SearchStrategyManager searchStrategyManager;

    @ViewComponent
    protected VerticalLayout contentBoxWithName;

    protected SearchResult searchResult;

    protected SearchStrategy searchStrategy;
    protected boolean needReload = false;
    protected List<String> entities = Collections.emptyList();
    protected String value;
    @Autowired
    protected RouteSupport routeSupport;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected DialogWindows dialogWindows;

    protected SearchFieldContext searchFieldContext;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        parseQueryParameters(event.getLocation().getQueryParameters().getParameters());
        if (needReload) {
            initSearchFieldContext();
            SearchContext searchContext = new SearchContext(value)
                    .setSize(searchProperties.getSearchResultPageSize())
                    .setEntities(entities);
            searchResult = entitySearcher.search(searchContext, searchStrategy);
            handleSearchResult(searchResult);

//            SearchContext searchContext = new SearchContext(preparedSearchText)
//                    .setSize(searchProperties.getSearchResultPageSize())
//                    .setEntities(entities);

        }
    }

    protected void initSearchFieldContext() {
        searchFieldContext = new SearchFieldContext();
        searchFieldContext.setEntities(entities);
        searchFieldContext.setValue(value);
        searchFieldContext.setSearchStrategy(searchStrategy);
        //only happens when opened in navigation mode
        searchFieldContext.setOpenMode(OpenMode.NAVIGATION);
    }

    protected void parseQueryParameters(Map<String, List<String>> parameters) {
        if (parameters.containsKey(QUERY_PARAM_STRATEGY)) {
            parameters.get(QUERY_PARAM_STRATEGY).stream()
                    .findAny()
                    .ifPresent(parameter -> searchStrategy = searchStrategyManager.getSearchStrategyByName(parameter));
        }
        if (parameters.containsKey(QUERY_PARAM_NEED_RELOAD)) {
            parameters.get(QUERY_PARAM_NEED_RELOAD).stream()
                    .findAny()
                    .ifPresent(parameter -> needReload = Boolean.parseBoolean(parameter));
        } else {
            addNeedReloadParameter();
        }
        if (parameters.containsKey(QUERY_PARAM_VALUE)) {
            parameters.get(QUERY_PARAM_VALUE).stream()
                    .findAny()
                    .ifPresent(parameter -> value = parameter);
        }
        if (parameters.containsKey(QUERY_PARAM_ENTITIES)) {
            entities = parameters.get(QUERY_PARAM_ENTITIES);
        }
    }

    protected void addNeedReloadParameter() {
        getUI().ifPresent(ui -> {
            routeSupport.setQueryParameter(ui, QUERY_PARAM_NEED_RELOAD, Boolean.TRUE);
        });
    }

    public void initView(SearchFieldContext searchFieldContext, SearchResult searchResult) {
        setSearchResult(searchResult);
        this.searchFieldContext = searchFieldContext;
        handleSearchResult(searchResult);

    }

    public SearchResultsView setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
        return this;
    }

    @Override
    public String getPageTitle() {
        String caption = messageBundle.getMessage("resultViewTitle");
        if (searchResult != null) {
            String searchText = searchResult.getSearchText();
            return caption + ": " + searchText;
        } else {
            return super.getPageTitle();
        }
    }

    protected void handleSearchResult(SearchResult searchResult) {
        String searchText = searchResult.getSearchText();
        if (StringUtils.isBlank(searchText)) {
            handleNoSearchText();
            return;
        }

        renderResult(searchResult);
    }

    protected void handleNoSearchText() {
        Span span = createNoSearchTextSpan();
        contentBoxWithName.add(span);
    }

    protected void renderResult(SearchResult searchResult) {
        contentBoxWithName.removeAll();
        contentBoxWithName.add(createSearchField());

        VirtualList<SearchResultEntry> virtualList = uiComponents.create(VirtualList.class);
        virtualList.setRenderer(searchResultRenderer);
        virtualList.setItems(searchResult.getAllEntries());
        virtualList.setWidthFull();

        contentBoxWithName.expand(virtualList);
        contentBoxWithName.add(virtualList);
    }

    protected Span createNoSearchTextSpan() {
        Span span = uiComponents.create(Span.class);
        span.setText(messageBundle.getMessage("noSearchText"));
        return span;
    }

    protected Span createHitSpan(String caption) {
        Span hitSpan = uiComponents.create(Span.class);
        caption = "<div>" + caption + "</div>";
        hitSpan.add(new Html(caption));
        return hitSpan;
    }

    protected SearchField createSearchField() {
        SearchField searchField = uiComponents.create(SearchField.class);
        searchField.setSearchStrategy(searchFieldContext.getSearchStrategy());
        searchField.setValue(searchFieldContext.getValue());
        searchField.setEntities(searchFieldContext.getEntities());
        searchField.setOpenMode(searchFieldContext.getOpenMode());
        return searchField;
    }

    protected JmixButton createInstanceButton(String entityName, SearchResultEntry entry) {
        JmixButton instanceBtn = uiComponents.create(JmixButton.class);
        instanceBtn.addClassName("link");
        instanceBtn.addThemeName("tertiary-inline");
        instanceBtn.setText(messageTools.getEntityCaption(metadata.getClass(entry.getEntityName())) + " - "
                + entry.getInstanceName());
        instanceBtn.addClickListener(event -> openEntityView(entry, entityName));

        return instanceBtn;
    }

    protected void openEntityView(SearchResultEntry entry, String entityName) {
        MetaClass metaClass = metadata.getSession().getClass(entityName);
        Object entity = reloadEntity(metaClass, idSerialization.stringToId(entry.getDocId()));
        if (OpenMode.DIALOG.equals(searchFieldContext.getOpenMode())) {
            dialogWindows.detail(this, metaClass.getJavaClass())
                    .editEntity(entity)
                    .open();
        } else {
            viewNavigators.detailView(metaClass.getJavaClass())
                    .withBackwardNavigation(true)
                    .editEntity(entity)
                    .navigate();
        }
    }

    protected final ComponentRenderer<Component, SearchResultEntry> searchResultRenderer = new ComponentRenderer<>(entry -> {
        VerticalLayout verticalLayout = uiComponents.create(VerticalLayout.class);
        verticalLayout.setWidthFull();
        verticalLayout.setSpacing(false);
        verticalLayout.setPadding(false);
        JmixButton instanceBtn = createInstanceButton(entry.getEntityName(), entry);
        verticalLayout.add(instanceBtn);

        List<String> list = new ArrayList<>(entry.getFieldHits().size());
        Set<String> uniqueCaptions = new HashSet<>();
        for (FieldHit fieldHit : entry.getFieldHits()) {
            String fieldCaption = formatFieldCaption(entry.getEntityName(), fieldHit.getFieldName());
            if (!uniqueCaptions.contains(fieldCaption)) {
                list.add(fieldCaption + " : " + fieldHit.getHighlights());
                uniqueCaptions.add(fieldCaption);
            }
        }
        Collections.sort(list);

        for (String caption : list) {
            Span hitSpan = createHitSpan(caption);
            verticalLayout.add(hitSpan);
        }

        return verticalLayout;
//            entityDetails.addContent(verticalLayout);
//        }
//        return entityDetails;
    });

    protected String formatFieldCaption(String entityName, String fieldName) {
        List<String> captionParts = new ArrayList<>();
        String[] parts = fieldName.split("\\.");
        MetaClass currentMetaClass = metadata.getClass(entityName);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            MetaProperty currentMetaProperty = currentMetaClass.findProperty(part);
            if (currentMetaProperty == null) {
                break;
            }

            if (currentMetaProperty.getRange().isDatatype()
                    && (Datatype<?>) currentMetaProperty.getRange().asDatatype() instanceof FileRefDatatype
                    && i + 1 < parts.length) {
                String propertyCaption = messageTools.getPropertyCaption(currentMetaProperty);
                String nextPart = parts[i + 1];
                String labelKey = systemFieldLabels.get(nextPart);
                if (labelKey != null) {
                    String labelValue = messageBundle.getMessage(labelKey);
                    propertyCaption = propertyCaption + "[" + labelValue + "]";
                }
                captionParts.add(propertyCaption);
            } else {
                captionParts.add(messageTools.getPropertyCaption(currentMetaProperty));

                if (currentMetaProperty.getRange().isClass()) {
                    currentMetaClass = currentMetaProperty.getRange().asClass();
                } else {
                    break;
                }
            }
        }
        return Joiner.on(".").join(captionParts);
    }

    protected Object reloadEntity(MetaClass metaClass, Object entityId) {
        return dataManager
                .load(metaClass.getJavaClass())
                .id(entityId)
                .fetchPlan(FetchPlan.LOCAL)
                .one();
    }
}
