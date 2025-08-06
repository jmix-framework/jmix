package io.jmix.flowui.app.usermenu;


import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.security.SubstituteUserAction;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.main.JmixUserIndicator;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.LinkedList;
import java.util.List;

@ViewController(id = "substituteUserView")
@ViewDescriptor(path = "substitute-user-view.xml")
@DialogMode(width = "30em", height = "40em", closeOnEsc = true)
public class SubstituteUserView extends StandardView {

    @ViewComponent
    protected BaseAction applyAction;
    @ViewComponent
    protected TypedTextField<String> userFilter;
    @ViewComponent
    protected JmixListBox<UserDetails> usersList;

    @Autowired
    protected Actions actions;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected UserSubstitutionManager userSubstitutionManager;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Messages messages;

    protected ListBoxListDataView<UserDetails> usersDataView;

    @Subscribe
    public void onInit(final InitEvent event) {
        initUsersList();
        refreshActions();
    }

    private void initUsersList() {
        List<UserDetails> currentAndSubstitutedUsers = new LinkedList<>();
        currentAndSubstitutedUsers.add(currentUserSubstitution.getAuthenticatedUser());
        currentAndSubstitutedUsers.addAll(userSubstitutionManager.getCurrentSubstitutedUsers());

        ListDataProvider<UserDetails> dataProvider = createUsersDataProvider(currentAndSubstitutedUsers);
        usersDataView = usersList.setItems(dataProvider);
        usersList.setValue(currentUserSubstitution.getEffectiveUser());
    }

    protected ListDataProvider<UserDetails> createUsersDataProvider(List<UserDetails> currentAndSubstitutedUsers) {
        ListDataProvider<UserDetails> dataProvider = new ListDataProvider<>(currentAndSubstitutedUsers);
        dataProvider.setFilter(userDetails -> {
            String filterString = userFilter.getTypedValue();
            return Strings.isNullOrEmpty(filterString)
                    || StringUtils.containsIgnoreCase(metadataTools.getInstanceName(userDetails), filterString);
        });
        return dataProvider;
    }

    @Subscribe("applyAction")
    public void onApplyAction(final ActionPerformedEvent event) {
        UserDetails newUser = usersList.getValue();

        if (newUser == null
                || currentUserSubstitution.getEffectiveUser().equals(newUser)) {
            return;
        }

        dialogs.createOptionDialog()
                // TODO: gg, duplicate messages
                .withHeader(messages.getMessage(JmixUserIndicator.class, "substitutionConfirmation.header"))
                .withText(messages.formatMessage(JmixUserIndicator.class, "substitutionConfirmation.text",
                        metadataTools.getInstanceName(newUser)))
                .withActions(
                        ((SubstituteUserAction) actions.create(SubstituteUserAction.ID))
                                .withUsers(currentUserSubstitution.getEffectiveUser(), newUser)
                                .withText(messages.getMessage("actions.Ok"))
                                .withIcon(VaadinIcon.CHECK.create())
                                .withVariant(ActionVariant.PRIMARY),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .open();
    }

    @Subscribe("usersList")
    public void onUsersListComponentValueChange(final ComponentValueChangeEvent<JmixListBox<?>, ?> event) {
        refreshActions();
    }

    @Subscribe("userFilter")
    public void onUserFilterTypedValueChange(final TypedValueChangeEvent<TypedTextField<String>, String> event) {
        UserDetails value = usersList.getValue();
        usersDataView.refreshAll();

        if (usersDataView.getItems()
                .anyMatch(userDetails -> userDetails.equals(value))) {
            usersList.setValue(value);
        }
    }

    protected void refreshActions() {
        applyAction.setEnabled(usersList.getValue() != null
                && !usersList.getValue().equals(currentUserSubstitution.getEffectiveUser()));
    }
}