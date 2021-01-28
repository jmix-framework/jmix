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

package io.jmix.ui.widget.client.suggestionfield;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractFieldConnector;
import com.vaadin.shared.ui.Connect;
import elemental.json.JsonArray;
import io.jmix.ui.widget.JmixSuggestionField;

@Connect(JmixSuggestionField.class)
public class JmixSuggestionFieldConnector extends AbstractFieldConnector {

    protected JmixSuggestionFieldServerRpc serverRpc = RpcProxy.create(JmixSuggestionFieldServerRpc.class, this);

    public JmixSuggestionFieldConnector() {
        //noinspection Convert2Lambda
        registerRpc(JmixSuggestionFieldClientRpc.class, new JmixSuggestionFieldClientRpc() {
            @Override
            public void showSuggestions(JsonArray suggestions, boolean userOriginated) {
                getWidget().showSuggestions(suggestions, userOriginated);
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        JmixSuggestionFieldWidget widget = getWidget();

        widget.searchExecutor = query -> serverRpc.searchSuggestions(query);
        widget.arrowDownActionHandler = query -> serverRpc.onArrowDownKeyPressed(query);
        widget.enterActionHandler = query -> serverRpc.onEnterKeyPressed(query);
        widget.suggestionSelectedHandler = suggestion -> {
            serverRpc.selectSuggestion(suggestion.getId());

            updateWidgetValue(widget);
        };
        widget.cancelSearchHandler = () -> serverRpc.cancelSearch();
    }

    @Override
    public JmixSuggestionFieldWidget getWidget() {
        return (JmixSuggestionFieldWidget) super.getWidget();
    }

    @Override
    public JmixSuggestionFieldState getState() {
        return (JmixSuggestionFieldState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        JmixSuggestionFieldWidget widget = getWidget();
        if (stateChangeEvent.hasPropertyChanged("minSearchStringLength")) {
            widget.setMinSearchStringLength(getState().minSearchStringLength);
        }

        if (stateChangeEvent.hasPropertyChanged("asyncSearchDelayMs")) {
            widget.setAsyncSearchDelayMs(getState().asyncSearchDelayMs);
        }

        if (stateChangeEvent.hasPropertyChanged("text")) {
            updateWidgetValue(widget);
        }

        if (stateChangeEvent.hasPropertyChanged("inputPrompt")) {
            widget.setInputPrompt(getState().inputPrompt);
        }

        if (stateChangeEvent.hasPropertyChanged("popupStylename")) {
            widget.setPopupStyleName(getState().popupStylename);
        }

        if (stateChangeEvent.hasPropertyChanged("popupWidth")) {
            widget.setPopupWidth(getState().popupWidth);
        }

        if (stateChangeEvent.hasPropertyChanged("selectFirstSuggestionOnShow")) {
            widget.setSelectFirstSuggestionOnShow(getState().selectFirstSuggestionOnShow);
        }

        widget.setReadonly(isReadOnly());
    }

    protected void updateWidgetValue(JmixSuggestionFieldWidget widget) {
        String stateValue = getState().text;
        if (!stateValue.equals(widget.getValue())) {
            widget.setValue(stateValue, false);
        }
    }
}
