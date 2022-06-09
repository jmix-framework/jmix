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

package io.jmix.ui.component;

import com.google.common.base.Strings;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The field that search and shows selected values as separated tags right in the field.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "TagField",
        category = "Components",
        xmlElement = "tagField",
        canvasBehaviour = CanvasBehaviour.INPUT_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/tag-field.html",
        icon = "io/jmix/ui/icon/component/tagField.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface TagField<V> extends SuggestionFieldComponent<Collection<V>, V>, SupportsUserAction<Collection<V>> {

    String NAME = "tagField";

    static <T> ParameterizedTypeReference<TagField<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<TagField<T>>() {
        };
    }

    @StudioElement
    @Override
    void setSearchExecutor(@Nullable SearchExecutor<V> searchExecutor);

    /**
     * @return {code true} if clear button is visible
     */
    boolean isClearAllVisible();

    /**
     * Display the button that clears all tags from the field. The default value is {@code false}.
     *
     * @param visible whether to display clear button
     */
    @StudioProperty(name = "clearAllVisible", defaultValue = "false")
    void setClearAllVisible(boolean visible);

    /**
     * @return tag style provider or {@code null} if not set
     */
    @Nullable
    Function<? super V, String> getTagStyleProvider();

    /**
     * Sets tag style provider. It is used for generating style name for tag.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "tagField", subject = "tagStyleProvider")
     * private String tagFieldTagStyleProvider(User user) {
     *     switch (user.getAccountType()) {
     *         case PREMIUM:
     *             return "user-premium";
     *         case COMMON:
     *             return "user-common";
     *         default:
     *             return null;
     *     }
     * }
     * </pre>
     *
     * @param tagStyleProvider tag style provider to set
     */
    void setTagStyleProvider(@Nullable Function<? super V, String> tagStyleProvider);

    /**
     * @return tag caption provider or {@code null} if not set
     */
    @Nullable
    Function<? super V, String> getTagCaptionProvider();

    /**
     * Sets tag caption provider. It is used for generating tag caption.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "tagField", subject = "tagCaptionProvider")
     * private String tagFieldCaptionProvider(User user) {
     *     return "User: " + user.getUsername();
     * }
     * </pre>
     *
     * @param tagCaptionProvider caption provider to set
     */
    void setTagCaptionProvider(@Nullable Function<? super V, String> tagCaptionProvider);

    /**
     * @return tag comparator
     */
    @Nullable
    Comparator<? super V> getTagComparator();

    /**
     * Sets comparator for sorting tags in the UI.
     *
     * @param tagComparator comparator to set
     */
    void setTagComparator(@Nullable Comparator<? super V> tagComparator);

    /**
     * Sets tag click listener. It subscribes to {@link TagClickEvent} that is invoked when
     * user clicks on tag content.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addTagClickListener(Consumer<TagClickEvent<V>> listener);

    /**
     * Describes tag click event. The event is fired when user clicks on tag content.
     *
     * @param <V> type of value
     */
    class TagClickEvent<V> extends EventObject {

        protected V item;

        public TagClickEvent(TagField<V> source, V item) {
            super(source);

            this.item = item;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TagField<V> getSource() {
            return (TagField<V>) super.getSource();
        }

        /**
         * @return clicked item
         */
        public V getItem() {
            return item;
        }
    }

    /**
     * Enables to handle a user's entered text and add it to a TagField value.
     * <p>
     * For instance:
     * <pre>
     * tagField.setEnterActionHandler(new TagField.NewTagProvider&lt;ServiceType&gt;() {
     *     &#64;Nullable
     *     &#64;Override
     *     public ServiceType create(String text) {
     *         ServiceType serviceType = dataManager.create(ServiceType.class);
     *         serviceType.setName(text);
     *         return serviceType;
     *     }
     * });
     * </pre>
     *
     * @param <V> type of item
     */
    abstract class NewTagProvider<V> implements Consumer<EnterPressEvent> {

        @Override
        public void accept(EnterPressEvent event) {
            if (Strings.isNullOrEmpty(event.getText())) {
                return;
            }

            V item = create(event.getText());

            if (item != null && event.getSource() instanceof TagField) {
                TagField<V> tagField = (TagField<V>) event.getSource();

                Collection<V> newValue = new ArrayList<>(tagField.getValue() == null
                        ? Collections.emptyList()
                        : tagField.getValue());
                newValue.add(item);

                tagField.setValueFromUser(newValue);
            }
        }

        /**
         * Provides ability to create new item from entered text.
         * Method is invoked when user press ENTER key.
         *
         * @param text user's entered text
         * @return new item
         */
        @Nullable
        public abstract V create(String text);
    }
}
