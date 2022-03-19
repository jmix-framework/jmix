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


import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.grapesjs.component.GjsBlock;
import io.jmix.grapesjs.component.GjsPlugin;
import io.jmix.grapesjs.component.GrapesJsHtmlEditor;
import io.jmix.grapesjs.widget.grapesjshtmleditorcomponent.GrapesJsHtmlEditorComponent;
import io.jmix.ui.component.impl.AbstractComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class GrapesJsHtmlEditorImpl extends AbstractComponent<GrapesJsHtmlEditorComponent> implements GrapesJsHtmlEditor {

    protected String prevValue;
    protected Collection<String> disabledBlocks;
    protected Collection<GjsPlugin> plugins = new ArrayList<>();
    protected Collection<GjsBlock> blocks = new ArrayList<>();

    public GrapesJsHtmlEditorImpl() {
        this.component = new GrapesJsHtmlEditorComponent();
        attachValueListener(this.component);
    }

    protected void attachValueListener(GrapesJsHtmlEditorComponent component) {
        component.setListener(vEvent -> {
            final String value = getValue();
            final String oldValue = prevValue;
            prevValue = value;

            if (!EntityValues.propertyValueEquals(oldValue, value)) {
                if (hasValidationError()) {
                    setValidationError(null);
                }

                ValueChangeEvent event = new ValueChangeEvent(this, oldValue, value);
                getEventHub().publish(ValueChangeEvent.class, event);
            }
        });
    }

    @Override
    public String getValue() {
        return component.getValue();
    }

    @Override
    public void setValue(String value) {
        component.setValue(value);
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<String>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Collection<String> getDisabledBlocks() {
        return disabledBlocks;
    }

    @Override
    public void setPlugins(Collection<GjsPlugin> plugins) {
        this.plugins = plugins;
        component.setPlugins(plugins);
    }

    @Override
    public void addPlugins(Collection<GjsPlugin> plugins) {
        this.plugins.addAll(plugins);
        component.setPlugins(this.plugins);
    }

    @Override
    public void removePlugins(Collection<GjsPlugin> plugins) {
        this.plugins.removeAll(plugins);
        component.setPlugins(this.plugins);
    }

    @Override
    public Collection<GjsPlugin> getPlugins() {
        return plugins;
    }

    @Override
    public Collection<GjsBlock> getCustomBlocks() {
        return blocks;
    }

    @Override
    public void setCustomBlocks(Collection<GjsBlock> blocks) {
        this.blocks = blocks;
        component.setBlocks(blocks);
    }

    @Override
    public void addBlocks(Collection<GjsBlock> blocks) {
        this.blocks.addAll(blocks);
        component.setBlocks(this.blocks);
    }

    @Override
    public void removeCustomBlocks(Collection<GjsBlock> blocks) {
        this.blocks.removeAll(blocks);
        component.setBlocks(this.blocks);
    }

    @Override
    public void runCommand(String command) {
        component.runCommand(command);
    }

    @Override
    public void stopCommand(String command) {
        component.stopCommand(command);
    }

    @Override
    public void setDisabledBlocks(Collection<String> disabledBlocks) {
        this.disabledBlocks = disabledBlocks;
        component.setDisabledBlocks(disabledBlocks);
    }



}