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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.EntitySuggestionField;
import io.jmix.ui.component.SecuredActionsHolder;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.JmixPickerField;
import io.jmix.ui.widget.JmixSuggestionPickerField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_STYLE_GENERATOR;

public class EntitySuggestionFieldImpl<V> extends EntityPickerImpl<V>
        implements EntitySuggestionField<V>, SecuredActionsHolder {

    private static final Logger log = LoggerFactory.getLogger(EntitySuggestionFieldImpl.class);

    protected BackgroundWorker backgroundWorker;

    protected BackgroundTaskHandler<List<V>> handler;

    protected SearchExecutor<V> searchExecutor;

    protected Function<? super V, String> optionStyleProvider;

    protected Consumer<EnterPressEvent> enterPressHandler;
    protected Consumer<ArrowDownEvent> arrowDownHandler;

    protected Locale locale;

    public EntitySuggestionFieldImpl() {
    }

    @Override
    protected JmixPickerField<V> createComponent() {
        return new JmixSuggestionPickerField<>();
    }

    @Override
    public JmixSuggestionPickerField<V> getComponent() {
        return (JmixSuggestionPickerField<V>) super.getComponent();
    }

    @Autowired
    public void setBackgroundWorker(BackgroundWorker backgroundWorker) {
        this.backgroundWorker = backgroundWorker;
    }

    @Override
    protected void initComponent(JmixPickerField<V> component) {
        getComponent().setTextViewConverter(this::formatValue);

        getComponent().setSearchExecutor(query -> {
            cancelSearch();
            searchSuggestions(query);
        });

        getComponent().setCancelSearchHandler(this::cancelSearch);
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
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
        if (this.searchExecutor == null)
            return null;

        final SearchExecutor<V> currentSearchExecutor = this.searchExecutor;

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
                try {
                    handleSearchResult(result);
                } catch (RuntimeException e) {
                    log.error("Error while processing search result", e);
                }
            }

            @Override
            public void canceled() {
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

    @SuppressWarnings("unchecked")
    @Nullable
    protected String generateItemStylename(Object item) {
        if (optionStyleProvider == null) {
            return null;
        }

        return this.optionStyleProvider.apply((V) item);
    }

    @Override
    public Subscription addFieldValueChangeListener(Consumer<FieldValueChangeEvent<V>> listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFieldEditable() {
        return false;
    }

    @Override
    public void setFieldEditable(boolean editable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAsyncSearchDelayMs() {
        return getComponent().getAsyncSearchDelayMs();
    }

    @Override
    public void setAsyncSearchDelayMs(int asyncSearchDelayMs) {
        getComponent().setAsyncSearchDelayMs(asyncSearchDelayMs);
    }

    @Nullable
    @Override
    public SearchExecutor<V> getSearchExecutor() {
        return searchExecutor;
    }

    @Override
    public void setSearchExecutor(@Nullable SearchExecutor<V> searchExecutor) {
        this.searchExecutor = searchExecutor;
    }

    @Nullable
    @Override
    public Consumer<EnterPressEvent> getEnterPressHandler() {
        return enterPressHandler;
    }

    @Override
    public void setEnterPressHandler(@Nullable Consumer<EnterPressEvent> handler) {
        enterPressHandler = handler;

        if (handler != null) {
            if (getComponent().getEnterActionHandler() == null) {
                getComponent().setEnterActionHandler(this::onEnterPressHandler);
            }
        } else {
            getComponent().setEnterActionHandler(null);
        }
        getComponent().setSelectFirstSuggestionOnShow(handler == null);
    }

    @Nullable
    @Override
    public Consumer<ArrowDownEvent> getArrowDownHandler() {
        return arrowDownHandler;
    }

    @Override
    public void setArrowDownHandler(@Nullable Consumer<ArrowDownEvent> handler) {
        arrowDownHandler = handler;

        if (handler != null) {
            if (getComponent().getArrowDownActionHandler() == null) {
                getComponent().setArrowDownActionHandler(this::onArrowDownHandler);
            }
        } else {
            getComponent().setArrowDownActionHandler(null);
        }
    }

    @Override
    public int getMinSearchStringLength() {
        return getComponent().getMinSearchStringLength();
    }

    @Override
    public void setMinSearchStringLength(int minSearchStringLength) {
        getComponent().setMinSearchStringLength(minSearchStringLength);
    }

    @Override
    public int getSuggestionsLimit() {
        return getComponent().getSuggestionsLimit();
    }

    @Override
    public void setSuggestionsLimit(int suggestionsLimit) {
        getComponent().setSuggestionsLimit(suggestionsLimit);
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

        if (frameOwner instanceof ScreenFragment) {
            frameOwner = ComponentsHelper.getScreen((ScreenFragment) frameOwner);
        }

        if (lastDialog == null || Objects.equals(frameOwner, lastDialog)) {
            getComponent().showSuggestions(suggestions, userOriginated);
        }
    }

    @Override
    public void setPopupWidth(String popupWidth) {
        getComponent().setPopupWidth(popupWidth);
    }

    @Override
    public String getPopupWidth() {
        return getComponent().getPopupWidth();
    }

    @Nullable
    @Override
    public String getInputPrompt() {
        return getComponent().getInputPrompt();
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        getComponent().setInputPrompt(inputPrompt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionStyleProvider(@Nullable Function<? super V, String> optionStyleProvider) {
        if (this.optionStyleProvider != optionStyleProvider) {
            this.optionStyleProvider = optionStyleProvider;

            if (optionStyleProvider != null) {
                getComponent().setOptionsStyleProvider(this::generateItemStylename);
            } else {
                getComponent().setOptionsStyleProvider(NULL_STYLE_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionStyleProvider() {
        return optionStyleProvider;
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        getComponent().setPopupStyleName(name);
    }

    @Override
    public void addStyleName(String styleName) {
        super.addStyleName(styleName);

        getComponent().addPopupStyleName(styleName);
    }

    @Override
    public void removeStyleName(String styleName) {
        super.removeStyleName(styleName);

        getComponent().removePopupStyleName(styleName);
    }

    @Override
    protected void checkValueType(@Nullable V value) {
        // do not check
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
