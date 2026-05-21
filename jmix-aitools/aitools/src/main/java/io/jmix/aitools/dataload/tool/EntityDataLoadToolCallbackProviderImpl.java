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

package io.jmix.aitools.dataload.tool;

import io.jmix.aitools.tool.AbstractToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("aitols_AiEntityDataLoadToolCallbackProvider")
public class EntityDataLoadToolCallbackProviderImpl extends AbstractToolCallbackProvider<EntityDataLoadAiTool>
        implements EntityDataLoadToolCallbackProvider {

    @Autowired
    protected List<EntityDataLoadAiTool> entityDataLoadAiTools;

    @Override
    public List<ToolCallback> getToolCallbacks() {
        return getToolCallbacks(entityDataLoadAiTools);
    }
}
