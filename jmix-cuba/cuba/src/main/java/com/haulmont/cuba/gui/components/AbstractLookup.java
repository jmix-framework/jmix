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

import io.jmix.core.Messages;
import io.jmix.ui.Fragments;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base class for lookup screen controllers.
 *
 * @deprecated Use {@link io.jmix.ui.screen.StandardLookup} APIs instead.
 */
@Deprecated
public class AbstractLookup extends AbstractWindow
        implements com.haulmont.cuba.gui.components.Window.Lookup {

    private Predicate<ValidationContext> lookupValidator;
    private Consumer<Collection> lookupHandler;

    private Component lookupComponent;

    public AbstractLookup() {
        addInitListener(this::initLookupActions);
        addAfterInitListener(this::afterInit);
        addBeforeShowListener(this::beforeShow);
    }

    protected void initLookupActions(@SuppressWarnings("unused") InitEvent event) {
        addAction(new SelectAction(this));

        Messages messages = getApplicationContext().getBean(Messages.class);
        addAction(new BaseAction(LOOKUP_CANCEL_ACTION_ID)
                .withCaption(messages.getMessage("actions.Cancel"))
                .withHandler(e ->
                        close("cancel")
                ));
    }

    private void beforeShow(@SuppressWarnings("unused") BeforeShowEvent beforeShowEvent) {
        setupLookupComponent();
        setupCommitShortcut();
    }

    protected void setupCommitShortcut() {
        if (lookupHandler == null) {
            // window opened not as Lookup
            Action selectAction = getAction(LOOKUP_SELECT_ACTION_ID);
            if (selectAction != null) {
                selectAction.setShortcut(null);
            }
        }
    }

    protected void setupLookupComponent() {
        if (this.lookupHandler != null) {
            Component lookupComponent = getLookupComponent();
            if (lookupComponent instanceof LookupComponent) {
                ((LookupComponent<?>) lookupComponent).setLookupSelectHandler(this::selectItemsOnClick);
            }
        }
    }

    protected void afterInit(@SuppressWarnings("unused") AfterInitEvent event) {
        initLookupLayout();
    }

    @Override
    public Component getLookupComponent() {
        return lookupComponent;
    }

    @Override
    public void setLookupComponent(Component lookupComponent) {
        this.lookupComponent = lookupComponent;
    }

    @Override
    public void initLookupLayout() {
        Action selectAction = getAction(LOOKUP_SELECT_ACTION_ID);

        if (selectAction != null && selectAction.getOwner() == null) {
            Fragments fragments = UiControllerUtils.getScreenContext(this).getFragments();

            ScreenFragment lookupWindowActions = fragments.create(this, "lookupWindowActions");
            lookupWindowActions.getFragment().setId("lookupWindowActions");
            lookupWindowActions.getFragment().setVisible(false);

            getFrame().add(lookupWindowActions.getFragment());
        }

        Element element = ((Component.HasXmlDescriptor) getFrame()).getXmlDescriptor();
        if (element != null) {
            String lookupComponent = element.attributeValue("lookupComponent");
            if (StringUtils.isNotEmpty(lookupComponent)) {
                Component component = getFrame().getComponent(lookupComponent);
                setLookupComponent(component);
            }
        }

        Component lookupComponent = getLookupComponent();
        if (lookupComponent instanceof LookupComponent.LookupSelectionChangeNotifier) {
            LookupComponent.LookupSelectionChangeNotifier selectionNotifier = (LookupComponent.LookupSelectionChangeNotifier) lookupComponent;
            if (selectAction != null) {
                //noinspection unchecked
                selectionNotifier.addLookupValueChangeListener(valueChangeEvent ->
                        selectAction.setEnabled(!selectionNotifier.getLookupSelectedItems().isEmpty()));

                selectAction.setEnabled(!selectionNotifier.getLookupSelectedItems().isEmpty());
            }
        }
    }

    protected void selectItemsOnClick(@SuppressWarnings("unused") Collection collection) {
        Action selectAction = getActionNN(LOOKUP_SELECT_ACTION_ID);
        selectAction.actionPerform(getLookupComponent());
    }

    @Nullable
    @Override
    public Consumer<Collection> getSelectHandler() {
        return lookupHandler;
    }

    @Override
    public Predicate<ValidationContext> getSelectValidator() {
        return lookupValidator;
    }

    @Override
    public void setSelectValidator(Predicate lookupValidator) {
        this.lookupValidator = lookupValidator;
    }

    @Override
    public void setSelectHandler(@Nullable Consumer lookupHandler) {
        this.lookupHandler = lookupHandler;

        if (lookupHandler != null) {
            getComponentNN("lookupWindowActions").setVisible(true);
        }
    }
}
