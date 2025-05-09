/*
 * Copyright 2023 Haulmont.
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

package component.genericfilter.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "filter-metadata-tools-test-view")
@ViewController("FilterMetadataToolsTestView")
@ViewDescriptor("filter-metadata-tools-test-view.xml")
public class FilterMetadataToolsTestView extends StandardView {

    @ViewComponent
    public GenericFilter ordersFilter;

    @ViewComponent
    public GenericFilter mainDsEntityFilter;

    @ViewComponent
    public GenericFilter db1JpaEntityFilter;

    @ViewComponent
    public GenericFilter mem1DtoEntityFilter;

    @ViewComponent
    public GenericFilter mem2DtoEntityFilter;

    @ViewComponent
    public GenericFilter noStoreDtoEntityFilter;

    @ViewComponent
    public GenericFilter ordersKeyValueFilter;

    @ViewComponent
    public GenericFilter mainDsEntityKeyValueFilter;
}
