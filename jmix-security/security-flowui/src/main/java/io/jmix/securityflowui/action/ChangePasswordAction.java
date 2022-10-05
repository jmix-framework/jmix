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

package io.jmix.securityflowui.action;

import com.vaadin.flow.component.Component;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.securityflowui.view.changepassword.ChangePasswordView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@ActionType(ChangePasswordAction.ID)
public class ChangePasswordAction<E extends UserDetails>
        extends SecuredListDataComponentAction<ChangePasswordAction<E>, E>
        implements ExecutableAction, AdjustWhenViewReadOnly {

    public static final String ID = "changePassword";

    protected boolean currentPasswordRequired = false;
    protected DialogWindows dialogWindows;

    public ChangePasswordAction() {
        super(ID);
    }

    public ChangePasswordAction(String id) {
        super(id);
    }

    @Autowired
    protected void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.changePassword");
    }

    public void setCurrentPasswordRequired(boolean currentPasswordRequired) {
        this.currentPasswordRequired = currentPasswordRequired;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();

        FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isEditPermitted()) {
            return false;
        }

        return super.isPermitted();
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        E selectedItem = target.getSingleSelectedItem();
        if (selectedItem == null) {
            throw new IllegalStateException(String.format("There is not selected item in %s target",
                    getClass().getSimpleName()));
        }

        buildAndShowDialog(selectedItem);
    }

    protected void buildAndShowDialog(E selectedItem) {
        DialogWindow<ChangePasswordView> dialog = dialogWindows.view(findParent(), ChangePasswordView.class)
                .build();
        ChangePasswordView view = dialog.getView();
        view.setUsername(selectedItem.getUsername());
        view.setCurrentPasswordRequired(currentPasswordRequired);
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

    public ChangePasswordAction<E> withCurrentPasswordRequired(boolean currentPasswordRequired) {
        this.currentPasswordRequired = currentPasswordRequired;
        return this;
    }
}
