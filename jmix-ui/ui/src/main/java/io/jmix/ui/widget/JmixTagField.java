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

import com.google.common.base.Strings;
import com.vaadin.server.KeyMapper;
import elemental.json.Json;
import elemental.json.JsonObject;
import io.jmix.ui.widget.client.tagfield.JmixTagFieldClientRpc;
import io.jmix.ui.widget.client.tagfield.JmixTagFieldServerRpc;
import io.jmix.ui.widget.client.tagfield.JmixTagFieldState;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class JmixTagField<V> extends JmixAbstractSuggestionField<Collection<V>, V> {

    protected static final String TAG_CAPTION_KEY = "caption";
    protected static final String TAG_STYLE_KEY = "style";
    protected static final String TAG_KEY = "key";

    protected final KeyMapper<V> valueKeyMapper = new KeyMapper<>();

    protected Function<? super V, String> tagStyleProvider;
    protected Function<? super V, String> tagCaptionProvider;
    protected Comparator<? super V> tagComparator;

    protected Consumer<V> tagClickHandler;

    public JmixTagField() {
        registerRpc(new JmixTagFieldServerRpc() {
            @Override
            public void onTagClick(String tagKey) {
                JmixTagField.this.onTagClick(tagKey);
            }

            @Override
            public void onTagRemove(String tagKey) {
                JmixTagField.this.onTagRemove(tagKey);
            }

            @Override
            public void clearItems() {
                setValue(Collections.emptyList(), true);
            }
        });
    }

    public Function<? super V, String> getTagStyleProvider() {
        return tagStyleProvider;
    }

    public void setTagStyleProvider(@Nullable Function<? super V, String> tagStyleProvider) {
        this.tagStyleProvider = tagStyleProvider;
    }

    @Nullable
    public Function<? super V, String> getTagCaptionProvider() {
        return tagCaptionProvider;
    }

    public void setTagCaptionProvider(@Nullable Function<? super V, String> tagCaptionProvider) {
        this.tagCaptionProvider = tagCaptionProvider;
    }

    @Nullable
    public Comparator<? super V> getTagComparator() {
        return tagComparator;
    }

    public void setTagComparator(@Nullable Comparator<? super V> tagComparator) {
        this.tagComparator = tagComparator;

        refreshTags(getValue());
    }

    @Nullable
    public Consumer<V> getTagClickHandler() {
        return tagClickHandler;
    }

    public void setTagClickHandler(@Nullable Consumer<V> tagClickHandler) {
        this.tagClickHandler = tagClickHandler;

        getState().tagsClickable = tagClickHandler != null;
    }

    public boolean isClearAllVisible() {
        return getState(false).clearAllVisible;
    }

    public void setClearAllVisible(boolean visible) {
        getState().clearAllVisible = visible;
    }

    public void clearText() {
        getRpcProxy(JmixTagFieldClientRpc.class).clearText();
    }

    @Override
    protected void onSelectSuggestion(String suggestionId) {
        V suggestion = keyMapper.get(suggestionId);

        Collection<V> value = getValue();
        if (isNotEmpty(value) && value.contains(suggestion)) {
            return;
        }

        Collection<V> newValue = new ArrayList<>(value == null
                ? Collections.emptyList()
                : value);

        newValue.add(suggestion);

        setValue(newValue, true);
    }

    @Override
    protected void doSetValue(Collection<V> value) {
        super.doSetValue(value);

        refreshTags(value);
    }

    @Override
    protected JmixTagFieldState getState() {
        return (JmixTagFieldState) super.getState();
    }

    @Override
    protected JmixTagFieldState getState(boolean markAsDirty) {
        return (JmixTagFieldState) super.getState(markAsDirty);
    }

    protected List<JsonObject> convertItemsToJson(@Nullable Collection<V> value) {
        if (CollectionUtils.isEmpty(value)) {
            return Collections.emptyList();
        }

        List<JsonObject> items = new ArrayList<>(value.size());
        for (V v : value) {
            JsonObject json = getTagJson(v);
            items.add(json);
        }
        return items;
    }

    protected JsonObject getTagJson(V item) {
        JsonObject json = Json.createObject();
        json.put(TAG_KEY, valueKeyMapper.key(item));

        String caption;
        if (tagCaptionProvider != null) {
            caption = tagCaptionProvider.apply(item);
        } else if (textViewConverter != null) {
            caption = textViewConverter.apply(item);
        } else {
            caption = item.toString();
        }
        json.put(TAG_CAPTION_KEY, Strings.nullToEmpty(caption));

        if (tagStyleProvider != null) {
            json.put(TAG_STYLE_KEY, Strings.nullToEmpty(tagStyleProvider.apply(item)));
        }

        return json;
    }

    protected void onTagClick(String tagKey) {
        if (tagClickHandler != null) {
            V item = valueKeyMapper.get(tagKey);
            tagClickHandler.accept(item);
        }
    }

    protected void onTagRemove(String tagKey) {
        V item = valueKeyMapper.get(tagKey);

        List<V> newValue = new ArrayList<>(getValue() == null
                ? Collections.emptyList()
                : getValue());

        newValue.remove(item);

        setValue(newValue, true);
    }

    protected void refreshTags(@Nullable Collection<V> value) {
        if (tagComparator != null && isNotEmpty(value)) {
            value = value.stream()
                    .sorted(tagComparator)
                    .collect(Collectors.toList());
        }

        getState().items = convertItemsToJson(value);
    }

}
