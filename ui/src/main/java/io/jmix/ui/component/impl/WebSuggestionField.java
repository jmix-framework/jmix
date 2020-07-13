/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.core.MetadataTools;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.SuggestionField;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.JmixSuggestionField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static io.jmix.ui.component.impl.WebComboBox.NULL_STYLE_GENERATOR;

public class WebSuggestionField<V> extends WebV8AbstractField<JmixSuggestionField<V>, V, V>
        implements SuggestionField<V>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(WebSuggestionField.class);

    protected BackgroundTaskHandler<List<V>> handler;

    protected SearchExecutor<V> searchExecutor;

    protected EnterActionHandler enterActionHandler;
    protected ArrowDownActionHandler arrowDownActionHandler;

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> optionStyleProvider;

    protected BackgroundWorker backgroundWorker;
    protected MetadataTools metadataTools;
    protected Locale locale;

    public WebSuggestionField() {
        component = createComponent();

        attachValueChangeListener(component);
    }

    @Autowired
    protected void setBackgroundWorker(BackgroundWorker backgroundWorker) {
        this.backgroundWorker = backgroundWorker;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Autowired
    protected void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    protected JmixSuggestionField<V> createComponent() {
        return new JmixSuggestionField<>();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixSuggestionField<V> component) {
        component.setTextViewConverter(this::convertToTextView);

        component.setSearchExecutor(query -> {
            cancelSearch();
            searchSuggestions(query);
        });

        component.setCancelSearchHandler(this::cancelSearch);
    }

    @Nullable
    @Override
    public V getValue() {
        V value = super.getValue();

        // todo rework OptionWrapper compatibility
        //noinspection unchecked
        return value instanceof OptionWrapper
                ? (V) ((OptionWrapper) value).getValue()
                : value;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected String generateItemStylename(Object item) {
        if (optionStyleProvider == null) {
            return null;
        }

        return this.optionStyleProvider.apply((V)item);
    }

    protected String convertToTextView(@Nullable V value) {
        if (value == null) {
            return "";
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(value);
        }

        return generateDefaultItemCaption(value);
    }

    protected String generateDefaultItemCaption(V item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        if (this.optionCaptionProvider != optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    protected void cancelSearch() {
        if (handler != null) {
            log.debug("Cancel previous search");

            handler.cancel();
            handler = null;
        }
    }

    protected void searchSuggestions(final String query) {
        BackgroundTask<Long, List<V>> task = getSearchSuggestionsTask(query);
        if (task != null) {
            handler = backgroundWorker.handle(task);
            handler.execute();
        }
    }

    @Nullable
    protected BackgroundTask<Long, List<V>> getSearchSuggestionsTask(final String query) {
        if (this.searchExecutor == null) {
            return null;
        }

        SearchExecutor<V> currentSearchExecutor = this.searchExecutor;

        Map<String, Object> params;
        if (currentSearchExecutor instanceof ParametrizedSearchExecutor) {
            params = ((ParametrizedSearchExecutor<?>) currentSearchExecutor).getParams();
        } else {
            params = Collections.emptyMap();
        }

        return new BackgroundTask<Long, List<V>>(0) {
            @Override
            public List<V> run(TaskLifeCycle<Long> taskLifeCycle) throws Exception {
                List<V> result;
                try {
                    result = asyncSearch(currentSearchExecutor, query, params);
                } catch (RuntimeException e) {
                    log.error("Error in async search thread", e);

                    result = Collections.emptyList();
                }

                return result;
            }

            @Override
            public void done(List<V> result) {
                log.debug("Search results for '{}'", query);
                handleSearchResult(result);
            }

            @Override
            public boolean handleException(Exception ex) {
                log.error("Error in async search thread", ex);
                return true;
            }
        };
    }

    protected List<V> asyncSearch(SearchExecutor<V> searchExecutor, String searchString,
                                  Map<String, Object> params) throws Exception {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        log.debug("Search '{}'", searchString);

        List<V> searchResultItems;
        if (searchExecutor instanceof ParametrizedSearchExecutor) {
            ParametrizedSearchExecutor<V> pSearchExecutor = (ParametrizedSearchExecutor<V>) searchExecutor;
            searchResultItems = pSearchExecutor.search(searchString, params);
        } else {
            searchResultItems = searchExecutor.search(searchString, Collections.emptyMap());
        }

        return searchResultItems;
    }

    protected void handleSearchResult(List<V> results) {
        showSuggestions(results, true);
    }

    @Override
    public int getMinSearchStringLength() {
        return component.getMinSearchStringLength();
    }

    @Override
    public void setMinSearchStringLength(int minSearchStringLength) {
        component.setMinSearchStringLength(minSearchStringLength);
    }

    @Override
    public int getSuggestionsLimit() {
        return component.getSuggestionsLimit();
    }

    @Override
    public void setSuggestionsLimit(int suggestionsLimit) {
        component.setSuggestionsLimit(suggestionsLimit);
    }

    @Override
    public int getAsyncSearchDelayMs() {
        return component.getAsyncSearchDelayMs();
    }

    @Override
    public void setAsyncSearchDelayMs(int asyncSearchDelayMs) {
        component.setAsyncSearchDelayMs(asyncSearchDelayMs);
    }

    @Override
    public EnterActionHandler getEnterActionHandler() {
        return enterActionHandler;
    }

    @Override
    public void setEnterActionHandler(EnterActionHandler enterActionHandler) {
        this.enterActionHandler = enterActionHandler;
        component.setEnterActionHandler(enterActionHandler::onEnterKeyPressed);
    }

    @Override
    public ArrowDownActionHandler getArrowDownActionHandler() {
        return arrowDownActionHandler;
    }

    @Override
    public void setArrowDownActionHandler(ArrowDownActionHandler arrowDownActionHandler) {
        this.arrowDownActionHandler = arrowDownActionHandler;
        component.setArrowDownActionHandler(arrowDownActionHandler::onArrowDownKeyPressed);
    }

    @Override
    public void showSuggestions(List<V> suggestions) {
        showSuggestions(suggestions, false);
    }

    protected void showSuggestions(List<V> suggestions, boolean userOriginated) {
        FrameOwner frameOwner = getFrame().getFrameOwner();
        Collection<Screen> dialogScreens = UiControllerUtils.getScreenContext(frameOwner)
                .getScreens()
                .getOpenedScreens()
                .getDialogScreens();

        Screen lastDialog = null;
        for (Screen dialogScreen : dialogScreens) {
            lastDialog = dialogScreen;
        }

        if (lastDialog == null || Objects.equals(frameOwner, lastDialog)) {
            component.showSuggestions(suggestions, userOriginated);
        }
    }

    @Override
    public SearchExecutor getSearchExecutor() {
        return searchExecutor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSearchExecutor(SearchExecutor searchExecutor) {
        this.searchExecutor = searchExecutor;
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Nullable
    @Override
    public String getInputPrompt() {
        return component.getInputPrompt();
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        component.setInputPrompt(inputPrompt);
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);

        component.setPopupStyleName(name);
    }

    @Override
    public void addStyleName(String styleName) {
        super.addStyleName(styleName);

        component.addPopupStyleName(styleName);
    }

    @Override
    public void removeStyleName(String styleName) {
        super.removeStyleName(styleName);

        component.removePopupStyleName(styleName);
    }

    @Override
    public void setPopupWidth(String popupWidth) {
        component.setPopupWidth(popupWidth);
    }

    @Override
    public String getPopupWidth() {
        return component.getPopupWidth();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionStyleProvider(@Nullable Function<? super V, String> optionStyleProvider) {
        if (this.optionStyleProvider != optionStyleProvider) {
            this.optionStyleProvider = optionStyleProvider;

            if (optionStyleProvider != null) {
                component.setOptionsStyleProvider(this::generateItemStylename);
            } else {
                component.setOptionsStyleProvider(NULL_STYLE_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }
}
