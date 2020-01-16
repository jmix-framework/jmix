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

package io.jmix.ui.dynamicattributes;

import io.jmix.core.View;
import io.jmix.core.entity.BaseGenericIdEntity;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.components.HasValue;
import io.jmix.ui.components.PickerField;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// todo dummy component to observe the surface of dynamic attributes usage on ui

@Component(DynamicAttributesGuiTools.NAME)
public class DynamicAttributesGuiTools {
    public static final String NAME = "cuba_DynamicAttributesGuiTools";

    /**
     * Enforce the datasource to change modified status if dynamic attribute is changed
     */
    /*
    TODO: legacy-ui
    @SuppressWarnings("unchecked")
    public void listenDynamicAttributesChanges(final Datasource datasource) {

    }*/

    /**
     * Get attributes which should be added automatically to the screen and component.
     * Based on visibility settings from category attribute editor.
     */
    public Set<CategoryAttribute> getAttributesToShowOnTheScreen(MetaClass metaClass, String screen, @Nullable String component) {
        return Collections.emptySet();
    }

    /**
     * Get attributes which should be added automatically to the screen and component.
     * Based on visibility settings from category attribute editor.
     * Resulting list is sorted using CategoryAttribute.orderNo parameter.
     */
    public List<CategoryAttribute> getSortedAttributesToShowOnTheScreen(MetaClass metaClass, String screen, @Nullable String component) {
        return Collections.emptyList();
    }

    /**
     * Method checks whether any class in the view hierarchy contains dynamic attributes that must be displayed on
     * the current screen
     */
    public boolean screenContainsDynamicAttributes(View mainDatasourceView, String screenId) {
        return false;
    }

    public void initDefaultAttributeValues(BaseGenericIdEntity item, MetaClass metaClass) {
    }

    /*
    TODO: legacy-ui
    @SuppressWarnings("unchecked")
    public void listenCategoryChanges(Datasource ds) {
    }*/

    /**
     * Initializes the pickerField for selecting the dynamic attribute value. If the CategoryAttribute has "where" or
     * "join" clauses then the data in lookup screens will be filtered with these clauses
     *
     * @param pickerField       PickerField component whose lookup action must be initialized
     * @param categoryAttribute CategoryAttribute that is represented by the pickerField
     */
    public void initEntityPickerField(PickerField pickerField, CategoryAttribute categoryAttribute) {
    }

    /**
     * Creates the collection datasource that is used for selecting the dynamic attribute value. If the
     * CategoryAttribute has "where" or "join" clauses then only items that satisfy these clauses will be presented in
     * the options datasource
     */
    /*
    TODO: legacy-ui
    public CollectionDatasource createOptionsDatasourceForLookup(MetaClass metaClass, String joinClause, String whereClause) {
        throw new UnsupportedOperationException();
    }*/

    /**
     * Creates the lookup action that will open the lookup screen with the dynamic filter applied. This filter contains
     * a condition with join and where clauses
     */
    public PickerField.LookupAction createLookupAction(PickerField pickerField,
                                                       String joinClause,
                                                       String whereClause) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reload dynamic attributes on the entity
     */
    @SuppressWarnings("unchecked")
    public void reloadDynamicAttributes(BaseGenericIdEntity entity) {
    }

    /**
     * Returns validators for a dynamic attribute
     * @return collection of validators
     */
    public Collection<Consumer<?>> createValidators(CategoryAttribute attribute) {
        return null;
    }




    /**
     * Returns custom DecimalFormat for dynamic attribute with specified {@code NumberFormatPattern}
     */
    @Nullable
    public DecimalFormat getDecimalFormat(CategoryAttribute attribute) {
        return null;
    }

    /**
     * Returns custom AdaptiveNumberDatatype for dynamic attribute with specified {@code NumberFormatPattern}
     */
    @Nullable
    public Datatype getCustomNumberDatatype(CategoryAttribute attribute) {
        return null;
    }

    /**
     * Returns column capture for dynamic attribute
     */
    public String getColumnCapture(CategoryAttribute attribute) {
        return null;
    }

    /**
     * Returns {@code ValueChangeEventListener} for dynamic attribute that has one or more dependent attributes.
     * This listener recalculates values for all dependent dynamic attributes hierarchically. The listener uses
     * {@code recalculationInProgress} ThreadLocal variable to avoid unnecessary calculation.
     */
    @SuppressWarnings("unchecked")
    public Consumer<HasValue.ValueChangeEvent> getValueChangeEventListener(final CategoryAttribute attribute) {
        return null;
    }
}
