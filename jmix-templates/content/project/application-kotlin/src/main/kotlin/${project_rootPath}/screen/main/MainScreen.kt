package ${project_rootPackage}.screen.main

import io.jmix.ui.ScreenTools
import io.jmix.ui.component.AppWorkArea
import io.jmix.ui.component.Button
import io.jmix.ui.component.Window.HasWorkArea
import io.jmix.ui.component.mainwindow.Drawer
import io.jmix.ui.icon.JmixIcon
import io.jmix.ui.navigation.Route
import io.jmix.ui.screen.*
import org.springframework.beans.factory.annotation.Autowired

@UiController("${normalizedPrefix_underscore}MainScreen")
@UiDescriptor("main-screen.xml")
@Route(path = "main", root = true)
open class MainScreen : Screen(), HasWorkArea {

    @Autowired
    private lateinit var screenTools: ScreenTools

    @Autowired
    private lateinit var workArea: AppWorkArea

    @Autowired
    private lateinit var drawer: Drawer

    @Autowired
    private lateinit var collapseDrawerButton: Button

    override fun getWorkArea(): AppWorkArea = workArea

    @Subscribe("collapseDrawerButton")
    private fun onCollapseDrawerButtonClick(event: Button.ClickEvent) {
        drawer.run {
            toggle()
            if (isCollapsed) {
                collapseDrawerButton.setIconFromSet(JmixIcon.CHEVRON_RIGHT)
            } else {
                collapseDrawerButton.setIconFromSet(JmixIcon.CHEVRON_LEFT)
            }
        }
    }

    @Subscribe
    fun onAfterShow(event: AfterShowEvent?) {
        screenTools.run {
            openDefaultScreen(
                UiControllerUtils.getScreenContext(this@MainScreen).screens
            )
            handleRedirect()
        }
    }
}