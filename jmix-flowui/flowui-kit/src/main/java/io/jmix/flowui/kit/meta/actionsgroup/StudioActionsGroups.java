package io.jmix.flowui.kit.meta.actionsgroup;

import io.jmix.flowui.kit.meta.StudioActionsGroup;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioActionsGroups {

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.valuepicker.PickerAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.component.PickerComponent"}
    )
    void pickerActions();

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.view.ViewAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.view.View"},
            unsupportedTarget = {"io.jmix.flowui.app.main.StandardMainView"}
    )
    void viewActions();

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.security.LogoutAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.app.main.StandardMainView"}
    )
    void mainViewActions();

    @StudioActionsGroup(
            name = "Actions",
            actionClassFqn = "io.jmix.flowui.action.list.ListDataComponentAction",
            xmlElement = "actions",
            icon = "io/jmix/flowui/kit/meta/icon/actionsgroup/actions.svg",
            target = {"io.jmix.flowui.component.ListDataComponent"}
    )
    void listDataComponentActions();
}
