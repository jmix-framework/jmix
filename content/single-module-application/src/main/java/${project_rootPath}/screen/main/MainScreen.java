package ${project_rootPackage}.screen.main;

import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("${project_idPrefix}_MainScreen")
@UiDescriptor("main-screen.xml")
public class MainScreen extends Screen implements Window.HasWorkArea {

    @Autowired
    private AppWorkArea workArea;

    @Override
    public AppWorkArea getWorkArea() {
        return workArea;
    }
}
