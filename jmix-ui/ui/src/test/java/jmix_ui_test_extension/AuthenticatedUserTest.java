package jmix_ui_test_extension;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.ui.Screens;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.testassist.UiTestAssistConfiguration;
import io.jmix.ui.testassist.junit.UiTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import test_support.UiTestConfiguration;
import test_support.UiTestSecurityConfiguration;
import test_support.entity.sec.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(authenticatedUser = "admin",
        mainScreenId = "testMainScreen",
        screenBasePackages = "jmix_ui_test_extension.test_support")
@ContextConfiguration(classes = {UiTestConfiguration.class, UiTestAssistConfiguration.class,
        UiTestSecurityConfiguration.class, UiConfiguration.class, EclipselinkConfiguration.class,
        DataConfiguration.class, CoreConfiguration.class})
public class AuthenticatedUserTest {

    @Autowired
    CurrentAuthentication currentAuthentication;

    @Test
    @DisplayName("Checks that current authentication is admin")
    void checkCurrentAuthenticationIsAdmin() {
        assertEquals("admin", currentUser().getLogin());
    }

    @Nested
    class NestedClass {

        @Test
        @DisplayName("Checks that current authentication is admin")
        void checkCurrentAuthenticationIsAdmin() {
            assertEquals("admin", currentUser().getLogin());
        }
    }

    @Nested
    @UiTest(authenticatedUser = "anotherUser", mainScreenId = "anotherScreenId", screenBasePackages = "com.example.app")
    class NestedClassWithUiTestAnnotation {

        @Test
        @DisplayName("Checks that current authentication is still admin")
        void checkCurrentAuthenticationIsAdmin() {
            assertEquals("admin", currentUser().getLogin());
        }

        @Test
        @DisplayName("Checks that mainScreenId is still testMainScreen")
        void checkMainScreenId(Screens screens) {
            Screens.OpenedScreens openedScreens = screens.getOpenedScreens();
            assertEquals("testMainScreen", openedScreens.getRootScreen().getId());
        }

        @Test
        @DisplayName("Checks that screen base packages are not changed")
        void checkScreenBasePackages(Screens screens) {
            // Outer class provides package for TestMainScreen, we should check
            // that no exceptions thrown and TestMainScreen is opened
            Screens.OpenedScreens openedScreens = screens.getOpenedScreens();
            assertEquals("testMainScreen", openedScreens.getRootScreen().getId());
        }
    }

    @NotNull
    private User currentUser() {
        return (User) currentAuthentication.getUser();
    }
}
