/*
 * Copyright 2020 Haulmont.
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
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.SuggestionFieldComponent;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.JmixAbstractSuggestionField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_STYLE_GENERATOR;

/**
 * Base class for SuggestionField components.
 *
 * @param <V> value type - collection or not
 * @param <I> item type
 * @param <T> component type
 */
public abstract class AbstractSuggestionField<V, I, T extends JmixAbstractSuggestionField<V, I>>
        extends AbstractField<T, V, V>
        implements SuggestionFieldComponent<V, I>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AbstractSuggestionField.class);

    protected BackgroundTaskHandler<List<I>> handler;
    protected SearchExecutor<I> searchExecutor;

    protected BackgroundWorker backgroundWorker;
    protected MetadataTools metadataTools;
    protected Locale locale;

    protected Function<? super I, String> optionStyleProvider;
    protected Formatter<? super I> formatter;

    protected Consumer<EnterPressEvent> enterPressHandler;
    protected Consumer<ArrowDownEvent> arrowDownHandler;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent(component);
    }

    protected void initComponent(T component) {
        component.setTextViewConverter(this::convertToTextView);

        component.setSearchExecutor(query -> {
            cancelSearch();
            searchSuggestions(query);
        });

        component.setCancelSearchHandler(this::cancelSearch);
    }

    @Override
    public void setSearchExecutor(@Nullable SearchExecutor<I> searchExecutor) {
        this.searchExecutor = searchExecutor;
    }

    @Nullable
    @Override
    public SearchExecutor<I> getSearchExecutor() {
        return searchExecutor;
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

    @Nullable
    @Override
    public Consumer<EnterPressEvent> getEnterPressHandler() {
        return enterPressHandler;
    }

    @Override
    public void setEnterPressHandler(@Nullable Consumer<EnterPressEvent> handler) {
        enterPressHandler = handler;

        if (enterPressHandler != null) {
            if (component.getEnterActionHandler() == null) {
                component.setEnterActionHandler(this::onEnterPressHandler);
            }
        } else {
            component.setEnterActionHandler(null);
        }
        component.setSelectFirstSuggestionOnShow(handler == null);
    }

    @Nullable
    @Override
    public Consumer<ArrowDownEvent> getArrowDownHandler() {
        return arrowDownHandler;
    }

    @Override
    public void setArrowDownHandler(Consumer<ArrowDownEvent> handler) {
        arrowDownHandler = handler;

        if (arrowDownHandler != null) {
            if (component.getArrowDownActionHandler() == null) {
                component.setArrowDownActionHandler(this::onArrowDownHandler);
            }
        } else {
            component.setArrowDownActionHandler(null);
        }
    }

    @Override
    public void showSuggestions(List<I> suggestions) {
        showSuggestions(suggestions, false);
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
    public void setStyleName(@Nullable String name) {
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
    public void setOptionStyleProvider(@Nullable Function<? super I, String> optionStyleProvider) {
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
    public Function<? super I, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Formatter<I> getFormatter() {
        return (Formatter<I>) formatter;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super I> formatter) {
        this.formatter = formatter;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected String generateItemStylename(Object item) {
        if (optionStyleProvider == null) {
            return null;
        }

        return this.optionStyleProvider.apply((I)item);
    }

    @Nullable
    protected BackgroundTask<Long, List<I>> getSearchSuggestionsTask(final String query) {
        if (this.searchExecutor == null) {
            return null;
        }

        SearchExecutor<I> currentSearchExecutor = this.searchExecutor;

        Map<String, Object> params;
        if (currentSearchExecutor instanceof ParametrizedSearchExecutor) {
            params = ((ParametrizedSearchExecutor<?>) currentSearchExecutor).getParams();
        } else {
            params = Collections.emptyMap();
        }

        return new BackgroundTask<Long, List<I>>(0) {
            @Override
            public List<I> run(TaskLifeCycle<Long> taskLifeCycle) throws Exception {
                List<I> result;
                try {
                    result = asyncSearch(currentSearchExecutor, query, params);
                } catch (RuntimeException e) {
                    log.error("Error in async search thread", e);

                    result = Collections.emptyList();
                }

                return result;
            }

            @Override
            public void done(List<I> result) {
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

    protected List<I> asyncSearch(SearchExecutor<I> searchExecutor, String searchString,
                                  Map<String, Object> params) throws Exception {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        log.debug("Search '{}'", searchString);

        List<I> searchResultItems;
        if (searchExecutor instanceof ParametrizedSearchExecutor) {
            ParametrizedSearchExecutor<I> pSearchExecutor = (ParametrizedSearchExecutor<I>) searchExecutor;
            searchResultItems = pSearchExecutor.search(searchString, params);
        } else {
            searchResultItems = searchExecutor.search(searchString, Collections.emptyMap());
        }

        return searchResultItems;
    }

    protected void handleSearchResult(List<I> results) {
        showSuggestions(results, true);
    }

    protected void showSuggestions(List<I> suggestions, boolean userOriginated) {
        FrameOwner frameOwner = getFrame().getFrameOwner();
        Collection<Screen> dialogScreens = UiControllerUtils.getScreenContext(frameOwner)
                .getScreens()
                .getOpenedScreens()
                .getDialogScreens();

        Screen lastDialog = null;
        for (Screen dialogScreen : dialogScreens) {
            lastDialog = dialogScreen;
        }

        if (frameOwner instanceof ScreenFragment) {
            frameOwner = ComponentsHelper.getScreen((ScreenFragment) frameOwner);
        }

        if (lastDialog == null || Objects.equals(frameOwner, lastDialog)) {
            component.showSuggestions(suggestions, userOriginated);
        }
    }

    protected void cancelSearch() {
        if (handler != null) {
            log.debug("Cancel previous search");

            handler.cancel();
            handler = null;
        }
    }

    protected void searchSuggestions(final String query) {
        BackgroundTask<Long, List<I>> task = getSearchSuggestionsTask(query);
        if (task != null) {
            handler = backgroundWorker.handle(task);
            handler.execute();
        }
    }

    protected String convertToTextView(@Nullable I item) {
        if (item == null) {
            return "";
        }

        if (formatter != null) {
            return nullToEmpty(formatter.apply(item));
        }

        return applyDefaultValueFormat(item);
    }

    protected String applyDefaultValueFormat(I item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    protected void onEnterPressHandler(String currentSearchString) {
        if (enterPressHandler != null) {
            enterPressHandler.accept(new EnterPressEvent(this, currentSearchString));
        }
    }

    protected void onArrowDownHandler(String currentSearchString) {
        if (arrowDownHandler != null) {
            arrowDownHandler.accept(new ArrowDownEvent(this, currentSearchString));
        }
    }
}
