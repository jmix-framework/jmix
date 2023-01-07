/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.common.xmlparsing;

import io.jmix.core.annotation.Internal;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Internal
@Component("core_Dom4jToolsShutdownListener")
public class Dom4jToolsShutdownListener {

    @Autowired
    protected BeanFactory beanFactory;

    @EventListener(ContextClosedEvent.class)
    public void appContextStopped() {
        beanFactory.getBean(Dom4jTools.class).shutdown();
    }

}
