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

package io.jmix.flowui.sys;

import io.jmix.core.impl.metadata.MetadataGenerationPublishedEvent;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("flowui_MetadataGenerationUiInvalidator")
public class MetadataGenerationUiInvalidator {

    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected MenuConfig menuConfig;
    @Autowired
    protected ViewSupport viewSupport;

    /**
     * Clears UI registries that must be rebuilt from the newly published metadata generation.
     *
     * @param event publication event of the new metadata generation
     */
    @EventListener
    public void onMetadataGenerationPublished(MetadataGenerationPublishedEvent event) {
        viewRegistry.reset();
        menuConfig.reset();
        viewSupport.clearTitleCache();
    }
}
