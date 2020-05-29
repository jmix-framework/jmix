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
package io.jmix.ui.presentation;

import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.component.Component;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.util.*;

/**
 * Stub. By default, UI does not provide persistence functionality for presentations. To save/load presentations add
 * "ui-persistence" add-on.
 */
@org.springframework.stereotype.Component(TablePresentations.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmptyTablePresentationsImpl implements TablePresentations {

    public EmptyTablePresentationsImpl(Component c) {
        // do nothing
    }

    @Override
    public TablePresentation getCurrent() {
        return null;
    }

    @Override
    public void setCurrent(TablePresentation p) {
        // do nothing
    }

    @Override
    public Element getSettings(TablePresentation p) {
        return null;
    }

    @Override
    public String getSettingsString(TablePresentation p) {
        return null;
    }

    @Override
    public void setSettings(TablePresentation p, Element e) {
        // do nothing
    }

    @Override
    public void setSettings(TablePresentation p, String settings) {
        // do nothing
    }

    @Override
    public TablePresentation getPresentation(Object id) {
        return null;
    }

    @Override
    public String getCaption(Object id) {
        return null;
    }

    @Override
    public Collection<Object> getPresentationIds() {
        return Collections.emptyList();
    }

    @Override
    public TablePresentation getDefault() {
        return null;
    }

    @Override
    public void setDefault(TablePresentation p) {
        // do nothing
    }

    @Override
    public void add(TablePresentation p) {
        // do nothing
    }

    @Override
    public void remove(TablePresentation p) {
        // do nothing
    }

    @Override
    public void modify(TablePresentation p) {
        // do nothing
    }

    @Override
    public boolean isAutoSave(TablePresentation p) {
        return false;
    }

    @Override
    public boolean isGlobal(TablePresentation p) {
        return false;
    }

    @Override
    public void commit() {
        // do nothing
    }

    @Override
    public TablePresentation getPresentationByName(String name) {
        return null;
    }

    @Override
    public void addListener(PresentationsChangeListener listener) {
        // do nothing
    }

    @Override
    public void removeListener(PresentationsChangeListener listener) {
        // do nothing
    }
}
