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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.action.tagpicker.TagLookupAction;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.Comparator;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The field that shows selected values as separated closable tags which are placed near to the field.
 * The field also contains a set of buttons defined by actions.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "TagPicker",
        category = "Components",
        xmlElement = "tagPicker",
        icon = "io/jmix/ui/icon/component/tagPicker.svg",
        canvasBehaviour = CanvasBehaviour.COMBO_BOX,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/tag-picker.html",
        unsupportedProperties = {"fieldEditable", "optionsEnum"}
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "optionsContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF,
                        typeParameter = "V")
        }
)
public interface TagPicker<V> extends ValuesPicker<V>, OptionsField<Collection<V>, V>, HasInputPrompt, HasFilterMode {

    String NAME = "tagPicker";

    /**
     * @return tag caption provider or {@code null} if not set
     */
    @Nullable
    Function<? super V, String> getTagCaptionProvider();

    /**
     * Sets caption provider. It is used for generating tag caption.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "tagPicker", subject = "tagCaptionProvider")
     * private String tagPickerCaptionProvider(User user) {
     *     return "User: " + user.getUsername();
     * }
     * </pre>
     *
     * @param tagCaptionProvider caption provider to set
     */
    void setTagCaptionProvider(@Nullable Function<? super V, String> tagCaptionProvider);

    /**
     * Sets tag click listener. It subscribes to {@link TagClickEvent} that is invoked when
     * user clicks on tag content.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addTagClickListener(Consumer<TagClickEvent<V>> listener);

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
     * &#64;Install(to = "tagPicker", subject = "tagStyleProvider")
     * private String tagPickerTagStyleProvider(User user) {
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
     * @return {@code true} if tags should be placed inline
     */
    boolean isInlineTags();

    /**
     * Defines how tags should be oriented in the field:
     * <ul>
     *     <li>inline = true - tags will be shown one after another in one line if it will be enough available space.
     *     Otherwise, the next tags will be moved to another line. The width of the tag is {@code AUTO}.</li>
     *     <li>inline = false - tags will be shown under each other. Tag occupies all available space
     *     on the line.</li>
     * </ul>
     * The default value is {@code false}.
     *
     * @param inline whether tags should be placed inline
     */
    @StudioProperty(name = "inlineTags", defaultValue = "false")
    void setInlineTags(boolean inline);

    /**
     * @return position of tags relative to the field
     */
    TagPosition getTagPosition();

    /**
     * Sets position where tags should be placed relative to the field.
     * <ul>
     *     <li>{@link TagPosition#TOP} - tags are placed above the field.</li>
     *     <li>{@link TagPosition#RIGHT} - tags are placed to the right of the field.</li>
     *     <li>{@link TagPosition#BOTTOM} - tags are placed under the field.</li>
     *     <li>{@link TagPosition#LEFT} - tags are placed to the left of the field.</li>
     * </ul>
     * The default value is {@link TagPosition#BOTTOM}.
     *
     * @param position position to set
     */
    @StudioProperty(name = "tagPosition", type = PropertyType.ENUMERATION, defaultValue = "BOTTOM",
            options = {"TOP", "RIGHT", "BOTTOM", "LEFT"})
    void setTagPosition(TagPosition position);

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
     * @return entity meta class or {@code null}
     */
    @Nullable
    MetaClass getMetaClass();

    /**
     * Sets entity meta class to the field. It enables to use field with entity type without data container
     * and use {@link TagLookupAction}.
     *
     * @param metaClass entity meta class
     */
    @StudioProperty(name = "metaClass", type = PropertyType.ENTITY_NAME, typeParameter = "V")
    void setMetaClass(MetaClass metaClass);

    /**
     * @return the page length of the suggestion popup.
     */
    int getPageLength();

    /**
     * Sets the page length for the suggestion popup. Setting the page length to
     * 0 will disable suggestion popup paging (all items visible).
     *
     * @param pageLength the pageLength to set
     */
    @StudioProperty(defaultValue = "10")
    @PositiveOrZero
    void setPageLength(int pageLength);

    /**
     * @return is selected options is hidden from the dropdown list
     */
    boolean isHideSelectedOptions();

    /**
     * Sets whether the field should hide options from the dropdown list if they have already selected.
     * The default value is {@code true}.
     *
     * @param hide {@code true} if selected options should be hidden, {@code false} otherwise
     */
    @StudioProperty(name = "hideSelectedOptions", defaultValue = "false")
    void setHideSelectedOptions(boolean hide);

    /**
     * The interface that marks implementation as {@link TagPicker}'s action.
     *
     * @see TagLookupAction
     */
    interface TagPickerAction extends ValuePicker.ValuePickerAction {

        String PROP_MULTISELECT = "multiSelect";

        /**
         * Sets {@link TagPicker} to the action.
         *
         * @param tagPicker field to set
         */
        void setTagPicker(@Nullable TagPicker tagPicker);

        default void setPicker(ValuePicker valuePicker) {
            if (valuePicker != null && !(valuePicker instanceof TagPicker)) {
                throw new IllegalArgumentException("Incorrect component type. Must be " +
                        "'TagPicker' or its inheritors");
            }

            setTagPicker((TagPicker) valuePicker);
        }
    }

    /**
     * Describes tag click event. The event is fired when user clicks on tag content.
     *
     * @param <V> value type
     */
    class TagClickEvent<V> extends EventObject {

        protected final V item;

        public TagClickEvent(TagPicker<V> source, V item) {
            super(source);
            this.item = item;
        }

        @Override
        public TagPicker<V> getSource() {
            return (TagPicker<V>) super.getSource();
        }

        /**
         * @return clicked item
         */
        public V getItem() {
            return item;
        }
    }

    /**
     * Defines position of tags relative to the field.
     */
    enum TagPosition {

        /**
         * Tags are placed above the field.
         */
        TOP,

        /**
         * Tags are placed to the right of the field.
         */
        RIGHT,

        /**
         * Tags are placed under the field.
         */
        BOTTOM,

        /**
         * Tags are placed to the left of the field.
         */
        LEFT;
    }
}
