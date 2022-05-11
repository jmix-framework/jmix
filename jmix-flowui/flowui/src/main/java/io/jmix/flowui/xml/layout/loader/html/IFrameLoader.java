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

package io.jmix.flowui.xml.layout.loader.html;

import com.google.common.base.Strings;
import com.vaadin.flow.component.html.IFrame;

import java.util.stream.Stream;

public class IFrameLoader extends AbstractHtmlComponentLoader<IFrame> {

    @Override
    protected IFrame createComponent() {
        return factory.create(IFrame.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadString(element, "name", resultComponent::setName);
        loadString(element, "allow", resultComponent::setAllow);
        loadString(element, "resource", resultComponent::setSrc);
        loadString(element, "resourceDoc", resultComponent::setSrcdoc);
        loadEnum(element, IFrame.ImportanceType.class, "importance", resultComponent::setImportance);
        loadSandbox();
    }

    protected void loadSandbox() {
        IFrame.SandboxType[] sandboxes = Stream.of(loaderSupport.loadString(element, "sandbox")
                        .orElse("")
                        .split(","))
                .filter(s -> !Strings.isNullOrEmpty(s.trim()))
                .map(stringValue -> Enum.valueOf(IFrame.SandboxType.class, stringValue.trim()))
                .toArray(IFrame.SandboxType[]::new);

        resultComponent.setSandbox(sandboxes);
    }
}
