package io.jmix.securityui.screen.user;

import io.jmix.security.entity.User;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;

@UiController("sec_User.browse")
@UiDescriptor("user-browse.xml")
@LookupComponent("standardUsersTable")
@Route("users")
@LoadDataBeforeShow
public class UserBrowse extends StandardLookup<User> {
}