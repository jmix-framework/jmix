/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.logicalfilter;

import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import org.springframework.lang.Nullable;

@Internal
public class GroupFilterUtils {

    public static void updateDataLoaderInitialCondition(GroupFilter groupFilter, @Nullable Condition condition) {
        groupFilter.updateDataLoaderInitialCondition(condition);
    }
}
