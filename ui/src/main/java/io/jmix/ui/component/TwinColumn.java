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

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A component with two lists: left list for available options, right list for selected values.
 *
 * @param <V> value and options type for the component
 */
public interface TwinColumn<V> extends OptionsField<Collection<V>, V>, Component.Focusable, HasOptionStyleProvider<V> {

    String NAME = "twinColumn";

    /**
     * @return the number of visible rows
     */
    int getRows();

    /**
     * Sets the number of visible rows.
     *
     * @param rows number of visible rows
     */
    void setRows(int rows);

    /**
     * Allows you to configure whether items should be reordered after selection.
     * <p>
     * Reordering is enabled by default.
     *
     * @param reorderable pass 'true' to enable reordering or 'false' otherwise
     */
    void setReorderable(boolean reorderable);

    /**
     * @return true if items are reordered or false otherwise
     */
    boolean isReorderable();

    /**
     * Enables "Add all" and "Remove all" buttons.
     *
     * @param enabled true if buttons should be enabled
     */
    void setAddAllBtnEnabled(boolean enabled);

    /**
     * @return true if buttons are enabled
     */
    boolean isAddAllBtnEnabled();

    /**
     * Set caption for the left column.
     *
     * @param leftColumnCaption
     */
    void setLeftColumnCaption(@Nullable String leftColumnCaption);

    /**
     * Return caption of the left column.
     *
     * @return caption text or null if not set.
     */
    @Nullable
    String getLeftColumnCaption();

    /**
     * Set caption for the right column.
     *
     * @param rightColumnCaption
     */
    void setRightColumnCaption(@Nullable String rightColumnCaption);

    /**
     * Return caption of the right column.
     *
     * @return caption text or null if not set.
     */
    @Nullable
    String getRightColumnCaption();
}
