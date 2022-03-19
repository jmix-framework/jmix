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
package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.screen.LookupScreen;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.jmix.ui.screen.LookupScreen.LOOKUP_SELECT_ACTION_ID;
import static io.jmix.ui.screen.LookupScreen.LOOKUP_SELECT_CLOSE_ACTION;

/**
 * An action used in the lookup screens to select an item.
 * <p>
 * This action is automatically added to a screen if it is open via the {@code openLookup()} method.
 */
public class SelectAction extends AbstractAction {

    protected AbstractLookup window;

    public SelectAction(AbstractLookup window) {
        super(LOOKUP_SELECT_ACTION_ID);
        this.window = window;
        this.primary = true;

        setShortcut(AppBeans.get(UiScreenProperties.class).getCommitShortcut());

        Messages messages = AppBeans.get(Messages.class);
        setCaption(messages.getMessage("actions.Select"));
    }

    @Override
    public void actionPerform(Component component) {
        if (window.getSelectHandler() == null) {
            // window opened not as Lookup
            return;
        }

        if (!validate())
            return;

        window.getFrameOwner()
                .close(LOOKUP_SELECT_CLOSE_ACTION)
                .then(this::handleSelection);
    }

    protected Collection getSelectedItems(LookupComponent lookupComponent) {
        return lookupComponent.getLookupSelectedItems();
    }

    @SuppressWarnings("unchecked")
    protected boolean validate() {
        Predicate<LookupScreen.ValidationContext> validator = window.getSelectValidator();
        if (validator == null) {
            return true;
        }

        LookupComponent lookupComponent = getLookupComponent();
        Collection selected = getSelectedItems(lookupComponent);

        return validator.test(new LookupScreen.ValidationContext(window, selected));
    }

    protected LookupComponent getLookupComponent() {
        Component lookupComponent = window.getLookupComponent();
        if (lookupComponent == null) {
            throw new IllegalStateException("lookupComponent is not set");
        }
        if (!(lookupComponent instanceof LookupComponent)) {
            throw new UnsupportedOperationException("Unsupported lookupComponent type: " + lookupComponent.getClass());
        }
        return (LookupComponent) lookupComponent;
    }

    protected void removeListeners(Collection selected) {
        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);
        for (Object obj : selected) {
            if (obj instanceof Entity) {
                metadataTools.traverseAttributes(obj,
                        (entity, property) -> ((Entity) entity).__getEntityEntry().removeAllListeners());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleSelection() {
        LookupScreen frameOwner = (LookupScreen) window.getFrameOwner();

        Consumer<Collection> lookupHandler = frameOwner.getSelectHandler();
        LookupComponent lookupComponent = getLookupComponent();

        Collection selected = getSelectedItems(lookupComponent);
        removeListeners(selected);
        lookupHandler.accept(selected);
    }
}
