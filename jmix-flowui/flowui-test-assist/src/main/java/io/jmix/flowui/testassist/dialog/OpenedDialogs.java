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

import com.google.common.collect.Iterables;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.flowui.event.dialog.DialogClosedEvent;
import io.jmix.flowui.event.dialog.DialogOpenedEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean contains opened {@link Dialog}s in order of opening.
 * <p>
 * Example of the order in which dialogs are stored:
 * <ul>
 *     <li>first opened dialog has index {@code 0}</li>
 *     <li>seconds opened dialog has index {@code 1}</li>
 *     <li>last opened dialog has index {@code openedDialogs.size() - 1}</li>
 * </ul>
 */
@Component("flowui_OpenedDialogs")
public class OpenedDialogs {

    protected static Map<Dialog, DialogInfo> openedDialogs = new LinkedHashMap<>();

    /**
     * @return immutable list of {@link DialogInfo}s
     */
    public List<DialogInfo> getDialogs() {
        return List.copyOf(openedDialogs.values());
    }

    /**
     * @return the most recent opened {@link DialogInfo} or {@code null} if no opened dialogs
     */
    @Nullable
    public DialogInfo getLastDialog() {
        return CollectionUtils.isEmpty(openedDialogs.values())
                ? null
                : Iterables.getLast(openedDialogs.values());
    }

    /**
     * Closes opened {@link Dialog}s and removes them from the storage map.
     */
    public void closeOpenedDialogs() {
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
