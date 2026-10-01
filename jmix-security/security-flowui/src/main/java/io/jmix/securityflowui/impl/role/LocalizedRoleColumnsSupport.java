/*
 * Copyright 2026 Haulmont.
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

package io.jmix.securityflowui.impl.role;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.grid.ColumnPathRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.model.CollectionChangeType;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.role.RoleLocalizationSupport;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shows the name and the description of role models in the user's locale in the data grids of the role views, and
 * orders role models by the name. The texts are compared ignoring case, as in-memory sorting compares strings.
 */
@Component("sec_LocalizedRoleColumnsSupport")
@NullMarked
public class LocalizedRoleColumnsSupport {

    private static final String NAME_COLUMN_KEY = "name";
    private static final String DESCRIPTION_COLUMN_KEY = "description";

    @Autowired
    private RoleLocalizationSupport roleLocalizationSupport;

    /**
     * Compares by a text ignoring case, as in-memory sorting compares strings. Both the initial order and the column
     * comparators use it, so that they order alike.
     */
    private static <T> Comparator<T> comparingText(Function<T, String> text) {
        return Comparator.comparing(text, String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Gives the {@code name} and the {@code description} columns of the grid, those it shows, a renderer of the text
     * in the user's locale and a comparator by that text. It fills in only what the view has not configured: a column
     * keeps a renderer other than the default one of its property, a comparator set explicitly and whether it is
     * sortable.
     *
     * @param grid the grid of role models
     * @param <M>  the type of the role models
     */
    public <M extends BaseRoleModel> void install(DataGrid<M> grid) {
        new GridTexts<M>().attachTo(grid);
    }

    /**
     * Returns the role models ordered by the name in the user's locale, ignoring case. The name of each model is
     * resolved once.
     *
     * @param roleModels the role models to order
     * @param <M>        the type of the role models
     * @return a new list of the role models
     */
    public <M extends BaseRoleModel> List<M> sortByName(Collection<M> roleModels) {
        Map<M, String> names = new IdentityHashMap<>();
        for (M roleModel : roleModels) {
            names.put(roleModel, roleLocalizationSupport.getLocalizedName(roleModel));
        }
        return roleModels.stream()
                .sorted(comparingText(names::get))
                .collect(Collectors.toList());
    }

    /**
     * The texts of one grid. A text of a model is resolved when a column first needs it and kept for that instance of
     * the model, so that sorting does not resolve it for every comparison, until the model changes or the container of
     * the grid refreshes, which it also does after an in-memory sort.
     */
    private class GridTexts<M extends BaseRoleModel> {

        // By instance: a model of a database role keeps the id of the role, so a new model of the role is equal to the
        // old one, and a container may take it in with its events muted, as a detail view reloads its child roles.
        private final Map<M, String> names = new IdentityHashMap<>();
        private final Map<M, String> descriptions = new IdentityHashMap<>();

        private void attachTo(DataGrid<M> grid) {
            installColumn(grid, NAME_COLUMN_KEY, this::getName);
            installColumn(grid, DESCRIPTION_COLUMN_KEY, this::getDescription);

            if (grid.getItems() instanceof ContainerDataUnit<?> containerDataUnit) {
                // The items of a grid of role models come from a container of these models.
                @SuppressWarnings("unchecked")
                CollectionContainer<M> container = (CollectionContainer<M>) containerDataUnit.getContainer();
                container.addCollectionChangeListener(this::onCollectionChange);
                container.addItemPropertyChangeListener(event -> forget(event.getItem()));
            }
        }

        private void installColumn(DataGrid<M> grid, String columnKey, ItemLabelGenerator<M> textProvider) {
            DataGridColumn<M> column = grid.getColumnByKey(columnKey);
            // The grid still returns a column that security has removed; a renderer would send its values to the
            // client.
            if (column == null || !grid.getColumns().contains(column)) {
                return;
            }

            if (column.getRenderer() instanceof ColumnPathRenderer) {
                column.setRenderer(new TextRenderer<>(textProvider));
            }

            if (column.getComparatorOrNull() == null) {
                // Setting a comparator makes a column sortable.
                boolean sortable = column.isSortable();
                column.setComparator(comparingText(textProvider));
                column.setSortable(sortable);
            }
        }

        /**
         * Drops the texts of models that the container of the grid no longer holds, so as not to keep those models:
         * all the texts on a refresh and the texts of the removed models. An added model, or one put in place of
         * another, is a new instance with texts of its own; the texts of the replaced one stay until a refresh.
         */
        private void onCollectionChange(CollectionContainer.CollectionChangeEvent<M> event) {
            if (event.getChangeType() == CollectionChangeType.REFRESH) {
                names.clear();
                descriptions.clear();
            } else if (event.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
                event.getChanges().forEach(this::forget);
            }
        }

        private void forget(M roleModel) {
            names.remove(roleModel);
            descriptions.remove(roleModel);
        }

        private String getName(M roleModel) {
            return names.computeIfAbsent(roleModel, roleLocalizationSupport::getLocalizedName);
        }

        private String getDescription(M roleModel) {
            return descriptions.computeIfAbsent(roleModel,
                    model -> Objects.requireNonNullElse(roleLocalizationSupport.getLocalizedDescription(model), ""));
        }
    }
}
