package ${packageName}

import com.vaadin.flow.router.Route
import io.jmix.flowui.app.main.StandardMainView
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor

@Route("")
@ViewController("${id}")
@ViewDescriptor("${descriptorName}.xml")
class ${controllerName} : StandardMainView() {
}