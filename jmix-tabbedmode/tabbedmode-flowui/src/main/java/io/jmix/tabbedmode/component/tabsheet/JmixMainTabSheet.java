/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.component.tabsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class JmixMainTabSheet extends JmixTabSheet {

    public void removeAll() {
        new ArrayList<>(tabToContent.keySet())
                .forEach(this::remove);
    }

    public Set<Tab> getTabs() {
        return Collections.unmodifiableSet(tabToContent.keySet());
    }

    // TODO: gg, add collection version
    public Stream<Component> getTabComponentsStream() {
        return tabToContent.values().stream();
    }

    // TODO: gg, issue to add selected
    @Nullable
    @Override
    public Tab getSelectedTab() {
        return super.getSelectedTab();
    }

    /*@Override
    public Component getContentByTab(Tab tab) {
        return findContentByTab(tab)
                .orElseThrow(() -> new IllegalStateException("Specified tab has no content"));
    }

    public Optional<Component> findContentByTab(Tab tab) {
        return Optional.ofNullable(tabToContent.get(tab));
    }*/
}
