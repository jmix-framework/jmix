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

package io.jmix.ui.widget;

import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class JmixTagPicker<V> extends JmixComboBoxPickerField<Collection<V>> {

    public static final String TAGPICKER_STYLENAME = "jmix-tagpicker";

    public static final String TAGCONTAINER_COMPOSITION_STYLENAME = "jmix-tagpicker-composition";

    public static final String TAGS_TOP_STYLENAME = "tags-top";
    public static final String TAGS_RIGHT_STYLENAME = "tags-right";
    public static final String TAGS_BOTTOM_STYLENAME = "tags-bottom";
    public static final String TAGS_LEFT_STYLENAME = "tags-left";

    public enum TagContainerPosition {
        TOP, RIGHT, BOTTOM, LEFT;
    }

    protected CssLayout composition;
    protected JmixTagContainer<V> tagContainer;

    protected TagContainerPosition containerPosition = TagContainerPosition.BOTTOM;

    public JmixTagPicker() {
        addStyleName(TAGPICKER_STYLENAME);
        addStyleName(TAGS_BOTTOM_STYLENAME);

        updateTagContainerVisibility();
    }

    @Override
    protected void initLayout() {
        super.initLayout();

        tagContainer = new JmixTagContainer<>();
        tagContainer.setWidth(100, Unit.PERCENTAGE);
        tagContainer.setInlineTags(false);
        tagContainer.setRemoveTagHandler(this::onTagLabelRemove);

        composition = new CssLayout();
        composition.setWidth(100, Unit.PERCENTAGE);
        composition.setStyleName(TAGCONTAINER_COMPOSITION_STYLENAME);

        composition.addComponent(container); // contains actions layout with ComboBox
        composition.addComponent(tagContainer);
    }

    @Override
    protected void initField() {
        super.initField();

        getFieldInternal().setEmptySelectionAllowed(false);
    }

    @Override
    protected Component initContent() {
        return composition;
    }

    @Override
    protected void onFieldValueChange(ValueChangeEvent<?> event) {
        super.onFieldValueChange(event);

        V itemValue = (V) event.getValue();
        if (itemValue == null) {
            return;
        }

        List<V> newValue = null;

        if (internalValue == null) {
            newValue = new ArrayList<>();
            newValue.add(itemValue);
        } else if (!internalValue.contains(itemValue)) {
            newValue = new ArrayList<>(internalValue);
            newValue.add(itemValue);
        }

        if (newValue != null) {
            setValue(newValue, event.isUserOriginated());
        }

        // clear input
        getFieldInternal().setValue(null);
    }

    @Override
    public Collection<V> getValue() {
        return internalValue;
    }

    @Override
    protected void doSetValue(Collection<V> value) {
        internalValue = value;

        refreshTags();

        updateTagContainerVisibility();

        markAsDirty();
    }

    @Override
    protected boolean isDifferentValue(Collection<V> newValue) {
        return !equalCollections(getValue(), newValue);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<Collection<V>> listener) {
        return addListener(ValueChangeEvent.class, listener, ValueChangeListener.VALUE_CHANGE_METHOD);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        tagContainer.setEditable(!readOnly);
        field.setVisible(!readOnly);
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(TAGPICKER_STYLENAME
                + " " + getTagContainerPositionStyle(getTagContainerPosition())
                + " " + removeComponentStyles(style));
    }

    public boolean isInlineTags() {
        return tagContainer.isInlineTags();
    }

    public void setInlineTags(boolean inline) {
        tagContainer.setInlineTags(inline);
    }

    public TagContainerPosition getTagContainerPosition() {
        return containerPosition;
    }

    public void setTagContainerPosition(TagContainerPosition containerPosition) {
        if (this.containerPosition != containerPosition) {
            this.containerPosition = containerPosition;
            removeStyleNames(TAGS_TOP_STYLENAME, TAGS_RIGHT_STYLENAME, TAGS_BOTTOM_STYLENAME, TAGS_LEFT_STYLENAME);

            composition.removeComponent(tagContainer);

            switch (this.containerPosition) {
                case TOP:
                    composition.addComponent(tagContainer, 0);
                    addStyleName(TAGS_TOP_STYLENAME);
                    break;
                case RIGHT:
                    composition.addComponent(tagContainer);
                    addStyleName(TAGS_RIGHT_STYLENAME);
                    break;
                case BOTTOM:
                    composition.addComponent(tagContainer);
                    addStyleName(TAGS_BOTTOM_STYLENAME);
                    break;
                case LEFT:
                    composition.addComponent(tagContainer, 0);
                    addStyleName(TAGS_LEFT_STYLENAME);
            }

            updateTagContainerVisibility();
        }
    }

    public void refreshTags() {
        tagContainer.showTags(internalValue);
    }

    public void setTagCaptionProvider(Function<V, String> tagCaptionProvider) {
        tagContainer.setTagCaptionProvider(tagCaptionProvider);
    }

    public void setTagClickHandler(@Nullable Consumer<V> tagClickHandler) {
        tagContainer.setTagClickHandler(tagClickHandler);
    }

    @Nullable
    public Function<? super V, String> getTagStyleProvider() {
        return tagContainer.getTagStyleProvider();
    }

    public void setTagStyleProvider(@Nullable Function<? super V, String> tagStyleProvider) {
        tagContainer.setTagStyleProvider(tagStyleProvider);
    }

    @Nullable
    public Comparator<? super V> getTagComparator() {
        return tagContainer.getTagComparator();
    }

    public void setTagComparator(@Nullable Comparator<? super V> tagComparator) {
        tagContainer.setTagComparator(tagComparator);
    }

    protected void updateTagContainerVisibility() {
        switch (containerPosition) {
            case TOP:
            case RIGHT:
            case BOTTOM:
                tagContainer.setVisible(tagContainer.getComponentCount() > 0);
                break;
            case LEFT:
                // always show tagContainer as field moves to the available space to the left
                tagContainer.setVisible(true);
        }
    }

    protected void onTagLabelRemove(V item) {
        List<V> newValue = new ArrayList<>(internalValue);
        newValue.remove(item);
        setValue(newValue.isEmpty() ? null : newValue, true);
    }

    protected boolean equalCollections(@Nullable Collection<V> a, @Nullable Collection<V> b) {
        if (CollectionUtils.isEmpty(a) && CollectionUtils.isEmpty(b)) {
            return true;
        }

        if ((CollectionUtils.isEmpty(a) && CollectionUtils.isNotEmpty(b))
                || (CollectionUtils.isNotEmpty(a) && CollectionUtils.isEmpty(b))) {
            return false;
        }

        //noinspection ConstantConditions
        return CollectionUtils.isEqualCollection(a, b);
    }

    protected String getTagContainerPositionStyle(TagContainerPosition containerPosition) {
        if (containerPosition == TagContainerPosition.TOP) {
            return TAGS_TOP_STYLENAME;
        } else if (containerPosition == TagContainerPosition.RIGHT) {
            return TAGS_RIGHT_STYLENAME;
        } else if (containerPosition == TagContainerPosition.LEFT) {
            return TAGS_LEFT_STYLENAME;
        } else {
            return TAGS_BOTTOM_STYLENAME;
        }
    }

    protected String removeComponentStyles(String styleName) {
        String style = super.removeComponentStyles(styleName);
        return style.replaceAll(TAGPICKER_STYLENAME
                + "|" + TAGS_TOP_STYLENAME
                + "|" + TAGS_RIGHT_STYLENAME
                + "|" + TAGS_BOTTOM_STYLENAME
                + "|" + TAGS_LEFT_STYLENAME, "");
    }
}
