package ${packageName}

import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader

class ${loaderClassName} : AbstractComponentLoader<${componentClassName}>() {

    override fun createComponent(): ${componentClassName} = factory.create(${componentClassName}::class.java)

    override fun loadComponent() {
        // TODO load your component's XML attributes, e.g.:
        // loadString(element, "value") { resultComponent.value = it }
        componentLoader().loadClassNames(resultComponent, element)
    }
}
