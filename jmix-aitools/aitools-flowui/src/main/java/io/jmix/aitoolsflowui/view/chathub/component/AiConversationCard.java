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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import io.jmix.flowui.theme.StyleUtility;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Outlined card for a single conversation in the chat hub's recent list and
 * history side panel. The card body is a real link to the conversation, so it
 * supports opening in a new browser tab, copying the address and keyboard
 * activation; a plain click is handled in-app by the open handler instead of
 * following the link. The trash delete button is rendered only when a delete
 * handler is supplied (history panel); without one the card shows no delete
 * button.
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
    protected String href;
    protected Runnable openHandler;

    @Nullable
    protected String activityDate;
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
     * Sets the formatted last-activity date shown under the title; when {@code null} the date line is
     * omitted. Call {@link #build()} afterwards to apply the change.
     *
     * @param activityDate formatted last-activity date, or {@code null} to omit the date line
     */
    @NullMarked
    public void setActivityDate(@Nullable String activityDate) {
        this.activityDate = activityDate;
    }

    /**
     * Sets the address the card body links to (required) — the conversation's own route. It is what the
     * browser uses for "open in new tab" and "copy link address"; a plain click goes to
     * {@link #setOpenHandler(Runnable)} instead. Call {@link #build()} afterwards to apply the change.
     *
     * @param href address of the conversation
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * Sets the handler invoked on a plain click on the card body (required), so that the click stays an
     * in-app navigation instead of a page load. Modified and non-primary clicks are left to the browser.
     * Call {@link #build()} afterwards to apply the change.
     *
     * @param openHandler handler invoked on a plain click on the card body
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
     * {@link #setTitle(String)}, {@link #setHref(String)} and
     * {@link #setOpenHandler(Runnable)} are required and must be set
     * beforehand.
     */
    public void build() {
        Component icon = requireNonNull(this.icon, "icon must be set before build()");
        String title = requireNonNull(this.title, "title must be set before build()");
        String href = requireNonNull(this.href, "href must be set before build()");
        Runnable openHandler = requireNonNull(this.openHandler, "openHandler must be set before build()");

        getContent().removeAll();

        Anchor body = createBody(icon, title, href, openHandler, activityDate);
        HorizontalLayout row = createRow(body);

        if (deleteHandler != null) {
            row.add(createDeleteButton(deleteHandler, deleteAriaLabel));
        }

        getContent().add(row);
    }

    /**
     * Stacks the title row and (optionally) the date as the link body of the
     * card. The whole body is the open-conversation hit area.
     *
     * @param icon         title-row icon
     * @param title        conversation title
     * @param href         address of the conversation
     * @param openHandler  handler invoked on a plain click on the body
     * @param activityDate formatted last-activity date, or {@code null} to omit the date line
     * @return the assembled card body
     */
    protected Anchor createBody(Component icon,
                                String title,
                                String href,
                                Runnable openHandler,
                                @Nullable String activityDate) {
        Anchor body = new Anchor(href);
        body.addClassName(BODY_CN);
        body.add(createTitleRow(icon, title));

        if (activityDate != null) {
            body.add(createDate(activityDate));
        }

        bindOpenHandler(body, openHandler);

        return body;
    }

    /**
     * Routes a plain click on the link to the open handler instead of letting the browser load the address.
     * Everything else — a modified click, a middle click (which fires {@code auxclick}, not {@code click}),
     * the context menu — is left to the browser, which is what makes "open in new tab" and "copy link
     * address" work.
     * <p>
     * Suppressing the default action is part of the filter expression on purpose: Flow evaluates every
     * event-data expression before it checks the filter, so {@code preventDefault} passed as event data
     * would also cancel the modified clicks that must reach the browser. The filter is evaluated once per
     * event, so folding {@code preventDefault} into the guard cancels exactly the clicks that are handled
     * here.
     *
     * @param body        the link body of the card
     * @param openHandler handler invoked on a plain click
     */
    protected void bindOpenHandler(Anchor body, Runnable openHandler) {
        body.getElement()
                .addEventListener("click", event -> openHandler.run())
                .setFilter("""
                        event.button === 0 && !event.ctrlKey && !event.metaKey
                            && !event.shiftKey && !event.altKey
                            && (event.preventDefault() || true)
                        """);
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

    protected Span createDate(String activityDate) {
        Span dateSpan = new Span(activityDate);
        dateSpan.addClassName(DATE_CN);
        return dateSpan;
    }

    /**
     * Outer row that lets the link body grow and parks an optional delete
     * button alongside it — outside the link, so deleting is not a click on
     * the conversation.
     *
     * @param body the link body of the card
     * @return the assembled outer row
     */
    protected HorizontalLayout createRow(Anchor body) {
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
