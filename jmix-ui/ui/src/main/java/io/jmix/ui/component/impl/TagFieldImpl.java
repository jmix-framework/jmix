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

package io.jmix.ui.component.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.TagField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.widget.JmixTagField;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TagFieldImpl<V> extends AbstractSuggestionField<Collection<V>, V, JmixTagField<V>>
        implements TagField<V> {

    public TagFieldImpl() {
        component = createComponent();

        attachValueChangeListener(component);
    }

    protected JmixTagField<V> createComponent() {
        return new JmixTagField<>();
    }

    @Override
    public boolean isClearAllVisible() {
        return component.isClearAllVisible();
    }

    @Override
    public void setClearAllVisible(boolean visible) {
        component.setClearAllVisible(visible);
    }

    @Override
    public Function<? super V, String> getTagStyleProvider() {
        return component.getTagStyleProvider();
    }

    @Override
    public void setTagStyleProvider(@Nullable Function<? super V, String> tagStyleProvider) {
        component.setTagStyleProvider(tagStyleProvider);
    }

    @Override
    public Function<? super V, String> getTagCaptionProvider() {
        return component.getTagCaptionProvider();
    }

    @Override
    public void setTagCaptionProvider(@Nullable Function<? super V, String> tagCaptionProvider) {
        component.setTagCaptionProvider(tagCaptionProvider);
    }

    @Nullable
    @Override
    public Comparator<? super V> getTagComparator() {
        return component.getTagComparator();
    }

    @Override
    public void setTagComparator(@Nullable Comparator<? super V> tagComparator) {
        component.setTagComparator(tagComparator);
    }

    @Override
    public Subscription addTagClickListener(Consumer<TagClickEvent<V>> listener) {
        if (!getEventHub().hasSubscriptions(TagClickEvent.class)) {
            component.setTagClickHandler(this::onTagClick);
        }

        getEventHub().subscribe(TagClickEvent.class, (Consumer) listener);

        return removeTagClickListener(listener);
    }

    protected Subscription removeTagClickListener(Consumer<TagClickEvent<V>> listener) {
        return () -> {
            getEventHub().unsubscribe(TagClickEvent.class, (Consumer) listener);

            if (!getEventHub().hasSubscriptions(TagClickEvent.class)) {
                getComponent().setTagClickHandler(null);
            }
        };
    }

    @Override
    protected boolean fieldValueEquals(@Nullable Collection<V> value, @Nullable Collection<V> oldValue) {
        return equalCollections(value, oldValue);
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

    protected void onTagClick(V item) {
        publish(TagClickEvent.class, new TagClickEvent<>(this, item));
    }

    @Override
    public void setValueFromUser(@Nullable Collection<V> value) {
        setValueToPresentation(convertToPresentation(value));

        Collection<V> oldValue = internalValue;
        this.internalValue = value;

        if (!fieldValueEquals(value, oldValue)) {
            ValueChangeEvent<Collection<V>> event = new ValueChangeEvent<>(this, oldValue, value, true);
            publish(ValueChangeEvent.class, event);
        }

        component.clearText();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                || CollectionUtils.isEmpty(getValue());
    }

    @Override
    protected Collection<V> convertToModel(@Nullable Collection<V> componentRawValue) throws ConversionException {
        if (valueBinding != null) {
            Class<?> collectionType = valueBinding.getSource().getType();

            if (Set.class.isAssignableFrom(collectionType)) {
                return CollectionUtils.isEmpty(componentRawValue)
                        ? Collections.emptySet()
                        : new LinkedHashSet<>(componentRawValue);
            }
        }

        return CollectionUtils.isEmpty(componentRawValue)
                ? Collections.emptyList()
                : new ArrayList<>(componentRawValue);
    }
}
