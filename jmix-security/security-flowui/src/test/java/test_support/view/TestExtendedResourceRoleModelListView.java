/*
 * Copyright 2026 Haulmont.
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

package test_support.view;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.springframework.core.annotation.Order;

import java.util.Comparator;
import java.util.Objects;

/**
 * An application view that extends the list of resource roles and configures the columns of the grid its own way.
 * Its descriptor makes the description column unsortable.
 */
@Route("test-extended-resource-role-models")
@ViewController("test_ExtendedResourceRoleModel.list")
@ViewDescriptor("test-extended-resource-role-model-list-view.xml")
public class TestExtendedResourceRoleModelListView extends ResourceRoleModelListView {

    @ViewComponent
    private DataGrid<ResourceRoleModel> roleModelsTable;

    @Supply(to = "roleModelsTable.name", subject = "renderer")
    public Renderer<ResourceRoleModel> roleModelsTableNameRenderer() {
        return new TextRenderer<>(roleModel -> "CUSTOM " + roleModel.getCode());
    }

    /**
     * Runs before the handlers of the parent, which have no order.
     */
    @Subscribe
    @Order(10)
    public void onInitNameComparator(InitEvent event) {
        Objects.requireNonNull(roleModelsTable.getColumnByKey("name"))
                .setComparator(Comparator.comparing(ResourceRoleModel::getCode).reversed());
    }
}
