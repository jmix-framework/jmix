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

package io.jmix.ui.action.tagpicker;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.action.AbstractLookupAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.MultiSelectLookupScreen;
import io.jmix.ui.screen.Screen;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Lookup action for setting entities to the tag picker using the entity lookup screen.
 *
 * @param <E> type of entity
 */
@StudioAction(
        target = "io.jmix.ui.component.TagPicker",
        description = "Sets an entity to the tag picker using the entity lookup screen")
@ActionType(TagLookupAction.ID)
public class TagLookupAction<E> extends AbstractLookupAction<E>
        implements Action.ScreenOpeningAction, TagPicker.TagPickerAction, InitializingBean, Action.ExecutableAction {

    public static final String ID = "tag_lookup";

    protected TagPicker tagPicker;

    protected UiComponentProperties componentProperties;

    protected boolean multiSelect = true;
    protected boolean editable = true;

    public TagLookupAction() {
        super(ID);
    }

    public TagLookupAction(String id) {
        super(id);
    }

    @Autowired
    public void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP));
        setShortcut(componentProperties.getPickerLookupShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("entityPicker.action.lookup.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("entityPicker.action.lookup.tooltip"));
        }
    }

    @Override
    public void setTagPicker(@Nullable TagPicker tagPicker) {
        this.tagPicker = tagPicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);
        if (editable) {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP));
        } else {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP_READONLY));
        }
    }

    /**
     * @return whether multiselect enabled
     */
    public boolean isMultiSelect() {
        return multiSelect;
    }

    /**
     * Sets multiselect to the action. In the Lookup Screen it enables multiselect if
     * {@link LookupComponent} supports it. The default value is {@code true}.
     *
     * @param multiSelect whether multiselect should be enabled or not
     */
    @StudioPropertiesItem
    public void setMultiSelect(boolean multiSelect) {
        boolean oldValue = this.multiSelect;
        if (oldValue != multiSelect) {
            this.multiSelect = multiSelect;
            firePropertyChange(PROP_MULTISELECT, oldValue, multiSelect);
        }
    }

    /**
     * @return whether action is editable
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

    protected void setEditable(boolean editable) {
        boolean oldValue = this.editable;
        if (oldValue != editable) {
            this.editable = editable;
            firePropertyChange(PROP_EDITABLE, oldValue, editable);
        }
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            // call action perform handlers from super, delegate execution
            super.actionPerform(component);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        if (tagPicker == null) {
            throw new IllegalStateException("Action is not bound to a TagPicker");
        }

        MetaClass metaClass = tagPicker.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the TagPicker", "action ID", getId());
        }

        LookupBuilder builder = screenBuilders.lookup(tagPicker);

        builder = screenInitializer.initBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        Screen lookupScreen = builder.build();

        if (lookupScreen instanceof MultiSelectLookupScreen) {
            ((MultiSelectLookupScreen) lookupScreen).setLookupComponentMultiSelect(multiSelect);
        }

        screenInitializer.initScreen(lookupScreen);

        lookupScreen.show();
    }
}
