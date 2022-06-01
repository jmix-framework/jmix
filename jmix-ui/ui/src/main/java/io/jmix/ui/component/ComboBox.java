/*
 * Copyright 2019 Haulmont.
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

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import javax.validation.constraints.PositiveOrZero;
import java.util.function.Predicate;

/**
 * A filtering dropdown single-select component. Items are filtered based on user input.
 *
 * @param <V> type of options and value
 */
@StudioComponent(
        caption = "ComboBox",
        category = "Components",
        xmlElement = "comboBox",
        icon = "io/jmix/ui/icon/component/comboBox.svg",
        canvasBehaviour = CanvasBehaviour.COMBO_BOX,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/combo-box.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "optionsEnum", type = PropertyType.ENUM_CLASS, typeParameter = "V")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface ComboBox<V> extends OptionsField<V, V>, HasInputPrompt, Buffered,
        Component.Focusable, HasOptionStyleProvider<V>, HasOptionIconProvider<V>, HasOptionImageProvider<V>,
        HasFilterMode, HasEnterPressHandler {

    String NAME = "comboBox";

    ParameterizedTypeReference<ComboBox<String>> TYPE_STRING =
            new ParameterizedTypeReference<ComboBox<String>>() {};

    static <T> ParameterizedTypeReference<ComboBox<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<ComboBox<T>>() {};
    }

    /**
     * @return the null selection caption, not {@code null}
     */
    String getNullSelectionCaption();

    /**
     * Sets the null selection caption.
     * <p>
     * The empty string {@code ""} is the default null selection caption.
     * <p>
     * If null selection is allowed then the null item will be shown with the given caption.
     *
     * @param nullOption the caption to set, not {@code null}
     */
    @StudioProperty(name = "nullName", type = PropertyType.LOCALIZED_STRING)
    void setNullSelectionCaption(String nullOption);

    /**
     * @return true if text input allowed
     */
    boolean isTextInputAllowed();

    /**
     * Sets whether it is possible to input text into the field or whether the field area of the component is just used
     * to show what is selected.
     */
    @StudioProperty(defaultValue = "true")
    void setTextInputAllowed(boolean textInputAllowed);

    /**
     * When enabled popup automatically opens on focus.
     */
    void setAutomaticPopupOnFocus(boolean automaticPopupOnFocus);

    /**
     * @return whether popup is automatically shows on focus.
     */
    boolean isAutomaticPopupOnFocus();

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
     * Sets visibility for first null element in suggestion popup.
     */
    @StudioProperty(defaultValue = "true")
    void setNullOptionVisible(boolean nullOptionVisible);

    /**
     * @return true if first null element is visible.
     */
    boolean isNullOptionVisible();

    /**
     * Returns the suggestion popup's width as a string. By default this
     * width is set to {@code null}.
     *
     * @return explicitly set popup width as size string or null if not set
     */
    @Nullable
    String getPopupWidth();

    /**
     * Sets the suggestion popup's width as a string. By using relative
     * units (e.g. "50%") it's possible to set the popup's width relative to the
     * LookupField itself.
     * <p>
     * By default this width is set to {@code null} so that the popup's width
     * can be greater than a component width to fit the content of all displayed items.
     * By setting width to "100%" the popup's width will be equal to the width of the LookupField.
     *
     * @param width the width
     */
    void setPopupWidth(@Nullable String width);

    /**
     * @return a predicate that tests whether an item with the given caption matches
     * to the given search string.
     */
    @Nullable
    Predicate<OptionsCaptionFilteringContext> getOptionsCaptionFilter();

    /**
     * Sets a predicate that tests whether an item with the given caption matches
     * to the given search string.
     *
     * @param filter a predicate to set
     */
    void setOptionsCaptionFilter(@Nullable Predicate<OptionsCaptionFilteringContext> filter);

    /**
     * Caption filtering context. Can be used to test whether an item with the given caption
     * matches to the given search string.
     */
    class OptionsCaptionFilteringContext {
        protected String itemCaption;
        protected String searchString;

        /**
         * @param itemCaption  the caption of the item to filter, not {@code null}
         * @param searchString the user entered search string, not {@code null}
         */
        public OptionsCaptionFilteringContext(String itemCaption, String searchString) {
            this.itemCaption = itemCaption;
            this.searchString = searchString;
        }

        /**
         * @return the caption of the item to filter, not {@code null}
         */
        public String getItemCaption() {
            return itemCaption;
        }

        /**
         * @return the user entered search string, not {@code null}
         */
        public String getSearchString() {
            return searchString;
        }
    }
}
