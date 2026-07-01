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

package io.jmix.aitoolsflowui.view.chathub.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import io.jmix.flowui.theme.StyleUtility;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Outlined card for a single conversation in the chat hub's recent list and
 * history side panel. Clicking the card body opens the conversation. The trash
 * delete button is rendered only when a delete handler is supplied (history
 * panel); without one the card shows no delete button.
 * <p>
 * Configure the card through the setters, then call {@link #build()} to
 * (re)assemble its content from the current property values.
 */
public class AiConversationCard extends Composite<Card> {

    protected static final String BASE_CN = "chat-hub-card";
    protected static final String ROW_CN = BASE_CN + "-row";
    protected static final String BODY_CN = BASE_CN + "-body";
    protected static final String TITLE_ROW_CN = BASE_CN + "-title-row";
    protected static final String ICON_CN = BASE_CN + "-icon";
    protected static final String TITLE_CN = BASE_CN + "-title";
    protected static final String DATE_CN = BASE_CN + "-date";
    protected static final String DELETE_CN = BASE_CN + "-delete";

    protected String title;
    protected Component icon;
    protected Runnable openHandler;

    @Nullable
    protected String createdDate;
    @Nullable
    protected Runnable deleteHandler;
    @Nullable
    protected String deleteAriaLabel;

    @Override
    protected Card initContent() {
        return createContent();
    }

    /**
     * Sets the title-row icon (required). Call {@link #build()} afterwards to
     * apply the change.
     *
     * @param icon title-row icon
     */
    public void setIcon(Component icon) {
        this.icon = icon;
    }

    /**
     * Sets the conversation title (required). Call {@link #build()} afterwards
     * to apply the change.
     *
     * @param title conversation title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the formatted creation date; when {@code null} the date line is
     * omitted. Call {@link #build()} afterwards to apply the change.
     *
     * @param createdDate formatted creation date, or {@code null} to omit the date line
     */
    @NullMarked
    public void setCreatedDate(@Nullable String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Sets the handler invoked when the card body is clicked (required). Call
     * {@link #build()} afterwards to apply the change.
     *
     * @param openHandler handler invoked when the card body is clicked
     */
    public void setOpenHandler(Runnable openHandler) {
        this.openHandler = openHandler;
    }

    /**
     * Sets the handler invoked by the delete button; when {@code null} no
     * delete button is rendered. Call {@link #build()} afterwards to apply the
     * change.
     *
     * @param deleteHandler handler invoked by the delete button, or {@code null} to render no delete button
     */
    @NullMarked
    public void setDeleteHandler(@Nullable Runnable deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    /**
     * Sets the {@code aria-label} of the delete button. Call {@link #build()}
     * afterwards to apply the change.
     *
     * @param deleteAriaLabel {@code aria-label} for the delete button, or {@code null} for none
     */
    @NullMarked
    public void setDeleteAriaLabel(@Nullable String deleteAriaLabel) {
        this.deleteAriaLabel = deleteAriaLabel;
    }

    /**
     * (Re)assembles the card content from the currently configured properties.
     * Call after the setters; {@link #setIcon(Component)},
     * {@link #setTitle(String)} and {@link #setOpenHandler(Runnable)} are
     * required and must be set beforehand.
     */
    public void build() {
        Component icon = requireNonNull(this.icon, "icon must be set before build()");
        String title = requireNonNull(this.title, "title must be set before build()");
        Runnable openHandler = requireNonNull(this.openHandler, "openHandler must be set before build()");

        getContent().removeAll();

        VerticalLayout body = createBody(icon, title, openHandler, createdDate);
        HorizontalLayout row = createRow(body);

        if (deleteHandler != null) {
            row.add(createDeleteButton(deleteHandler, deleteAriaLabel));
        }

        getContent().add(row);
    }

    /**
     * Stacks the title row and (optionally) the date as the clickable body of
     * the card. The whole body is the open-conversation hit area.
     *
     * @param icon        title-row icon
     * @param title       conversation title
     * @param openHandler handler invoked when the body is clicked
     * @param createdDate formatted creation date, or {@code null} to omit the date line
     * @return the assembled card body
     */
    protected VerticalLayout createBody(Component icon,
                                        String title,
                                        Runnable openHandler,
                                        @Nullable String createdDate) {
        VerticalLayout body = new VerticalLayout(createTitleRow(icon, title));
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
     *
     * @param icon  title-row icon
     * @param title conversation title
     * @return the assembled title row
     */
    protected HorizontalLayout createTitleRow(Component icon, String title) {
        icon.getElement().getClassList().add(ICON_CN);
        HorizontalLayout titleRow = new HorizontalLayout(icon, createTitle(title));
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setWidthFull();
        titleRow.addClassName(TITLE_ROW_CN);
        return titleRow;
    }

    protected Span createTitle(String title) {
        Span titleSpan = new Span(title);
        titleSpan.addClassName(TITLE_CN);
        Tooltip.forComponent(titleSpan).setText(title);
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
     *
     * @param body the clickable card body
     * @return the assembled outer row
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
     *
     * @param deleteHandler   handler invoked when the button is clicked
     * @param deleteAriaLabel {@code aria-label} for the button, or {@code null} for none
     * @return the assembled delete button
     */
    @NullMarked
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
