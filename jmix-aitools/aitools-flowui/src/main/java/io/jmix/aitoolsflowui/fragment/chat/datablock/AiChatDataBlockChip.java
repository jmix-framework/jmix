/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitoolsflowui.fragment.chat.datablock;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * INTERNAL.
 * <p>
 * The pending data block of a chat as a panel shows it: a band with the caption of the data, a control that removes
 * the block and an optional warning line. Meant for panels that implement {@link HasAiChatDataBlock}. Create it with
 * {@link UiComponents#create(Class)}.
 */
public class AiChatDataBlockChip extends Composite<Div> implements ApplicationContextAware, InitializingBean {

    private static final String BASE_CN = "jmix-ai-chat-data-block";
    private static final String ROW_CN = BASE_CN + "-row";
    private static final String ICON_CN = BASE_CN + "-icon";
    private static final String CAPTION_CN = BASE_CN + "-caption";
    private static final String REMOVE_CN = BASE_CN + "-remove";
    private static final String WARNING_CN = BASE_CN + "-warning";

    private ApplicationContext applicationContext;
    private Messages messages;
    private Icons icons;

    private Span caption;
    private Button removeButton;
    private Span warning;

    @Nullable
    private SerializableRunnable removeHandler;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        messages = applicationContext.getBean(Messages.class);
        icons = applicationContext.getBean(Icons.class);

        initComponent();
    }

    /**
     * Sets the text that names the data.
     *
     * @param caption the caption
     */
    public void setCaption(String caption) {
        this.caption.setText(caption);
    }

    /**
     * Sets the warning line shown under the caption.
     *
     * @param warning localized warning text, or {@code null} to hide the line
     */
    public void setWarning(@Nullable String warning) {
        this.warning.setText(warning != null ? warning : "");
        this.warning.setVisible(warning != null);
    }

    /**
     * Sets the handler called when the user removes the block.
     *
     * @param removeHandler the handler, or {@code null} to remove it
     */
    public void setRemoveHandler(@Nullable SerializableRunnable removeHandler) {
        this.removeHandler = removeHandler;
    }

    private void initComponent() {
        caption = createCaption();
        removeButton = createRemoveButton();
        warning = createWarning();

        Div row = new Div(createIcon(), caption, removeButton);
        row.addClassName(ROW_CN);

        Div root = getContent();
        root.addClassName(BASE_CN);
        root.add(row, warning);
    }

    private Component createIcon() {
        Component icon = icons.get(JmixFontIcon.TABLE);
        icon.getElement().getClassList().add(ICON_CN);
        icon.getElement().setAttribute("aria-hidden", "true");
        return icon;
    }

    private Span createCaption() {
        Span caption = new Span();
        caption.addClassName(CAPTION_CN);
        return caption;
    }

    private Button createRemoveButton() {
        String description = messages.getMessage("askAiAboutData.remove");

        Button button = new Button(icons.get(JmixFontIcon.CLOSE));
        button.addClassName(REMOVE_CN);
        button.addThemeVariants(ButtonVariant.SMALL, ButtonVariant.TERTIARY);
        button.setAriaLabel(description);
        button.setTooltipText(description);
        button.addClickListener(clickEvent -> onRemoveClick());
        return button;
    }

    private Span createWarning() {
        Span warning = new Span();
        warning.addClassName(WARNING_CN);
        warning.setVisible(false);
        return warning;
    }

    private void onRemoveClick() {
        if (removeHandler != null) {
            removeHandler.run();
        }
    }
}
