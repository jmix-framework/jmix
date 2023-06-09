/*
 * Copyright 2022 Haulmont.
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

package component.standarddetailview.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.TestCopyingSystemStateEntity;

@Route(value = "TestCopyingSystemStateDetailTestView/detail")
@ViewController("TestCopyingSystemStateDetailTestView")
@ViewDescriptor("test-copying-system-state-detail-test-view.xml")
@EditedEntityContainer("testCopyingSystemStateDc")
public class TestCopyingSystemStateDetailTestView extends StandardDetailView<TestCopyingSystemStateEntity> {

    @Override
    protected void findEntityId(BeforeEnterEvent event) {
        // Because DTO entity cannot be loaded by Id, we need to prevent Id parsing from route parameters
    }
}
