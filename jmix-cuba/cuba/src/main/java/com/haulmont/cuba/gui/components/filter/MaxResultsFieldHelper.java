/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.gui.components.filter;

import com.google.common.base.Splitter;
import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.LookupField;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component(MaxResultsFieldHelper.NAME)
public class MaxResultsFieldHelper {
    public static final String NAME = "cuba_MaxResultsFieldHelper";

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected FilterHelper filterHelper;
    @Autowired
    protected ThemeConstantsManager themeConstantsManager;
    @Autowired
    protected CubaProperties properties;

    public LookupField<Integer> createMaxResultsLookupField() {
        LookupField<Integer> maxResultsLookupField = uiComponents.create(LookupField.of(Integer.class));
        setUpMaxResultsLookupField(maxResultsLookupField);

        return maxResultsLookupField;
    }

    public LookupField<Integer> setUpMaxResultsLookupField(LookupField<Integer> maxResultsLookupField) {
        ThemeConstants theme = themeConstantsManager.getConstants();

        maxResultsLookupField.setWidth(theme.get("cuba.gui.Filter.maxResults.lookup.width"));
        filterHelper.setLookupTextInputAllowed(maxResultsLookupField, false);
        filterHelper.setLookupNullSelectionAllowed(maxResultsLookupField, false);

        List<Integer> maxResultOptions = new ArrayList<>();
        String maxResultOptionsStr = properties.getGenericFilterMaxResultsOptions();
        Iterable<String> split = Splitter.on(",").trimResults().split(maxResultOptionsStr);
        for (String option : split) {
            if ("NULL".equals(option)) {
                filterHelper.setLookupNullSelectionAllowed(maxResultsLookupField, true);
            } else {
                try {
                    Integer value = Integer.valueOf(option);
                    maxResultOptions.add(value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        maxResultsLookupField.setOptionsList(maxResultOptions);

        return maxResultsLookupField;
    }
}
