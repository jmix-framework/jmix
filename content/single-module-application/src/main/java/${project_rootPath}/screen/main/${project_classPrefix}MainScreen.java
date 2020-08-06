package ${project_rootPackage}.screen.main;

import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.LoadDataBeforeShow;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("${project_idPrefix}_MainScreen")
@UiDescriptor("${project_idPrefix}-main-screen.xml")
@LoadDataBeforeShow
public class ${project_classPrefix}MainScreen extends Screen implements Window.HasWorkArea {
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }
}
