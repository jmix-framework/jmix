/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package spec.haulmont.cuba.web.components.pickerfield

import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.DatasourceImpl
import com.haulmont.cuba.gui.components.PickerField
import io.jmix.core.FetchPlan
import io.jmix.core.common.event.Subscription
import io.jmix.core.impl.StandardSerialization
import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.User
import io.jmix.ui.component.Component
import io.jmix.ui.component.HasValue
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.web.UiScreenSpec

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class PickerFieldTest extends UiScreenSpec {

    @Autowired
    StandardSerialization standardSerialization

    def testNew() {
        when:
        Component component = cubaUiComponents.create(PickerField.NAME)
        then:
        component != null
        component instanceof PickerField
    }


    def testGetSetValue() {
        when:
        PickerField component = cubaUiComponents.create(PickerField.class)

        then:
        component.value == null

        when:
        component.setValue("One")

        then:
        thrown IllegalStateException
        component.value == null

        when:

        component.setMetaClass(metadata.getClass(User.class))
        component.setValue("One")

        then:
        thrown IllegalArgumentException
        component.value == null

        when:

        User user = new User()
        user.setLogin("admin")

        component.setMetaClass(metadata.getClass(User.class))
        component.setValue(user)

        then:
        user == component.value
    }


    def testSetToReadonly() {
        when:
        PickerField component = cubaUiComponents.create(PickerField.class)

        component.setEditable(false)
        component.setMetaClass(metadata.getClass(User.class))
        then:
        !component.editable

        when:
        User user = new User()
        user.setLogin("admin")
        component.setValue(user)

        then:
        user == component.value
        !component.editable
    }


    def testSetToReadonlyFromValueListener() {
        when:
        PickerField component = cubaUiComponents.create(PickerField.class)

        component.setMetaClass(metadata.getClass(User.class))
        then:
        component.editable

        when:
        component.addValueChangeListener({e -> component.setEditable(false)})

        User user = new User()
        user.setLogin("admin")
        component.setValue(user)

        then:
        user == component.value
        !component.editable
    }


    def testDatasource() {
        when:
        PickerField component = cubaUiComponents.create(PickerField.class)

        //noinspection unchecked
        def testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource() as Datasource<User>

        testDs.setItem(new User())
        ((DatasourceImpl) testDs).valid()

        then:
        component.value == null

        when:

        Group g = new Group()
        testDs.getItem().setGroup(g)

        Group g1 = new Group()
        Group g2 = new Group()

        component.setMetaClass(metadata.getClass(Group.class))

        component.setValue(g2)
        component.setDatasource(testDs, "group")

        then:
        g == component.value

        when:
        component.setValue(g1)

        then:
        g1 == testDs.item.group

        when:
        testDs.getItem().setGroup(g2)

        then:
        g2 == component.value
    }


    def testValueChangeListener() {
        when:
        PickerField component = cubaUiComponents.create(PickerField.class)

        final AtomicInteger counter = new AtomicInteger(0)

        //noinspection unchecked
        def testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource() as Datasource<User>

        testDs.setItem(new User())
        ((DatasourceImpl) testDs).valid()

        then:
        component.value == null

        when:
        final Group g = new Group()
        testDs.getItem().setGroup(g)

        final Group g1 = new Group()
        final Group g2 = new Group()

        Consumer<HasValue.ValueChangeEvent> listener1 = { e ->
            counter.addAndGet(1)
        }
        Subscription subscription = component.addValueChangeListener(listener1)

        component.setMetaClass(metadata.getClass(Group.class))
        component.setValue(g2)

        subscription.remove()

        then:
        counter.get() == 1

        when:
        Consumer<HasValue.ValueChangeEvent> listener2 = { e ->
            counter.addAndGet(1)
        }
        subscription = component.addValueChangeListener(listener2)

        component.setDatasource(testDs, "group")

        then:
        g == component.value
        counter.get() == 2

        when:

        subscription.remove()
        component.setValue(g1)

        then:
        g1 == testDs.item.group
        counter.get() == 2

        when:

        Consumer<HasValue.ValueChangeEvent> listener3 = { e ->
            counter.addAndGet(1)
        }

        subscription = component.addValueChangeListener(listener3)
        testDs.getItem().setGroup(g2)

        then:
        g2 == component.value
        counter.get() == 3

        when:
        subscription.remove()

        component.setValue(g)
        Group gCopy = (Group) standardSerialization.deserialize(standardSerialization.serialize(g))

        Consumer<HasValue.ValueChangeEvent> listener4 = { e ->
            counter.addAndGet(1)
        }
        subscription = component.addValueChangeListener(listener4)
        component.setValue(gCopy)

        then:
        counter.get() == 4

        when:
        subscription.remove()

        Consumer<HasValue.ValueChangeEvent> listener5 = { e ->

            counter.addAndGet(1)
        }
        subscription = component.addValueChangeListener(listener5)
        testDs.getItem().setGroup(g)

        then:
        counter.get() == 5

        subscription.remove()
    }
}
