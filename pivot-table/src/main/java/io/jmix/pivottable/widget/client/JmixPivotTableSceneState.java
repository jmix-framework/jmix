/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.widget.client;

import com.vaadin.shared.AbstractComponentState;

import java.util.Map;

public class JmixPivotTableSceneState extends AbstractComponentState {

    {
        primaryStyleName = "jmix-pivot-table";
    }

    public static final String REFRESH_EVENT = "r";
    public static final String CELL_CLICK_EVENT = "cc";

    public String data;
    public String options;
    public String json;

    public String emptyDataMessage;

    // key: language; value: messagesMap as json
    public Map<String, String> localeMap;
}
