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

package io.jmix.aitoolsflowui.view.aiconversation.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.theme.StyleUtility;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Outlined card for a single conversation in the starter's recent list and
 * history side panel. Clicking the card body opens the conversation. The trash
 * delete button is rendered only when a delete handler is supplied (history
 * panel); the recent list passes {@code null} and shows no delete button.
 */
@NullMarked
public class AiConversationCard extends Composite<Card> {

    protected static final String BASE_CN = "ai-conversation-starter-conversation-card";
    protected static final String ROW_CN = BASE_CN + "-row";
    protected static final String BODY_CN = BASE_CN + "-body";
    protected static final String TITLE_ROW_CN = BASE_CN + "-title-row";
    protected static final String ICON_CN = BASE_CN + "-icon";
    protected static final String TITLE_CN = BASE_CN + "-title";
    protected static final String DATE_CN = BASE_CN + "-date";
    protected static final String DELETE_CN = BASE_CN + "-delete";

    @Override
    protected Card initContent() {
        return createContent();
    }

    public void setConversation(String title,
                                @Nullable String createdDate,
                                Runnable openHandler,
                                @Nullable Runnable deleteHandler,
                                @Nullable String deleteAriaLabel) {
        getContent().removeAll();

        VerticalLayout body = createBody(title, createdDate, openHandler);
        HorizontalLayout row = createRow(body);

        if (deleteHandler != null) {
            row.add(createDeleteButton(deleteHandler, deleteAriaLabel));
        }

        getContent().add(row);
    }

    /**
     * Stacks the title row and (optionally) the date as the clickable body of
     * the card. The whole body is the open-conversation hit area.
     */
    protected VerticalLayout createBody(String title,
                                        @Nullable String createdDate,
                                        Runnable openHandler) {
        VerticalLayout body = new VerticalLayout(createTitleRow(title));
        body.setPadding(false);
        body.setSpacing(false);
        body.setWidthFull();
        body.addClassName(BODY_CN);
        body.getStyle().set("cursor", "pointer");
        body.addClickListener(e -> openHandler.run());

        if (createdDate != null) {
            body.add(createDate(createdDate));
        }

        return body;
    }

    /**
     * Icon + title in one row, so the icon visually reads as a marker of the
     * title rather than of the whole card.
     */
    protected HorizontalLayout createTitleRow(String title) {
        HorizontalLayout titleRow = new HorizontalLayout(createIcon(), createTitle(title));
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setWidthFull();
        titleRow.addClassName(TITLE_ROW_CN);
        return titleRow;
    }

    protected AiAssistantIcon createIcon() {
        AiAssistantIcon icon = new AiAssistantIcon();
        icon.addClassName(ICON_CN);
        return icon;
    }

    protected Span createTitle(String title) {
        Span titleSpan = new Span(title);
        titleSpan.addClassName(TITLE_CN);
        return titleSpan;
    }

    protected Span createDate(String createdDate) {
        Span dateSpan = new Span(createdDate);
        dateSpan.addClassName(DATE_CN);
        return dateSpan;
    }

    /**
     * Outer row that lets the clickable body grow and parks an optional
     * delete button alongside it.
     */
    protected HorizontalLayout createRow(VerticalLayout body) {
        HorizontalLayout row = new HorizontalLayout(body);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.addClassName(ROW_CN);
        row.expand(body);
        return row;
    }

    /**
     * Trash button. Cross-theme: {@link StyleUtility.Button#LINK_BUTTON} gives
     * the flat "tertiary-inline" look; {@code icon} and {@code error} are
     * generic vaadin-button theme attributes (Lumo/Aura both honour them) —
     * no {@code LUMO_*} variant prefixes.
     */
    protected Button createDeleteButton(Runnable deleteHandler,
                                        @Nullable String deleteAriaLabel) {
        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addClassName(StyleUtility.Button.LINK_BUTTON);
        deleteButton.addClassName(DELETE_CN);
        deleteButton.addThemeName("icon");
        deleteButton.addThemeName("error");
        if (deleteAriaLabel != null) {
            deleteButton.setAriaLabel(deleteAriaLabel);
        }
        deleteButton.addClickListener(e -> deleteHandler.run());
        return deleteButton;
    }

    protected Card createContent() {
        Card card = new Card();
        card.setWidthFull();
        card.addClassName(BASE_CN);
        card.addThemeVariants(CardVariant.OUTLINED);
        return card;
    }
}
