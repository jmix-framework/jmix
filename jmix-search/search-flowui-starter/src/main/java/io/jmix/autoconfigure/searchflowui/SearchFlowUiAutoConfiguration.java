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

package io.jmix.autoconfigure.searchflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.searchflowui.SearchFlowUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, FlowuiConfiguration.class, SearchConfiguration.class, SearchFlowUiConfiguration.class})
public class SearchFlowUiAutoConfiguration {
}
