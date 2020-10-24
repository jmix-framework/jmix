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

package io.jmix.ui.component.presentation.action;

import io.jmix.core.Messages;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Table;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.widget.JmixEnhancedTable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

public abstract class AbstractPresentationAction extends AbstractAction {

    protected Table table;
    protected JmixEnhancedTable tableImpl;
    protected ComponentSettingsBinder settingsBinder;

    public AbstractPresentationAction(Table table, String id, @Nullable ComponentSettingsBinder settingsBinder) {
        super(id);

        this.table = table;
        this.tableImpl = table.unwrap(JmixEnhancedTable.class);
        this.settingsBinder = settingsBinder;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(id);
    }
}
