package io.jmix.securityui.screen.user;


import io.jmix.core.EntityStates;
import io.jmix.security.entity.User;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.screen.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;
import java.util.Objects;

@UiController("sec_User.edit")
@UiDescriptor("user-edit.xml")
@EditedEntityContainer("userDc")
@LoadDataBeforeShow
public class UserEdit extends StandardEditor<User> {

    @Inject
    private EntityStates entityStates;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private PasswordField passwordField;

    @Inject
    private PasswordField confirmPasswordField;

    @Inject
    private Notifications notifications;

    @Inject
    private MessageBundle messageBundle;

    @Subscribe
    public void onInitEntity(InitEntityEvent<User> event) {
        passwordField.setVisible(true);
        confirmPasswordField.setVisible(true);
    }

//    @Subscribe(target = Target.DATA_CONTEXT)
//    public void onPreCommit(DataContext.PreCommitEvent event) {
//        if (entityStates.isNew(getEditedEntity())) {
//            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));
//        }
//    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            if (!Objects.equals(passwordField.getValue(), confirmPasswordField.getValue())) {
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messageBundle.getMessage("passwordsDoNotMatch"))
                        .show();
                event.preventCommit();
            }
            getEditedEntity().setPassword(passwordEncoder.encode(passwordField.getValue()));
        }
    }

}