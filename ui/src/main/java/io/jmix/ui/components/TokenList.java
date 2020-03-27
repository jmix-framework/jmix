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

package io.jmix.ui.components;

import io.jmix.core.Entity;
import io.jmix.ui.components.data.HasValueSource;
import io.jmix.ui.components.data.Options;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.options.ListEntityOptions;
import io.jmix.ui.components.data.options.MapEntityOptions;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TokenList<V extends Entity> extends Field<Collection<V>>,
        Component.BelongToFrame, Component.HasCaption, Component.Editable, Component.Focusable, HasCaptionMode,
        HasOptionCaptionProvider<V> {

    String NAME = "tokenList";

    /**
     * Sets field options.
     *
     * @param options field options
     */
    void setOptions(Options<V> options);

    /**
     * @return field options
     */
    Options<V> getOptions();

    /**
     * @return options filter mode
     */
    LookupField.FilterMode getFilterMode();

    /**
     * Sets the given {@code mode} to manage how options should be filtered.
     *
     * @param mode options filter mode
     */
    void setFilterMode(LookupField.FilterMode mode);

    /**
     * Sets function that provides caption for LookupField options.
     *
     * @param optionsCaptionProvider caption provider for options
     */
    void setLookupFieldOptionsCaptionProvider(Function<? super V, String> optionsCaptionProvider);

    /**
     * @return caption provider for LookupField options
     */
    Function<? super V, String> getLookupFieldOptionsCaptionProvider();

    /**
     * @return option captions mode generation
     *
     * @deprecated use {@link TokenList#getLookupFieldOptionsCaptionProvider()}
     */
    @Deprecated
    CaptionMode getOptionsCaptionMode();

    /**
     * Sets how LookupField option captions should be generated.
     *
     * @param optionsCaptionMode mode
     *
     * @deprecated use {@link TokenList#setLookupFieldOptionsCaptionProvider(Function)} instead
     */
    @Deprecated
    void setOptionsCaptionMode(CaptionMode optionsCaptionMode);

    /**
     * @return a property that is used for LookupField option captions generation
     *
     * @deprecated use {@link TokenList#getLookupFieldOptionsCaptionProvider()} instead
     */
    @Deprecated
    String getOptionsCaptionProperty();

    /**
     * Sets a property that will be used for LookupField option captions generation when {@link CaptionMode#PROPERTY} is used.
     *
     * @param optionsCaptionProperty property
     *
     * @deprecated use {@link TokenList#setLookupFieldOptionsCaptionProvider(Function)} instead
     */
    @Deprecated
    void setOptionsCaptionProperty(String optionsCaptionProperty);

    /**
     * Sets whether options should be refreshed after lookup window closing.
     */
    void setRefreshOptionsOnLookupClose(boolean refresh);

    /**
     * @return whether options should be refreshed after lookup window closing
     */
    boolean isRefreshOptionsOnLookupClose();

    /**
     * @return options list
     *
     * @deprecated use {@link TokenList#getOptions()} instead
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    default List getOptionsList() {
        Options<V> options = getOptions();
        if (options instanceof ListEntityOptions) {
            return ((ListEntityOptions<V>) options).getItemsCollection();
        }
        return null;
    }

    /**
     * @param optionsList options list
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void setOptionsList(List optionsList) {
        setOptions(new ListEntityOptions<>(optionsList));
    }

    /**
     * @return options map
     * @deprecated use {@link TokenList#getOptions()} instead
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Deprecated
    default Map<String, ?> getOptionsMap() {
        Options options = getOptions();
        if (options instanceof MapEntityOptions) {
            return ((MapEntityOptions) options).getItemsCollection();
        }
        return null;
    }

    /**
     * @param optionsMap options map
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default void setOptionsMap(Map<String, ?> optionsMap) {
        setOptions(new MapEntityOptions(optionsMap));
    }

    /**
     * @return whether inner LookupPickerField component has lookup action
     */
    boolean isLookup();

    /**
     * Sets whether inner LookupPickerField component should have lookup action.
     *
     * @param lookup enable lookup action
     */
    void setLookup(boolean lookup);

    /**
     * Sets a lookup screen provider that is used when {@link TokenList#isLookup()} enabled.
     * <p>
     * Provided screen should implement {@link LookupScreen} interface.
     *
     * @param lookupProvider lookup screen provider
     */
    void setLookupProvider(Supplier<Screen> lookupProvider);

    /**
     * @return lookup screen provider
     */
    Supplier<Screen> getLookupProvider();

    /**
     * @return lookup screen alias
     */
    @Deprecated
    String getLookupScreen();

    /**
     * Sets lookup screen alias.
     *
     * @param lookupScreen screen alias
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    void setLookupScreen(String lookupScreen);

    /**
     * Sets params that will be passed to lookup screen.
     *
     * @param params params
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    void setLookupScreenParams(Map<String, Object> params);

    /**
     * @return params that will be passed to lookup screen
     */
    @Nullable
    @Deprecated
    Map<String, Object> getLookupScreenParams();

    /**
     * @return lookup screen open mode
     */

    @Deprecated
    OpenType getLookupOpenMode();

    /**
     * Sets lookup screen open mode.
     * <p>
     * {@link OpenType#THIS_TAB} is the default.
     *
     * @param lookupOpenMode open mode
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    void setLookupOpenMode(OpenType lookupOpenMode);

    /**
     * @return clear button is enabled
     */
    boolean isClearEnabled();

    /**
     * Sets whether clear button is enabled or not
     *
     * @param clearEnabled clear button enabled
     */
    void setClearEnabled(boolean clearEnabled);

    /**
     * @return whether multiselect mode is enabled
     */
    boolean isMultiSelect();

    /**
     * Enables multiselect mode. It leads to the passing {@code MULTI_SELECT} param
     * to the lookup screen.
     *
     * @param multiselect multiselect
     */
    void setMultiSelect(boolean multiselect);

    /**
     * @return whether simple mode is used ("Add button" instead of LookupPickerField)
     */
    boolean isSimple();

    /**
     * Sets whether simple mode should be used ("Add button" instead of LookupPickerField)
     *
     * @param simple simple
     */
    void setSimple(boolean simple);

    /**
     * @return component editor (LookupPickerField / "Add" button) position
     */
    Position getPosition();

    /**
     * Sets component editor (LookupPickerField / "Add" button) position.
     * <p>
     * {@link Position#TOP} is the default.
     *
     * @param position editor position
     */
    void setPosition(Position position);

    /**
     * @return whether inline tokens mode should be used
     */
    boolean isInline();

    /**
     * Sets whether inline tokens mode should be used.
     *
     * @param inline inline mode
     */
    void setInline(boolean inline);

    /**
     * @return "Add" button caption
     */
    String getAddButtonCaption();

    /**
     * Sets "Add" button caption.
     *
     * @param caption caption
     */
    void setAddButtonCaption(String caption);

    /**
     * @return "Add" button icon
     */
    String getAddButtonIcon();

    /**
     * Sets "Add" button icon.
     *
     * @param icon icon
     */
    void setAddButtonIcon(String icon);

    /**
     * @return "Clear" button caption
     */
    String getClearButtonCaption();

    /**
     * Sets "Clear" button caption.
     *
     * @param caption caption
     */
    void setClearButtonCaption(String caption);

    /**
     * @return "Clear" button icon
     */
    String getClearButtonIcon();

    /**
     * Sets "Clear" button icon.
     *
     * @param icon icon
     */
    void setClearButtonIcon(String icon);

    /**
     * @return selected items change handler
     */
    ItemChangeHandler getItemChangeHandler();

    /**
     * Sets selected items change handler.
     *
     * @param handler items change handler
     */
    void setItemChangeHandler(ItemChangeHandler handler);

    /**
     * @return selected tokens click listener
     */
    ItemClickListener getItemClickListener();

    /**
     * Sets selected tokens click listener.
     *
     * @param itemClickListener items click listener
     */
    void setItemClickListener(ItemClickListener itemClickListener);

    /**
     * @return handler that is invoked after lookup screen closing
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    AfterLookupCloseHandler getAfterLookupCloseHandler();

    /**
     * Sets handler that is invoked after lookup screen closing.
     *
     * @param handler handler
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    void setAfterLookupCloseHandler(AfterLookupCloseHandler handler);

    /**
     * @return handler that is invoked when an item is selected in lookup screen
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    AfterLookupSelectionHandler getAfterLookupSelectionHandler();

    /**
     * Sets handler that is invoked when an item is selected in lookup screen.
     *
     * @param handler handler
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    void setAfterLookupSelectionHandler(AfterLookupSelectionHandler handler);

    @Deprecated
    void setTokenStyleGenerator(Function<Object, String> tokenStyleGenerator);

    @Deprecated
    Function<Object, String> getTokenStyleGenerator();

    /**
     * @return input prompt of LookupPickerField
     */
    String getLookupInputPrompt();

    /**
     * Sets the input prompt - a textual prompt that is displayed when the LookupPickerField
     * would otherwise be empty, to prompt the user for input.
     *
     * @param inputPrompt input prompt
     */
    void setLookupInputPrompt(String inputPrompt);

    /**
     * Enables to generate stylenames for tokens.
     *
     * @deprecated
     */
    @Deprecated
    interface TokenStyleGenerator extends Function<Object, String> {

        @Override
        default String apply(Object itemId) {
            return getStyle(itemId);
        }

        String getStyle(Object itemId);
    }

    /**
     * Enables to handle selected items change.
     */
    interface ItemChangeHandler {

        /**
         * Invoked when item is added.
         *
         * @param item item
         */
        void addItem(Object item);

        /**
         * Invoked when item is removed.
         *
         * @param item item
         */
        void removeItem(Object item);
    }

    /**
     * Selected items click handler.
     */
    @FunctionalInterface
    interface ItemClickListener {

        /**
         * Invoked when selected item is clicked.
         *
         * @param item item
         */
        void onClick(Object item);
    }

    /**
     * Enables to handle lookup screen closing.
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    @FunctionalInterface
    interface AfterLookupCloseHandler {

        /**
         * Invoked when lookup screen is closed.
         *
         * @param window   window
         * @param actionId action id
         */
        void onClose(Window window, String actionId);
    }

    /**
     * Enables to handle item selection in lookup screen.
     *
     * @deprecated use {@link TokenList#setLookupProvider(Supplier)} instead
     */
    @Deprecated
    @FunctionalInterface
    interface AfterLookupSelectionHandler {

        /**
         * Invoked when items are selected.
         *
         * @param items items
         */
        void onSelect(Collection items);
    }

    /**
     * Defines component editor position.
     */
    enum Position {

        /**
         * Editor is above tokens container.
         */
        TOP,

        /**
         * Editor is under tokens container.
         */
        BOTTOM
    }
}
