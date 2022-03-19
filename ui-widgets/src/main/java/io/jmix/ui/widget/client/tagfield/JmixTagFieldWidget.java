/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.widget.client.tagfield;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.StyleConstants;
import com.vaadin.client.ui.VLabel;
import elemental.json.JsonObject;
import io.jmix.ui.widget.client.suggestionfield.JmixSuggestionFieldWidget;
import io.jmix.ui.widget.client.suggestionfield.menu.SuggestionsContainer;
import io.jmix.ui.widget.client.tagpickerlabel.JmixTagLabelWidget;
import io.jmix.ui.widget.client.verticalmenu.FocusableFlowPanel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JmixTagFieldWidget extends JmixSuggestionFieldWidget {

    public static final String CLASSNAME = "jmix-tagfield";
    public static final String LAYOUT = CLASSNAME + "-layout";
    public static final String CLEAR_ALL_BUTTON = CLASSNAME + "-clear-all";
    public static final String FOCUS = "focus";
    public static final String EMPTY = "empty";
    public static final String CLEAR_ALL = "clear-all";
    public static final String SINGLELINE = "singleline";
    public static final String TEXT_OVERFLOW_TAGLABEL = "text-overflow";

    protected static final String TAG_CAPTION_KEY = "caption";
    protected static final String TAG_STYLE_KEY = "style";
    protected static final String TAG_KEY = "key";

    protected FocusableFlowPanel root;
    protected FlowPanel layout;
    protected VLabel clearAllBtn;

    protected Map<JmixTagLabelWidget, String> tagKeyMap;

    protected Consumer<String> tagClickHandler;
    protected Consumer<String> tagRemoveHandler;
    protected Runnable clearItemHandler;

    protected boolean clickableTag = false;
    protected boolean clearAllVisible = false;

    public JmixTagFieldWidget() {
        initClearAllButton();

        addWindowResizeHandler();
    }

    @Override
    protected void setupComposition() {
        root = GWT.create(FocusableFlowPanel.class);

        JmixTagFieldEvents handler = new JmixTagFieldEvents();
        root.addDomHandler(handler, FocusEvent.getType());
        root.addDomHandler(handler, BlurEvent.getType());
        root.addDomHandler(handler, ClickEvent.getType());

        layout = GWT.create(FocusableFlowPanel.class);
        layout.getElement().removeAttribute("tabIndex");
        layout.addDomHandler(new JmixLayoutTagFieldEvents(), ClickEvent.getType());
        layout.setStyleName(StyleConstants.UI_WIDGET);
        layout.addStyleName(LAYOUT);

        layout.add(textField);
        root.add(layout);

        root.getElement().removeAttribute("tabIndex");
        root.setStyleName(CLASSNAME);

        initWidget(root);
    }

    @Override
    protected void initTextField() {
        super.initTextField();

        textField.addStyleName(StyleConstants.UI_WIDGET);
    }

    protected void addClearAllButton() {
        if (clearAllVisible
                && isActive()
                && (tagKeyMap != null && !tagKeyMap.isEmpty())) {
            layout.add(clearAllBtn);
        }
    }

    protected void initClearAllButton() {
        clearAllBtn = GWT.create(VLabel.class);
        clearAllBtn.setStyleName(CLEAR_ALL_BUTTON);
        clearAllBtn.addStyleName(StyleConstants.UI_WIDGET);
        clearAllBtn.addClickHandler(event -> {
            if (isActive() && clearItemHandler != null) {
                clearItemHandler.run();
            }
        });
    }

    protected void addWindowResizeHandler() {
        Timer resizeTimer = new Timer() {
            @Override
            public void run() {
                updateWidgetHeightStyle();
            }
        };

        Window.addResizeHandler(event -> resizeTimer.schedule(50));
    }

    @Override
    protected SuggestionPopup createSuggestionPopup(SuggestionsContainer suggestionsContainer) {
        return new TagFieldSuggestionPopup(suggestionsContainer);
    }

    @Override
    public void setReadonly(boolean readonly) {
        super.setReadonly(readonly);

        updateWidgetsAvailability();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        updateWidgetsAvailability();
    }

    protected void updateWidgetsAvailability() {
        textField.setVisible(isActive());

        getElement().removeAttribute("tabIndex");

        // if textField is not visible, root should handle focus/blur
        if (isEnabled() && isReadonly()) {
            getElement().setAttribute("tabIndex", String.valueOf(getTabIndex()));
        }

        for (int i = 0; i < layout.getWidgetCount(); i++) {
            Widget widget = layout.getWidget(i);

            if (widget instanceof JmixTagLabelWidget) {
                widget.removeStyleName(StyleConstants.DISABLED);
                if (!isEnabled()) {
                    widget.addStyleName(StyleConstants.DISABLED);
                }
            }
        }
    }

    @Nullable
    public Consumer<String> getTagClickHandler() {
        return tagClickHandler;
    }

    public void setTagClickHandler(@Nullable Consumer<String> tagClickHandler) {
        this.tagClickHandler = tagClickHandler;
    }

    @Nullable
    public Consumer<String> getTagRemoveHandler() {
        return tagRemoveHandler;
    }

    public void setTagRemoveHandler(@Nullable Consumer<String> tagRemoveHandler) {
        this.tagRemoveHandler = tagRemoveHandler;
    }

    public boolean isClickableTag() {
        return clickableTag;
    }

    public void setClickableTag(boolean clickableTag) {
        this.clickableTag = clickableTag;
    }

    public Runnable getClearItemHandler() {
        return clearItemHandler;
    }

    public void setClearItemHandler(@Nullable Runnable clearItemHandler) {
        this.clearItemHandler = clearItemHandler;
    }

    public boolean isClearAllVisible() {
        return clearAllVisible;
    }

    public void setClearAllVisible(boolean clearAllVisible) {
        if (this.clearAllVisible != clearAllVisible) {
            this.clearAllVisible = clearAllVisible;

            if (clearAllVisible) {
                addClearAllButton();
                addStyleName(CLEAR_ALL);
            } else {
                removeStyleName(CLEAR_ALL);
                layout.remove(layout.getWidgetCount() - 1);
            }
        }
    }

    public void clearText() {
        setValue(null);
    }

    public void setItems(@Nullable List<JsonObject> items) {
        layout.clear();

        if (tagKeyMap == null) {
            tagKeyMap = new HashMap<>();
        } else {
            tagKeyMap.clear();
        }

        if (items == null || items.isEmpty()) {
            addStyleName(EMPTY);
        } else {
            removeStyleName(EMPTY);

            for (JsonObject json : items) {
                JmixTagLabelWidget tagLabel = generateTagLabel(json);
                tagKeyMap.put(tagLabel, json.getString(TAG_KEY));

                tagLabel.setItemClickHandler(() -> {
                    onTagClick(tagKeyMap.get(tagLabel));
                });
                tagLabel.setRemoveItemHandler(() -> {
                    onTagRemove(tagKeyMap.get(tagLabel));
                });

                layout.add(tagLabel);
            }
        }

        layout.add(textField);
        addClearAllButton();

        updateWidgetsAvailability();
        updateWidgetsWidth();

        textField.setFocus(true);
    }

    protected JmixTagLabelWidget generateTagLabel(JsonObject json) {
        JmixTagLabelWidget tagLabel = GWT.create(JmixTagLabelWidget.class);
        tagLabel.setText(json.getString(TAG_CAPTION_KEY));
        tagLabel.setEditable(!isReadonly());
        tagLabel.setClickable(clickableTag);
        tagLabel.addStyleName(StyleConstants.UI_WIDGET);

        if (json.hasKey(TAG_STYLE_KEY)) {
            tagLabel.addStyleDependentName(json.getString(TAG_STYLE_KEY));
            tagLabel.addStyleName(json.getString(TAG_STYLE_KEY));
        }

        return tagLabel;
    }

    protected void onTagClick(String tagKey) {
        if (tagClickHandler != null) {
            tagClickHandler.accept(tagKey);
        }
    }

    protected void onTagRemove(String tagKey) {
        if (tagRemoveHandler != null) {
            tagRemoveHandler.accept(tagKey);
        }
    }

    public boolean updateWidgetHeightStyle() {
        if (!getStyleName().contains(SINGLELINE)) {
            addStyleName(SINGLELINE);
        }

        if (getElement().getScrollHeight() > getElement().getOffsetHeight()) {
            removeStyleName(SINGLELINE);
            return true;
        }
        return false;
    }

    protected void updateWidgetsWidth() {
        for (int i = 0; i < layout.getWidgetCount(); i++) {
            Widget widget = layout.getWidget(i);

            if (widget instanceof JmixTagLabelWidget) {
                if (isTagLabelExceedFieldWidth((JmixTagLabelWidget) widget)) {
                    if (!widget.getStyleName().contains(TEXT_OVERFLOW_TAGLABEL)) {
                        widget.addStyleName(TEXT_OVERFLOW_TAGLABEL);
                    }
                }
            }
        }
    }

    protected boolean isTagLabelExceedFieldWidth(JmixTagLabelWidget label) {
        int fieldRight = getElement().getAbsoluteRight();

        if (isClearAllVisible()) {
            int closeAllWidth = clearAllBtn.getElement().getOffsetWidth();
            fieldRight = fieldRight - closeAllWidth;
        }

        ComputedStyle computedStyle = new ComputedStyle(label.getElement());
        int[] margin = computedStyle.getMargin();

        int labelRight = label.getElement().getAbsoluteRight() + margin[1]; // right margin

        return labelRight > fieldRight;
    }

    /**
     * Handle TextField blur
     *
     * @param event blur event
     */
    @Override
    protected void handleOnBlur(BlurEvent event) {
        super.handleOnBlur(event);

        root.removeStyleDependentName(FOCUS);
    }

    /**
     * Handle TextField focus
     *
     * @param event focus event
     */
    @Override
    protected void handleOnFocus(FocusEvent event) {
        super.handleOnFocus(event);

        root.addStyleDependentName(FOCUS);
    }

    /**
     * As widget hides in some cases (readOnly, disabled) TextField that handle blur/focus,
     * TagFieldWidget should handle blur/focus itself.
     */
    protected class JmixTagFieldEvents implements FocusHandler, BlurHandler, ClickHandler {

        @Override
        public void onFocus(FocusEvent event) {
            root.addStyleDependentName(FOCUS);
        }

        @Override
        public void onBlur(BlurEvent event) {
            root.removeStyleDependentName(FOCUS);
        }

        @Override
        public void onClick(ClickEvent event) {
            Element element = event.getNativeEvent().getEventTarget().cast();
            if (element.isOrHasChild(getElement())) {
                textField.setFocus(true);
            }
        }
    }

    protected class JmixLayoutTagFieldEvents implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            Element element = event.getNativeEvent().getEventTarget().cast();
            if (element.isOrHasChild(layout.getElement())) {
                textField.setFocus(true);
            }
        }
    }

    protected class TagFieldSuggestionPopup extends SuggestionPopup {

        public TagFieldSuggestionPopup(Widget widget) {
            super(widget);

            setOwner(JmixTagFieldWidget.this);
        }

        @Override
        protected Widget getRelativeWidget() {
            return root;
        }
    }
}
