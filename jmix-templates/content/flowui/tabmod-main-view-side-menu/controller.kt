package ${packageName}

import ${project_rootPackage}.entity.User
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.avatar.AvatarVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.router.Route
import io.jmix.core.Messages
import io.jmix.core.usersubstitution.CurrentUserSubstitution
import io.jmix.flowui.UiComponents
import io.jmix.flowui.view.Install
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor
import io.jmix.tabbedmode.app.main.StandardTabbedModeMainView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails

<%if (!updateLayoutProperty) {%>/*
 * To use the view as a main view don't forget to set
 * new value (see @ViewController) to 'jmix.ui.main-view-id' property.
 * Also, the route of this view (see @Route) must differ from the route of default MainView.
 */<%}%>
@Route("")
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
class ${controllerName} : StandardTabbedModeMainView() {

    @Autowired
    private lateinit var messages: Messages

    @Autowired
    private lateinit var uiComponents: UiComponents

    @Autowired
    private lateinit var currentUserSubstitution: CurrentUserSubstitution

    @Install(to = "userMenu", subject = "buttonRenderer")
    private fun userMenuButtonRenderer(userDetails: UserDetails): Component? {
        val user: User = userDetails as User? ?: return null
        val userName: String = generateUserName(user)

        val content = uiComponents.create(Div::class.java)
        content.className = "user-menu-button-content"

        val avatar: Avatar = createAvatar(userName)

        val name = uiComponents.create(Span::class.java)
        name.text = userName
        name.className = "user-menu-text"

        content.add(avatar, name)

        if (isSubstituted(user)) {
            val subtext = uiComponents.create(Span::class.java)
            subtext.text = messages.getMessage("userMenu.substituted")
            subtext.className = "user-menu-subtext"

            content.add(subtext)
        }

        return content
    }

    @Install(to = "userMenu", subject = "headerRenderer")
    private fun userMenuHeaderRenderer(userDetails: UserDetails): Component? {
        val user: User = userDetails as User? ?: return null
        val name: String = generateUserName(user)

        val content = uiComponents.create(Div::class.java)
        content.className = "user-menu-header-content"

        val avatar: Avatar = createAvatar(name)
        avatar.addThemeVariants(AvatarVariant.LUMO_LARGE)

        val text = uiComponents.create(Span::class.java)
        text.text = name
        text.className = "user-menu-text"

        content.add(avatar, text)

        if (name == user.getUsername()) {
            text.addClassNames("user-menu-text-subtext")
        } else {
            val subtext = uiComponents.create(Span::class.java)
            subtext.text = user.getUsername()
            subtext.className = "user-menu-subtext"

            content.add(subtext)
        }

        return content
    }

    private fun createAvatar(fullName: String): Avatar {
        val avatar = uiComponents.create(Avatar::class.java)
        avatar.name = fullName
        avatar.element.setAttribute("tabindex", "-1")
        avatar.className = "user-menu-avatar"

        return avatar
    }

    private fun generateUserName(user: User): String {
        val userName = "\${user.firstName ?: ""} \${user.lastName ?: ""}".trim()

        if (userName.isEmpty()) {
            return user.username!!
        } else {
            return userName
        }
    }

    private fun isSubstituted(user: User?): Boolean {
        val authenticatedUser = currentUserSubstitution.authenticatedUser
        return user != null && authenticatedUser.username != user.username
    }
}