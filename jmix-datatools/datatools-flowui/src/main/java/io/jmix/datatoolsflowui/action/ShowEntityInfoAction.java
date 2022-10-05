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
package io.jmix.datatoolsflowui.action;

import com.vaadin.flow.component.Component;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.datatoolsflowui.accesscontext.FlowuiShowEntityInfoContext;
import io.jmix.datatoolsflowui.view.entityinfo.EntityInfoView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Views;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(ShowEntityInfoAction.ID)
public class ShowEntityInfoAction extends SecuredListDataComponentAction<ShowEntityInfoAction, Object>
        implements ExecutableAction {

    public static final String ID = "show_entity_info";

    protected boolean visibleBySpecificUiPermission = true;
    protected Views views;
    protected ViewSupport viewSupport;
    protected DialogWindows dialogWindows;

    public ShowEntityInfoAction() {
        this(ID);
    }

    public ShowEntityInfoAction(String id) {
        super(id);
    }

    @Autowired
    public void setViews(Views views) {
        this.views = views;
    }

    @Autowired
    public void setViewSupport(ViewSupport viewSupport) {
        this.viewSupport = viewSupport;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        setText(messages.getMessage(ShowEntityInfoAction.class, "showEntityInfoAction.title"));
    }

    @Autowired
    @Override
    protected void setAccessManager(AccessManager accessManager) {
        super.setAccessManager(accessManager);

        FlowuiShowEntityInfoContext context = new FlowuiShowEntityInfoContext();
        accessManager.applyRegisteredConstraints(context);

        visibleBySpecificUiPermission = context.isPermitted();
    }

    @Override
    public boolean isVisibleByUiPermissions() {
        return visibleBySpecificUiPermission
                && super.isVisibleByUiPermissions();
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    public void execute() {
        checkTarget();

        Object selectedItem = target.getSingleSelectedItem();

        if (selectedItem == null) {
            throw new IllegalStateException(String.format("There is not selected item in %s target",
                    getClass().getSimpleName()));
        }
        showInfo(selectedItem);
    }

    public void showInfo(Object entity) {
        DialogWindow<EntityInfoView> dialog = dialogWindows.view(findParent(), EntityInfoView.class)
                .build();
        dialog.getView().setEntity(entity);
        dialog.open();
    }

    protected View<?> findParent() {
        View<?> view = UiComponentUtils.findView((Component) target);
        if (view == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    target.getClass().getSimpleName()));
        }

        return view;
    }
}
