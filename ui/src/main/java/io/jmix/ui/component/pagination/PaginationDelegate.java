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

package io.jmix.ui.component.pagination;

import com.google.common.base.Splitter;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Pagination;
import io.jmix.ui.sys.PersistenceManagerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for {@link Pagination} component. Performs operation on max result options.
 */
@Component(PaginationDelegate.NAME)
public class PaginationDelegate {
    public static final String NAME = "ui_PaginationDelegate";

    protected UiProperties uiProperties;
    protected PersistenceManagerClient persistenceManager;

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setPersistenceManager(PersistenceManagerClient persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public int findClosestValue(int maxResults, List<Integer> optionsList) {
        int minimumValue = Integer.MAX_VALUE;
        int closest = maxResults;

        for (int option : optionsList) {
            int diff = Math.abs(option - maxResults);
            if (diff < minimumValue) {
                minimumValue = diff;
                closest = option;
            }
        }

        return closest;
    }

    public List<Integer> getMaxResultsFromProperty() {
        String maxResultsProperty = uiProperties.getPaginationMaxResults();
        return parseMaxResultsOptions(maxResultsProperty);
    }

    public List<Integer> parseMaxResultsOptions(String maxResults) {
        Iterable<String> split = Splitter.on(",").trimResults().split(maxResults);

        List<Integer> result = new ArrayList<>();
        for (String option : split) {
            try {
                int value = Integer.parseInt(option);
                if (value > 0) {
                    result.add(value);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return result;
    }

    /**
     * Filters, sorts options that is provided by {@link UiProperties#getPaginationMaxResults()}. Adds max fetch value
     * if options list contains a greater value.
     *
     * @param loaderMaxResult loader's max result
     * @param metaClass       entity's MetaClass
     * @return filtered and sorted options
     */
    public List<Integer> filterPropertyOptions(@Nullable Integer loaderMaxResult, MetaClass metaClass) {
        Integer maxFetch = persistenceManager.getMaxFetchUI(metaClass.getName());
        return doFilter(getMaxResultsFromProperty(), loaderMaxResult, maxFetch);
    }

    /**
     * Filters, sorts options. Adds max fetch value if options list contains a greater value.
     *
     * @param options         max result options
     * @param loaderMaxResult loader's max result
     * @param metaClass       entity's MetaClass
     * @return filtered and sorted options
     */
    public List<Integer> filterOptions(List<Integer> options, @Nullable Integer loaderMaxResult, MetaClass metaClass) {
        Integer maxFetch = persistenceManager.getMaxFetchUI(metaClass.getName());
        return doFilter(options, loaderMaxResult, maxFetch);
    }

    /**
     * Checks if expected value exist in options. If no it will find the closest value from them.
     *
     * @param options       max result options
     * @param expectedValue value that is probably exist in options
     * @param metaClass     entity's MetaClass
     * @return value from options
     */
    public Integer getAllowedOption(List<Integer> options, Integer expectedValue, MetaClass metaClass) {
        // default loader's value
        if (expectedValue == Integer.MAX_VALUE) {
            return findClosestValue(persistenceManager.getFetchUI(metaClass.getName()), options);
        }

        if (!options.contains(expectedValue)) {
            return findClosestValue(expectedValue, options);
        }

        return expectedValue;
    }

    public Integer getMaxFetchValue(MetaClass metaClass) {
        return persistenceManager.getMaxFetchUI(metaClass.getName());
    }

    protected List<Integer> doFilter(List<Integer> options, @Nullable Integer loaderMaxResult, Integer maxFetch) {
        List<Integer> result = new ArrayList<>();
        for (Integer option : options) {
            if (option > 0 && option < maxFetch) {
                result.add(option);
            }

            if (option >= maxFetch && !result.contains(maxFetch)) {
                result.add(maxFetch);
            }
        }

        // if loader's max result is not in bounds
        if (loaderMaxResult != null
                && loaderMaxResult != Integer.MAX_VALUE
                && loaderMaxResult >= maxFetch
                && !result.contains(maxFetch)) {
            result.add(maxFetch);
        }

        Collections.sort(result);

        return result;
    }
}
