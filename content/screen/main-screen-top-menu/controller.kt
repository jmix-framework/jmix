package ${packageName}

import io.jmix.ui.component.Window.HasWorkArea
import org.springframework.beans.factory.annotation.Autowired
import io.jmix.ui.ScreenTools
import io.jmix.ui.component.AppWorkArea
import io.jmix.ui.navigation.Route
import io.jmix.ui.screen.*

@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
@Route(path = "main", root = true)
class ${controllerName} : Screen(), HasWorkArea {
    @Autowired
    private lateinit var screenTools: ScreenTools

    @Autowired
    private lateinit var workArea: AppWorkArea

    override fun getWorkArea(): AppWorkArea = workArea

    @Subscribe
    fun onAfterShow(event: AfterShowEvent?) {
        screenTools.openDefaultScreen(
            UiControllerUtils.getScreenContext(this).screens
        )
        screenTools.handleRedirect()
    }
}