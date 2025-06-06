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

package io.jmix.flowui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.event.view.ViewClosedEvent;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Contains opened {@link View}s in {@link OpenMode#DIALOG} for corresponding {@link UI}s.
 */
@Component("flowui_OpenedDialogWindows")
@VaadinSessionScope
public class OpenedDialogWindows {
    private static final Logger log = LoggerFactory.getLogger(OpenedDialogWindows.class);

    protected Map<UI, List<View<?>>> openedDialogs = new HashMap<>();

    /**
     * Returns a list of {@link View}s that were opened in {@link OpenMode#DIALOG} for the current {@link UI}.
     * <p>
     * <strong>Note that the last opened dialog will be the last item in the returned list.</strong> For instance,
     * if the user opens views as dialogs in the following order:
     * <ol>
     *     <li>
     *         Order lookup view - will be first in the returned list (0 index);
     *     </li>
     *     <li>
     *         Order detail view -will be second in the returned list (1 index);
     *     </li>
     *     <li>
     *         and so on.
     *     </li>
     * </ol>
     *
     * @return list of {@link View}s
     */
    public List<View<?>> getDialogs() {
        return getDialogs(UI.getCurrent());
    }

    /**
     * Returns a list of {@link View}s that were opened in {@link OpenMode#DIALOG} for the provided {@link UI}.
     * <p>
     * <strong>Note that the last opened dialog will be the last item in the returned list.</strong> For instance,
     * if the user opens views as dialogs in the following order:
     * <ol>
     *     <li>
     *         Order lookup view - will be first in the returned list (0 index);
     *     </li>
     *     <li>
     *         Order detail view -will be second in the returned list (1 index);
     *     </li>
     *     <li>
     *         and so on.
     *     </li>
     * </ol>
     *
     * @param ui the UI contains opened {@link View}s
     * @return list of {@link View}s
     */
    public List<View<?>> getDialogs(UI ui) {
        return Collections.unmodifiableList(openedDialogs.getOrDefault(ui, Collections.emptyList()));
    }

    /**
     * @return the currently opened {@link View} in {@link OpenMode#DIALOG} for the current {@link UI}.
     */
    public Optional<View<?>> getCurrentDialog() {
        return getCurrentDialog(UI.getCurrent());
    }

    /**
     * @param ui the UI contains the currently opened {@link View}
     * @return the currently opened {@link View} in {@link OpenMode#DIALOG} for the provided {@link UI}.
     */
    public Optional<View<?>> getCurrentDialog(UI ui) {
        List<View<?>> dialogs = getDialogs(ui);
        return dialogs.isEmpty() ? Optional.empty() : Optional.of(dialogs.get(dialogs.size() - 1));
    }

    @EventListener
    private void onViewOpened(ViewOpenedEvent event) {
        View<?> view = event.getSource();

        if (!UiComponentUtils.isComponentAttachedToDialog(view)) {
            return;
        }

        log.trace("{} dialog is opened", view.getClass().getSimpleName());

        UI currentUI = view.getUI().orElseGet(UI::getCurrent);

        List<View<?>> views = openedDialogs.computeIfAbsent(currentUI, __ -> new ArrayList<>());
        views.add(view);
    }

    @EventListener
    private void onViewClosed(ViewClosedEvent event) {
        View<?> view = event.getSource();

        if (!UiComponentUtils.isComponentAttachedToDialog(view)) {
            return;
        }

        UI currentUI = view.getUI().orElseGet(UI::getCurrent);

        log.trace("{} dialog is closed", view.getClass().getSimpleName());

        List<View<?>> views = openedDialogs.get(currentUI);
        if (views != null) {
            views.remove(view);
        }
    }
}
