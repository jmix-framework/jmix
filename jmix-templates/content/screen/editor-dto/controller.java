package ${packageName};

import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.ui.screen.*;
import ${entity.fqn};

import java.util.Collections;
import java.util.Set;

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${editorId}")
@UiDescriptor("${editorDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${editorControllerName} extends StandardEditor<${entity.className}> {

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private ${entity.className} ${dlId}LoadDelegate(LoadContext<${entity.className}> loadContext) {
        // Here you can load entity from an external store by ID passed in LoadContext
        return getEditedEntity();
    }

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> commitDelegate(SaveContext saveContext) {
        // Here you can save the edited entity or the whole SaveContext in an external store.
        // Return the set of saved instances.
        return Collections.singleton(getEditedEntity());
    }
}