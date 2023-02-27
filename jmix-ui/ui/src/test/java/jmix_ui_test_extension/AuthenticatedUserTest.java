package jmix_ui_test_extension;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.testassist.UiTestAssistConfiguration;
import io.jmix.ui.testassist.junit.UiTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.UiTestConfiguration;
import test_support.entity.sec.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@UiTest(authenticatedUser = "admin", mainScreenId = "testMainScreen", screenBasePackages = "jmix_ui_test_extension.test_support")
@SpringBootTest(classes = {UiTestConfiguration.class, UiTestAssistConfiguration.class})
public class AuthenticatedUserTest  {

    @Autowired
    CurrentAuthentication currentAuthentication;

    @Test
    void currentAuthentication_isAdmin() {

        assertThat(currentUser().getLogin())
                .isEqualTo("admin");
    }

    @Nested
    class NestedJunit5Class {

        @Test
        void currentAuthentication_isAdmin() {

            assertThat(currentUser().getLogin())
                    .isEqualTo("admin");
        }
    }

    @NotNull
    private User currentUser() {
        return (User) currentAuthentication.getUser();
    }
}
