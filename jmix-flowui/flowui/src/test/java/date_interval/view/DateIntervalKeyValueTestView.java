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

package date_interval.view;

import com.vaadin.flow.router.Route;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.util.Collection;

@Route("date-interval-key-value-test-view")
@ViewController
@ViewDescriptor("date-interval-key-value-test-view.xml")
public class DateIntervalKeyValueTestView extends StandardView {

    @ViewComponent
    public KeyValueCollectionContainer dateTimesDc;
    @ViewComponent
    public PropertyFilter<? super DateInterval> dateFilter;

    public Collection<KeyValueEntity> getItems() {
        return dateTimesDc.getItems();
    }
}
