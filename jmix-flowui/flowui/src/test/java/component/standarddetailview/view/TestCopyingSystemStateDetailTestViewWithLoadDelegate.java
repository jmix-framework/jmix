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

import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.TestCopyingSystemStateEntity;

@Route(value = "TestCopyingSystemStateDetailTestViewWithLoadDelegate/:id")
@ViewController("TestCopyingSystemStateDetailTestViewWithLoadDelegate")
@ViewDescriptor("test-copying-system-state-detail-test-view-with-load-delegate.xml")
@EditedEntityContainer("testCopyingSystemStateDc")
public class TestCopyingSystemStateDetailTestViewWithLoadDelegate extends StandardDetailView<TestCopyingSystemStateEntity> {
    @Autowired
    private Metadata metadata;

    @Install(to = "testCopyingSystemStateDl", target = Target.DATA_LOADER)
    protected TestCopyingSystemStateEntity loadDelegate(LoadContext<TestCopyingSystemStateEntity> context) {
        return metadata.create(TestCopyingSystemStateEntity.class);
    }
}
