<%
        def contentClassFqn = rootComponent == 'div' ? 'com.vaadin.flow.component.html.Div'
                : rootComponent == 'hbox' ? 'com.vaadin.flow.component.orderedlayout.HorizontalLayout'
                : rootComponent == 'vbox' ? 'com.vaadin.flow.component.orderedlayout.VerticalLayout'
                : rootComponent == 'gridLayout' ? 'io.jmix.flowui.component.gridlayout.GridLayout'
                : rootComponent == 'formLayout' ? 'io.jmix.flowui.component.formlayout.JmixFormLayout'
                : 'com.vaadin.flow.component.orderedlayout.VerticalLayout'
        def contentClassSimpleName = contentClassFqn.substring(contentClassFqn.lastIndexOf('.') + 1)
        if (contentClassSimpleName == 'GridLayout') {
            contentClassSimpleName += '<*>'
        }
%>
package ${packageName}

import ${contentClassFqn}
import io.jmix.flowui.fragment.Fragment
import io.jmix.flowui.fragment.FragmentDescriptor

<%if (classComment) {%>
        ${classComment}
<%}%>@FragmentDescriptor("${descriptorName}.xml")
class ${controllerName} : Fragment<${contentClassSimpleName}>() {
}