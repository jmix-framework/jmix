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

package com.haulmont.cuba.gui.components.compatibility;

import com.haulmont.cuba.gui.components.LookupField;
import io.jmix.ui.component.ComboBox;

import java.util.function.Predicate;

@Deprecated
public class LookupFieldFilterPredicateAdapter implements Predicate<ComboBox.OptionsCaptionFilteringContext> {

    protected LookupField.FilterPredicate filterPredicate;

    public LookupFieldFilterPredicateAdapter(LookupField.FilterPredicate filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LookupFieldFilterPredicateAdapter)) return false;

        LookupFieldFilterPredicateAdapter that = (LookupFieldFilterPredicateAdapter) o;

        return filterPredicate.equals(that.filterPredicate);
    }

    @Override
    public int hashCode() {
        return filterPredicate.hashCode();
    }

    @Override
    public boolean test(ComboBox.OptionsCaptionFilteringContext context) {
        return filterPredicate.test(context.getItemCaption(), context.getSearchString());
    }

    public LookupField.FilterPredicate getFilterPredicate() {
        return filterPredicate;
    }
}
