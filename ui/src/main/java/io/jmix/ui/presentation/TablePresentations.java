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
package io.jmix.ui.presentation;

import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Provide the workflow with presentations (visual settings of a component).
 * <br><br> A component must implement {@link HasTablePresentations} interface.
 *
 * @see TablePresentation
 */
public interface TablePresentations {

    /**
     * Returns the current active presentation or <code>null</code> if a current presentation didn't set
     */
    @Nullable
    TablePresentation getCurrent();

    /**
     * Sets current active presentation for a component
     */
    void setCurrent(@Nullable TablePresentation p);

    /**
     * @param p presentation
     * @return user settings for the selected presentation or <code>null</code> if the presentation doesn't exist or if
     * the presentation doesn't contain any settings
     */
    @Nullable
    String getSettingsString(TablePresentation p);

    /**
     * Sets user settings for the selected presentation
     *
     * @param p        presentation
     * @param settings user settings
     */
    void setSettings(TablePresentation p, @Nullable String settings);

    /**
     * Returns presentation by its id or <code>null</code> if a presentation doesn't exist
     */
    @Nullable
    TablePresentation getPresentation(Object id);

    /**
     * Returns presentation caption by its id
     */
    @Nullable
    String getCaption(Object id);

    /**
     * Returns a collection of the component presentations
     */
    Collection<Object> getPresentationIds();

    /**
     * Returns a default presentation or <code>null</code> if it didn't set
     */
    @Nullable
    TablePresentation getDefault();

    /**
     * Sets a default presentation
     */
    void setDefault(@Nullable TablePresentation p);

    /**
     * Adds a new presentation
     */
    void add(TablePresentation p);

    /**
     * Removes a presentation from the list of available presentations
     */
    void remove(TablePresentation p);

    /**
     * Modifies the selected presentation
     */
    void modify(TablePresentation p);

    /**
     * Returns <code>true</code> if the selected presentation has an <code>autoSave</code> settings else returns <code>false</code>
     */
    boolean isAutoSave(TablePresentation p);

    /**
     * Returns <code>true</code> if the selected presentation is marked as global else returns <code>false</code>
     */
    boolean isGlobal(TablePresentation p);

    /**
     * Commits all changes into the database
     */
    void commit();

    /**
     * Returns a presentation by its name with ignored case.
     * It returns <code>null</code> if a presentation with such name doesn't exist
     */
    @Nullable
    TablePresentation getPresentationByName(String name);

    /**
     * Adds listener
     */
    void addListener(PresentationsChangeListener listener);

    /**
     * Removes listener
     */
    void removeListener(PresentationsChangeListener listener);

    /**
     * @return presentation instance or stub if corresponding add-on is not added to the project
     */
    TablePresentation create();
}
