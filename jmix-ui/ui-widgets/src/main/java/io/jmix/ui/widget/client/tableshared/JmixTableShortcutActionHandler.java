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

package io.jmix.ui.widget.client.tableshared;

import com.google.gwt.dom.client.Element;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ui.ShortcutActionHandler;
import com.vaadin.v7.client.ui.table.TableConnector;

public class JmixTableShortcutActionHandler extends ShortcutActionHandler {

    protected TableConnector target;

    public JmixTableShortcutActionHandler(String pid, ApplicationConnection client, TableConnector target) {
        super(pid, client);
        this.target = target;
    }

    @Override
    protected ComponentConnector getTargetConnector(ComponentConnector target, Element et) {
        return this.target;
    }
}
