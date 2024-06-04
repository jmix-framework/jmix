/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package autowire.view;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.dropdownbutton.ComponentItem;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Customer;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Route("view-subscribe-dependency-injector-view")
@ViewController("ViewSubscribeDependencyInjectorView")
@ViewDescriptor("view-subscribe-dependency-injector-view.xml")
public class ViewSubscribeDependencyInjectorView extends StandardView {

    @Autowired
    protected Metadata metadata;

    @ViewComponent
    protected CollectionLoader<Customer> collectionDl;
    @ViewComponent
    protected CollectionContainer<Customer> collectionDc;

    @ViewComponent
    protected Action testAction;
    @ViewComponent
    protected TypedTextField<String> testComponent;
    @ViewComponent
    protected JmixTabSheet testComponent2;
    @ViewComponent
    protected EntityPicker<Customer> hasActionComponent;
    @ViewComponent
    protected DropdownButton dropdownButton;

    private final Map<String, Boolean> executedMap = new HashMap<>();

    @Subscribe
    protected void onInit(InitEvent event) {
        markAsExecuted(event);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        markAsExecuted(event);
    }

    @Subscribe
    protected void onReady(ReadyEvent event) {
        markAsExecuted(event);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onDataContextPreSave(DataContext.PreSaveEvent event) {
        markAsExecuted(event);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onDataContextPostSave(DataContext.PostSaveEvent event) {
        markAsExecuted(event);
    }

    @Subscribe(id = "collectionDl", target = Target.DATA_LOADER)
    protected void onCollectionDlPreLoad(CollectionLoader.PreLoadEvent<Customer> event) {
        markAsExecuted(event);
    }

    @Subscribe(id = "collectionDl", target = Target.DATA_LOADER)
    protected void onCollectionDlPostLoad(CollectionLoader.PostLoadEvent<Customer> event) {
        markAsExecuted(event);
    }

    @Subscribe(id = "collectionDc", target = Target.DATA_CONTAINER)
    protected void onCollectionDcChange(CollectionContainer.CollectionChangeEvent<Customer> event) {
        markAsExecuted(event);
    }

    @Subscribe(id = "collectionDc", target = Target.DATA_CONTAINER)
    protected void onCollectionDcItemChange(InstanceContainer.ItemChangeEvent<Customer> event) {
        markAsExecuted(event);
    }

    @Subscribe(id = "collectionDc", target = Target.DATA_CONTAINER)
    protected void onCollectionDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Customer> event) {
        markAsExecuted(event);
    }

    @Subscribe("testAction")
    protected void onTextAction(ActionPerformedEvent event) {
        markAsExecuted(event);
    }

    @Subscribe("testAction")
    protected void onTextActionPropertyChange(PropertyChangeEvent event) {
        markAsExecuted(event);
    }

    @Subscribe("testComponent")
    protected void onTestComponentTypedValueChangeEvent(TypedValueChangeEvent<TypedTextField<String>, String> event) {
        markAsExecuted(event);
    }

    @Subscribe("testComponent")
    protected void onTestComponentValueChangeEvent(ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        markAsExecuted(event);
    }

    @Subscribe("testComponent2")
    protected void onTestComponent2SelectedChangeEventEvent(JmixTabSheet.SelectedChangeEvent event) {
        markAsExecuted("SelectedChangeEvent");
    }

    @Subscribe("hasActionComponent.testAction")
    protected void onHasActionComponentTestActionPerformed(ActionPerformedEvent event) {
        markAsExecuted("HasActionComponent.ActionPerformedEvent");
    }

    @Subscribe("dropdownButton.componentItem.button")
    protected void onDropdownButtonComponentItemChildClick(ClickEvent<JmixButton> event) {
        markAsExecuted("DropdownButton.NestedElement.ClickEvent");
    }

    public void publishDataEvents() {
        // publish DataContext events
        getViewData().getDataContext().save();
        // publish DataLoader events
        collectionDl.load();

        Customer customer = metadata.create(Customer.class);
        // publish CollectionDcChange event
        collectionDc.setItems(Collections.singleton(customer));
        // publish CollectionItemPropertyChange
        customer.setName("testName");
        // publish CollectionItemChange
        collectionDc.setItem(customer);
    }

    public void publishComponentEvents() {
        // publish ActionPerformedEvent
        testAction.actionPerform(this);
        // publish ActionPropertyChangeEvent
        testAction.setText("testAction");
        // publish ValueChangeEvent
        testComponent.setTypedValue("testComponent");
        // publish SelectedChangeEvent
        testComponent2.setSelectedIndex(1);
        // publish HasActionComponent.ActionPerformedEvent
        Objects.requireNonNull(hasActionComponent.getAction("testAction")).actionPerform(hasActionComponent);
        // publish ClickEvent
        ((JmixButton) ((ComponentItem) Objects.requireNonNull(dropdownButton.getItem("componentItem"))).getContent()).click();
    }

    protected void markAsExecuted(Object event) {
        executedMap.put(event.getClass().getSimpleName(), true);
    }

    protected void markAsExecuted(String eventName) {
        executedMap.put(eventName, true);
    }

    public boolean checkExecutedEvent(String eventName) {
        return executedMap.get(eventName) != null && executedMap.get(eventName);
    }
}
