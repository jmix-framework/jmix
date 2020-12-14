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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.ui.CssLayout;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Container for {@link JmixTagLabel}.
 *
 * @param <V> value type
 */
public class JmixTagContainer<V> extends CssLayout {

    public static final String TAGCONTAINER_STYLENAME = "jmix-tagcontainer";
    public static final String TAGCONTAINER_READONLY_STYLENAME = "readonly";

    public static final String TAGCONTAINER_INLINE_STYLENAME = "inline";
    public static final String TAGCONTAINER_VERTICAL_STYLENAME = "vertical";

    protected List<V> items = new ArrayList<>();

    protected Function<V, String> tagCaptionProvider;
    protected Function<? super V, String> tagStyleProvider;
    protected Consumer<V> tagClickHandler;
    protected Consumer<V> removeTagHandler;

    protected Comparator<? super V> tagComparator;

    protected boolean editable = true;

    protected BiMap<V, JmixTagLabel> itemComponent = HashBiMap.create();

    public JmixTagContainer() {
        setPrimaryStyleName(TAGCONTAINER_STYLENAME);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            this.editable = editable;

            removeStyleName(TAGCONTAINER_READONLY_STYLENAME);

            if (!this.editable) {
                addStyleName(TAGCONTAINER_READONLY_STYLENAME);
            }

            for (JmixTagLabel label : itemComponent.inverse().keySet()) {
                label.setEditable(this.editable);
            }
        }
    }

    public boolean isInlineTags() {
        return getStyleName().contains(TAGCONTAINER_INLINE_STYLENAME);
    }

    public void setInlineTags(boolean inline) {
        removeStyleName(TAGCONTAINER_INLINE_STYLENAME);
        removeStyleName(TAGCONTAINER_VERTICAL_STYLENAME);

        addStyleName(inline
                ? TAGCONTAINER_INLINE_STYLENAME
                : TAGCONTAINER_VERTICAL_STYLENAME);
    }

    public void setTagCaptionProvider(Function<V, String> tagCaptionProvider) {
        this.tagCaptionProvider = tagCaptionProvider;
    }

    public @Nullable
    Function<? super V, String> getTagStyleProvider() {
        return tagStyleProvider;
    }

    public void setTagStyleProvider(@Nullable Function<? super V, String> tagStyleProvider) {
        if (this.tagStyleProvider != tagStyleProvider) {
            this.tagStyleProvider = tagStyleProvider;

            for (Map.Entry<V, JmixTagLabel> entry : itemComponent.entrySet()) {
                JmixTagLabel label = entry.getValue();
                label.setStyleName(tagStyleProvider == null
                        ? null
                        : tagStyleProvider.apply(entry.getKey()));
            }
        }
    }

    public void setTagClickHandler(@Nullable Consumer<V> tagClickHandler) {
        this.tagClickHandler = tagClickHandler;

        for (JmixTagLabel tagLabel : itemComponent.inverse().keySet()) {
            tagLabel.setClickHandler(this.tagClickHandler == null ? null : this::onTagLabelClick);
        }
    }

    public void setRemoveTagHandler(Consumer<V> removeTagHandler) {
        this.removeTagHandler = removeTagHandler;
    }

    @Nullable
    public Comparator<? super V> getTagComparator() {
        return tagComparator;
    }

    public void setTagComparator(@Nullable Comparator<? super V> comparator) {
        if (this.tagComparator != comparator) {
            this.tagComparator = comparator;

            refreshTags();
        }
    }

    public void showTags(@Nullable Collection<V> itemsToShow) {
        items = itemsToShow == null ? Collections.emptyList() : new ArrayList<>(itemsToShow);

        doRefreshTags(items);
    }

    public void refreshTags() {
        doRefreshTags(items);
    }

    protected void doRefreshTags(Collection<V> itemsToShow) {
        if (!itemsToShow.isEmpty() && tagComparator != null) {
            itemsToShow = itemsToShow.stream()
                    .sorted(tagComparator)
                    .collect(Collectors.toList());
        }

        removeAllComponents();

        Set<V> usedItems = new HashSet<>();

        for (V item : itemsToShow) {
            JmixTagLabel label = itemComponent.get(item);

            if (label == null) {
                label = createTagLabel();
                itemComponent.put(item, label);
            }

            label.setText(tagCaptionProvider != null
                    ? tagCaptionProvider.apply(item)
                    : item.toString());

            label.setStyleName(tagStyleProvider == null
                    ? null
                    : tagStyleProvider.apply(item));

            addComponent(label);
            usedItems.add(item);
        }

        for (V item : new ArrayList<>(itemComponent.keySet())) {
            if (!usedItems.contains(item)) {
                itemComponent.remove(item);
            }
        }
    }

    protected JmixTagLabel createTagLabel() {
        JmixTagLabel label = new JmixTagLabel();
        label.setEditable(isEditable());
        label.setRemoveHandler(this::onTagLabelRemove);

        if (tagClickHandler != null) {
            label.setClickHandler(this::onTagLabelClick);
        }
        return label;
    }

    protected void onTagLabelRemove(JmixTagLabel label) {
        V item = itemComponent.inverse().get(label);
        if (item != null) {
            items.remove(item);
            itemComponent.remove(item);

            if (removeTagHandler != null) {
                removeTagHandler.accept(item);
            }
        }
    }

    protected void onTagLabelClick(JmixTagLabel label) {
        if (tagClickHandler != null) {
            V item = itemComponent.inverse().get(label);
            if (item != null) {
                tagClickHandler.accept(item);
            }
        }
    }
}
