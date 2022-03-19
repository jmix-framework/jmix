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

package io.jmix.ui.widget.client.tagfield;

import com.google.gwt.core.client.Scheduler;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.JmixTagField;
import io.jmix.ui.widget.client.suggestionfield.JmixSuggestionFieldConnector;

@Connect(JmixTagField.class)
public class JmixTagFieldConnector extends JmixSuggestionFieldConnector {

    @Override
    protected void init() {
        super.init();

        registerRpc(JmixTagFieldClientRpc.class, () -> getWidget().clearText());

        getWidget().setTagClickHandler(tagKey -> getRpcProxy(JmixTagFieldServerRpc.class).onTagClick(tagKey));
        getWidget().setTagRemoveHandler(tagKey -> getRpcProxy(JmixTagFieldServerRpc.class).onTagRemove(tagKey));
        getWidget().setClearItemHandler(() -> getRpcProxy(JmixTagFieldServerRpc.class).clearItems());
    }

    @Override
    public JmixTagFieldWidget getWidget() {
        return (JmixTagFieldWidget) super.getWidget();
    }

    @Override
    public JmixTagFieldState getState() {
        return (JmixTagFieldState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("tagsClickable")) {
            getWidget().setClickableTag(getState().tagsClickable);
        }

        if (stateChangeEvent.hasPropertyChanged("items")) {
            getWidget().setItems(getState().items);

            // after updating height style do layout as TagField may overlay other components
            Scheduler.get().scheduleDeferred(() -> {
                if (getWidget().updateWidgetHeightStyle()) {
                    getLayoutManager().setNeedsMeasure(JmixTagFieldConnector.this);
                }
            });
        }

        if (stateChangeEvent.hasPropertyChanged("clearAllVisible")) {
            getWidget().setClearAllVisible(getState().clearAllVisible);
        }
    }
}
