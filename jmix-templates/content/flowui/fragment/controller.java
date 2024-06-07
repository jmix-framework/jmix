package ${packageName};

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;

<%if (classComment) {%>
        ${classComment}
<%}%>@FragmentDescriptor("${descriptorName}.xml")
public class ${controllerName} extends Fragment<VerticalLayout> {
}