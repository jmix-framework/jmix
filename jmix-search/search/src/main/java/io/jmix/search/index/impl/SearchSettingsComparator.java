/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.impl;

import org.elasticsearch.common.settings.Settings;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;

@Component("zndfl_SearchSettingsComparator")
public class SearchSettingsComparator {

    private static final List<String> DYNAMIC_ATTRIBUTES = asList(
            "index.number_of_replicas",
            "index.search.idle.after",
            "index.refresh_interval",
            "index.max_result_window",
            "index.max_inner_result_window",
            "index.max_rescore_window",
            "index.max_docvalue_fields_search",
            "index.max_script_fields",
            "index.max_ngram_diff",
            "index.max_shingle_diff",
            "index.max_refresh_listeners",
            "index.analyze.max_token_count",
            "index.highlight.max_analyzed_offset",
            "index.max_terms_count",
            "index.max_regex_length",
            "index.query.default_field",
            "index.routing.allocation.enable",
            "index.gc_deletes",
            "index.final_pipeline",
            "index.hidden"
    );

    public ComparingState compare(Settings searchServerSettings, Settings applicationSettings) {

        boolean settingsAreCompatible = true;
        boolean settingsAreChanged = false;

        for (String key: applicationSettings.keySet()){
            String applicationValue = applicationSettings.get(key);
            String searchServerValue = searchServerSettings.get(key);
            boolean valueIsChanged = !applicationValue.equals(searchServerValue);
            settingsAreChanged = settingsAreChanged || valueIsChanged;
            boolean valueIsCompatible = valueIsChanged && isDinamic(key);
            settingsAreCompatible = settingsAreCompatible && valueIsCompatible;
        }

        if(!settingsAreChanged) return ComparingState.EQUAL;

        return settingsAreCompatible ? ComparingState.COMPATIBLE : ComparingState.NOT_COMPATIBLE;
    }

    private boolean isDinamic(String key) {
        return DYNAMIC_ATTRIBUTES.contains(key);
    }
}
