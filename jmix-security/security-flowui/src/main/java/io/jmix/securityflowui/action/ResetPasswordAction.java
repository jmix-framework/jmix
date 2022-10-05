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
import io.jmix.securityflowui.view.resetpassword.ResetPasswordView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@ActionType(ResetPasswordAction.ID)
public class ResetPasswordAction<E extends UserDetails>
        extends SecuredListDataComponentAction<ResetPasswordAction<E>, E>
        implements ExecutableAction, AdjustWhenViewReadOnly {

    public static final String ID = "resetPassword";

    protected DialogWindows dialogWindows;

    public ResetPasswordAction() {
        super(ID);
    }

    public ResetPasswordAction(String id) {
        super(id);
    }

    @Autowired
    protected void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.resetPassword");
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

        buildAndShowDialog();
    }

    protected void buildAndShowDialog() {
        findParent().ifPresent(parent -> {
            DialogWindow<ResetPasswordView> dialog = dialogWindows.view(parent, ResetPasswordView.class)
                    .build();
            ResetPasswordView view = dialog.getView();
            view.setUsers(target.getSelectedItems());
            dialog.open();
        });

    }

    protected Optional<View<?>> findParent() {
        return target instanceof Component
                ? Optional.ofNullable(UiComponentUtils.findView((Component) target))
                : Optional.empty();
    }
}
