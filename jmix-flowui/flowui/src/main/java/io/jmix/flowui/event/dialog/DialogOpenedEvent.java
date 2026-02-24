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

package io.jmix.flowui.event.dialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.flowui.Dialogs.SideDialogBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Application event that is sent after the dialog is shown.
 */
public class DialogOpenedEvent extends ApplicationEvent {

    protected Component content;
    protected List<Button> buttons;

    protected List<Component> headerComponents;
    protected List<Component> contentComponents;
    protected List<Component> footerComponents;

    public DialogOpenedEvent(Dialog dialog, Component content, List<Button> buttons) {
        this(dialog,
                Collections.emptyList(),
                Collections.singletonList(content),
                CollectionUtils.isEmpty(buttons) ? Collections.emptyList() : new ArrayList<>(buttons));
    }

    public DialogOpenedEvent(Dialog dialog,
                             List<Component> headerComponents,
                             List<Component> contentComponents,
                             List<Component> footerComponents) {
        super(dialog);

        this.headerComponents = headerComponents;
        this.contentComponents = contentComponents;
        this.footerComponents = footerComponents;

        this.content = contentComponents.isEmpty() ? null : contentComponents.get(0);

        if (CollectionUtils.isEmpty(footerComponents)) {
            buttons = Collections.emptyList();
        } else {
            boolean allMatchButton = footerComponents.stream().allMatch(component -> component instanceof Button);
            if (allMatchButton) {
                buttons = footerComponents.stream().map(component -> (Button) component).toList();
            }
        }
    }

    @Override
    public Dialog getSource() {
        return (Dialog) super.getSource();
    }

    /**
     * Returns the dialog content component (e.g., for message dialog, option dialog, etc.).
     * If the dialog content includes multiple components, the first component is returned.
     *
     * @return content of the opened dialog
     */
    public Component getContent() {
        return content;
    }

    /**
     * Returns header components of the opened dialog if the builder of the dialog supports setting components to
     * the header (e.g. {@link SideDialogBuilder}).
     *
     * @return header components
     */
    public List<Component> getHeaderComponents() {
        return Collections.unmodifiableList(headerComponents);
    }

    /**
     * Returns content components of the opened dialog.
     *
     * @return content components
     */
    public List<Component> getContentComponents() {
        return Collections.unmodifiableList(contentComponents);
    }

    /**
     * Returns footer components of the opened dialog if the builder of the dialog supports setting components to
     * the footer (e.g. {@link SideDialogBuilder}).
     *
     * @return footer components
     */
    public List<Component> getFooterComponents() {
        return Collections.unmodifiableList(footerComponents);
    }

    /**
     * @return list of dialog buttons
     */
    public List<Button> getButtons() {
        return buttons;
    }
}
