package ${packageName};

import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class ${loaderClassName} extends AbstractComponentLoader<${componentClassName}> {

    @Override
    protected ${componentClassName} createComponent() {
        return factory.create(${componentClassName}.class);
    }

    @Override
    public void loadComponent() {
        // TODO load your component's XML attributes, e.g.:
        // loadString(element, "value", resultComponent::setValue);
        componentLoader().loadClassNames(resultComponent, element);
    }
}
