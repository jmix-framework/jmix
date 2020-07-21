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

package io.jmix.ui.widget.client.listselect.single;

import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.ui.listselect.ListSelectConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.client.listselect.JmixListSelectServerRpc;
import io.jmix.ui.widget.client.listselect.JmixSingleListSelectState;
import io.jmix.ui.widget.listselect.JmixSingleListSelect;

@Connect(JmixSingleListSelect.class)
public class JmixSingleListSelectConnector extends ListSelectConnector {

    @Override
    protected void init() {
        super.init();

        getWidget().setDoubleClickListener((itemIndex) ->
                getRpcProxy(JmixListSelectServerRpc.class).onDoubleClick(itemIndex));
    }

    @Override
    public JmixSingleListSelectWidget getWidget() {
        return (JmixSingleListSelectWidget) super.getWidget();
    }

    @Override
    public JmixSingleListSelectState getState() {
        return (JmixSingleListSelectState) super.getState();
    }

    @OnStateChange("nullOptionVisible")
    void setNullOptionVisible() {
        getWidget().setNullOptionVisible(getState().nullOptionVisible);
    }
}
