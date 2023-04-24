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

package io.jmix.grapesjs.component.impl;

import io.jmix.grapesjs.component.GjsBlock;
import io.jmix.grapesjs.component.GjsBlocksRepository;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("grpjs_GjsBlocksRepository")
public class GjsBlocksRepositoryImpl implements GjsBlocksRepository {

    protected Map<String, GjsBlock> registeredBlocks = new HashMap<>();

    @EventListener
    private void appStarted(ContextStartedEvent event) {
        registerDefaultBlocks();
    }

    protected void registerDefaultBlocks() {
    }

    @Override
    public GjsBlock getBlock(String name) {
        return registeredBlocks.get(name).clone();
    }

    @Override
    public void registerBlock(GjsBlock block) {
        registeredBlocks.put(block.getName(), block.clone());
    }
}
