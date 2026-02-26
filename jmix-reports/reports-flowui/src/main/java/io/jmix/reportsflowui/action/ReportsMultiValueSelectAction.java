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

package io.jmix.reportsflowui.action;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction;
import io.jmix.reportsflowui.component.ReportsMultiValueSelectContext;

/**
 * INTERNAL! Will be removed in next releases.
 *
 * @param <E> entity type
 */
@Internal
public class ReportsMultiValueSelectAction<E> extends MultiValueSelectAction<E> {

    public static final String ID = "report_multi_value_select";

    public ReportsMultiValueSelectAction() {
        this(ID);
    }

    public ReportsMultiValueSelectAction(String id) {
        super(id);

        multiValueSelectContext = new ReportsMultiValueSelectContext<>();
    }
}
