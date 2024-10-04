package ${project_rootPackage}.view.main

import com.vaadin.flow.router.Route
import io.jmix.flowui.app.main.StandardMainView
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor

@Route("")
@ViewController(id = "${normalizedPrefix_underscore}MainView")
@ViewDescriptor(path = "main-view.xml")
open class MainView : StandardMainView()
