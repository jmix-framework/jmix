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

package io.jmix.ui.widget.client.searchselect;

import io.jmix.ui.widget.JmixSearchSelect;
import io.jmix.ui.widget.client.combobox.JmixComboBoxConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;

@Connect(value = JmixSearchSelect.class, loadStyle = Connect.LoadStyle.LAZY)
public class JmixSearchSelectConnector extends JmixComboBoxConnector {

    @Override
    public JmixSearchSelectState getState() {
        return (JmixSearchSelectState) super.getState();
    }

    @Override
    public JmixSearchSelectWidget getWidget() {
        return (JmixSearchSelectWidget) super.getWidget();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("tabIndex")) {
            getWidget().updateTabIndex(getState().tabIndex);
        }
    }

    @Override
    protected void refreshData() {
        String filterInState = getState().currentFilterText != null ?
                getState().currentFilterText : "";
        if (!filterInState.equals(getWidget().lastFilter)) {
            return;
        }

        if (getDataSource().size() > 0) {
            super.refreshData();
        }
        getWidget().applyNewSuggestions();
    }
}
