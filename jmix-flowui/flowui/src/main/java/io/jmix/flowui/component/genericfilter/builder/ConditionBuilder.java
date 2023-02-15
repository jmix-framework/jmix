/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.genericfilter.builder;

import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.entity.filter.FilterCondition;
import org.springframework.core.Ordered;

import java.util.List;

public interface ConditionBuilder extends Ordered {

    List<FilterCondition> build(GenericFilter filter);
}
