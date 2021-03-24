package ${packageName}

import com.haulmont.cuba.gui.Route
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.screen.*
import com.haulmont.cuba.web.app.login.LoginScreen
import com.haulmont.cuba.web.gui.screen.ScreenDependencyUtils
import com.vaadin.ui.Dependency
import org.springframework.beans.factory.annotation.Autowired

<%if (classComment) {%>
${classComment}<%}%>
@Route(path = "login", root = true)
@UiController("${api.escapeKotlinDollar(id)}")
@UiDescriptor("${descriptorName}.xml")
class ${controllerName} : LoginScreen(){

    @Autowired
    private lateinit var bottomPanel: HBoxLayout

    @Autowired
    private lateinit var poweredByLink: Label<String>

    @Subscribe
    fun onAppLoginScreenInit(event: Screen.InitEvent) {
        loadStyles()
        initBottomPanel()
    }

    @Subscribe("submit")
    fun onSubmit(event: Action.ActionPerformedEvent) {
        login()
    }

    private fun loadStyles() {
        ScreenDependencyUtils.addScreenDependency(this,
                "vaadin://brand-login-screen/login.css", Dependency.Type.STYLESHEET)
    }

    private fun initBottomPanel() {
        if (!globalConfig.getLocaleSelectVisible()) {
            poweredByLink.setAlignment(Component.Alignment.MIDDLE_CENTER);

            if (!webConfig.getLoginDialogPoweredByLinkVisible()) {
                bottomPanel.setVisible(false)
            }
        }
    }

    override fun initLogoImage() {
        logoImage.setSource(RelativePathResource::class.java)
                .setPath("VAADIN/brand-login-screen/cuba-icon-login.svg")
    }
}
