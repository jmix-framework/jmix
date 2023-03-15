/*
 * Copyright 2023 Haulmont.
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

package io.jmix.ui.action.screen;

import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.LookupComponent;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.StandardLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@StudioAction(
        target = "io.jmix.ui.screen.Screen",
        description = "Selects item in lookup screen"
)
@ActionType(LookupSelectAction.ID)
public class LookupSelectAction<T> extends OperationResultScreenAction<LookupSelectAction<T>, StandardLookup<T>> {

    private static final Logger log = LoggerFactory.getLogger(LookupSelectAction.class);

    public static final String ID = "lookup_select";

    protected Subscription selectionListenerSubscription;

    public LookupSelectAction() {
        this(ID);
    }

    public LookupSelectAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        this.primary = true;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.LOOKUP_OK);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Select");
    }

    @Autowired
    public void setUiScreenProperties(UiScreenProperties uiScreenProperties) {
        setShortcut(uiScreenProperties.getCommitShortcut());
    }

    @Override
    public void setTarget(@Nullable StandardLookup<T> target) {
        super.setTarget(target);

        attachSelectionListener();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && hasSelectedItems();
    }

    protected boolean hasSelectedItems() {
        return !target.getLookupComponent().getLookupSelectedItems().isEmpty();
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.select(null);

        super.execute();
    }

    @SuppressWarnings("unchecked")
    protected void attachSelectionListener() {
        if (selectionListenerSubscription != null) {
            selectionListenerSubscription.remove();
            selectionListenerSubscription = null;
        }

        LookupComponent<T> lookupComponent = target.getLookupComponent();

        if (lookupComponent instanceof LookupComponent.LookupSelectionChangeNotifier) {
            selectionListenerSubscription = ((LookupComponent.LookupSelectionChangeNotifier<T>) lookupComponent)
                    .addLookupValueChangeListener(this::onSelectionChange);
        } else if (lookupComponent instanceof HasValue) {
            selectionListenerSubscription = ((HasValue<T>) lookupComponent)
                    .addValueChangeListener(this::onValueChange);
        } else {
            log.info("{} does not have lookup component", target.getClass().getName());
        }
    }

    protected void onSelectionChange(LookupComponent.LookupSelectionChangeEvent<T> event) {
        refreshState();
    }

    protected void onValueChange(HasValue.ValueChangeEvent<T> event) {
        refreshState();
    }
}
