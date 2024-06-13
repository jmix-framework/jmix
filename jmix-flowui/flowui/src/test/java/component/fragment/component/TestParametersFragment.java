package component.fragment.component;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ViewComponent;
import test_support.entity.sales.Product;

@FragmentDescriptor("test-parameters-fragment.xml")
public class TestParametersFragment extends Fragment<FormLayout> {

    @ViewComponent
    private EntityComboBox<Product> entityField;
    @ViewComponent
    private TypedTextField<String> textField;

    public void setContainer(CollectionContainer<Product> container) {
        entityField.setItems(container);
    }

    public void setPlaceholder(String placeholder) {
        textField.setPlaceholder(placeholder);
    }
}