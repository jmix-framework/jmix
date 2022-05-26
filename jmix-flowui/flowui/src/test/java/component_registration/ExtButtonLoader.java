package component_registration;

import io.jmix.flowui.xml.layout.loader.component.ButtonLoader;

public class ExtButtonLoader extends ButtonLoader {

    public static final String DEFAULT_TEXT = "default";

    @Override
    public void loadComponent() {
        super.loadComponent();

        resultComponent.setText(DEFAULT_TEXT);
    }
}
