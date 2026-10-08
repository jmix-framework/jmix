package ${packageName};

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;<% if (generateFrontend) { %>
import com.vaadin.flow.component.dependency.JsModule;<% } %>

@Tag("${tag}")<% if (generateFrontend) { %>
@JsModule("${jsModulePath}")<% } else { %>
// TODO add @JsModule/@NpmPackage pointing to your frontend module<% } %>
public class ${componentClassName} extends Component {

    public ${componentClassName}() {
    }
}
