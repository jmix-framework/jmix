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

package component.standardreadview.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.ReadEntityContainer;
import io.jmix.flowui.view.StandardReadView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.Order;

// The container of this view declares no loader on purpose.
@Route(value = "NoLoaderReadTestView/:id")
@ViewController("NoLoaderReadTestView")
@ViewDescriptor("no-loader-read-test-view.xml")
@ReadEntityContainer("orderDc")
public class NoLoaderReadTestView extends StandardReadView<Order> {
}
