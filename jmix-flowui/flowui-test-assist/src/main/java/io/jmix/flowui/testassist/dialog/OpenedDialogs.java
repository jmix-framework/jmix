/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.testassist.dialog;

import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.flowui.event.dialog.DialogClosedEvent;
import io.jmix.flowui.event.dialog.DialogOpenedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Bean contains opened {@link Dialog}s.
 */
@Component("ui_OpenedDialogs")
public class OpenedDialogs {

    protected Map<Dialog, DialogInfo> openedDialogs = new HashMap<>();

    /**
     * @return list of {@link DialogInfo}s
     */
    public List<DialogInfo> getDialogs() {
        return openedDialogs.values().stream().toList();
    }

    /**
     * Closes {@link Dialog}s.
     */
    public void clearOpenedDialogs() {
        Iterator<Dialog> iterator = openedDialogs.keySet().iterator();

        while (iterator.hasNext()) {
            Dialog dialog = iterator.next();
            iterator.remove();
            dialog.close();
        }
    }

    @EventListener
    protected void onDialogOpened(DialogOpenedEvent event) {
        Dialog dialog = event.getSource();
        openedDialogs.put(dialog, mapToDialogInfo(event));
    }

    @EventListener
    protected void onDialogClosed(DialogClosedEvent event) {
        Dialog dialog = event.getSource();
        openedDialogs.remove(dialog);
    }

    protected DialogInfo mapToDialogInfo(DialogOpenedEvent event) {
        return new DialogInfo(event.getSource())
                .withContent(event.getContent())
                .withButtons(event.getButtons());
    }
}
