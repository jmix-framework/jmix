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

package com.haulmont.cuba.gui.components.compatibility;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.data.DsContext;
import io.jmix.core.Messages;
import io.jmix.ui.component.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Enables us to show screens based on {@code AbstractWindow} as a <b>frame</b>.
 */
@Deprecated
public class LegacyFragmentAdapter extends AbstractFrame {

    private AbstractWindow screen;

    public LegacyFragmentAdapter(AbstractWindow legacyScreen) {
        this.screen = legacyScreen;
        this.messages = AppBeans.get(Messages.class);
    }

    /**
     * @return wrapper screen
     */
    public AbstractWindow getRealScreen() {
        return screen;
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        screen.init(params);
    }

    @Override
    public WindowManager getWindowManager() {
        return ((AbstractFrame) getFrame()).getWindowManager();
    }

    @Override
    public void setDsContext(DsContext dsContext) {
        super.setDsContext(dsContext);

        screen.setDsContext(dsContext);
    }

    @Override
    public Component getOwnComponent(String id) {
        return screen.getOwnComponent(id);
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return screen.getComponent(id);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return screen.getOwnComponents();
    }

    @Override
    public Stream<Component> getOwnComponentsStream() {
        return screen.getOwnComponentsStream();
    }

    @Override
    public Collection<Component> getComponents() {
        return screen.getComponents();
    }

    @Override
    public Object getComponent() {
        return screen.getComponent();
    }

    @Override
    public Object getComposition() {
        return screen.getComposition();
    }

    @Override
    public void add(Component... childComponents) {
        screen.add(childComponents);
    }

    @Override
    public void remove(Component... childComponents) {
        screen.remove(childComponents);
    }

    @Nonnull
    @Override
    public Component getComponentNN(String id) {
        return screen.getComponentNN(id);
    }
}
