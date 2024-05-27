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

package io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined;

import io.jmix.core.JmixOrder;
import io.jmix.core.annotation.Internal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_TodayPredefinedDateInterval")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
public class TodayPredefinedDateInterval extends PredefinedDateInterval {

    public static final String NAME = "today";

    public TodayPredefinedDateInterval() {
        super(NAME);
    }

    @Override
    public String apply(String property) {
        return String.format("@between({E}.%s, now, now + 1, day)", property);
    }
}
