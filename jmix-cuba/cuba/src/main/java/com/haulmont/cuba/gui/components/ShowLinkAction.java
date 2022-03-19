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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.BaseAction;

@Deprecated
@SuppressWarnings("rawtypes")
public class ShowLinkAction extends BaseAction {

    public static final String ACTION_ID = "showLink";

    public interface Handler {
        String makeLink(Entity entity);
    }

    protected CollectionDatasource ds;
    protected Handler handler;

    public ShowLinkAction(CollectionDatasource ds, Handler handler) {
        super(ACTION_ID);

        this.ds = ds;
        this.handler = handler;

        Messages messages = AppBeans.get(Messages.class);
        setCaption(messages.getMessage("table.showLinkAction"));
    }

    @Override
    public void actionPerform(io.jmix.ui.component.Component component) {
        if (ds == null) {
            return;
        }

        Messages messages = AppBeans.get(Messages.class);

        Dialogs dialogs = AppUI.getCurrent().getDialogs();

        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("table.showLinkAction"))
                .withMessage(compileLink(ds))
                .show();
    }

    protected String compileLink(CollectionDatasource ds) {
        StringBuilder sb = new StringBuilder();

        Messages messages = AppBeans.get(Messages.class);
        sb.append(messages.getMessage("table.showLinkAction.link")).append("<br/>");
        sb.append("<textarea class=\"jmix-table-showlink-textarea\" autofocus=\"true\" readonly=\"true\">").
                append(handler.makeLink(ds.getItem()).replace("&", "&amp")).append("</textarea>");

        return sb.toString();
    }
}
