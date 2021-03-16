/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License')
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spec.haulmont.cuba.web.components.textfield

import com.haulmont.chile.core.datatypes.Datatypes
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.gui.components.TextField
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.DatasourceImpl
import io.jmix.core.FetchPlan
import io.jmix.core.common.event.Subscription
import io.jmix.ui.component.Component
import io.jmix.ui.component.HasValue
import spec.haulmont.cuba.web.UiScreenSpec

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class TextFieldTest extends UiScreenSpec {

    def testNew() {
        when:
        Component component = cubaUiComponents.create(TextField)

        then:
        component != null
        component instanceof TextField
    }


    def testGetSetValue() {
        when:
        TextField component = cubaUiComponents.create(TextField.class)

        then:
        component.value == null

        when:
        component.value = 'Test'

        then:
        component.value == 'Test'
    }


    def testGetSetInteger() {
        when:
        TextField component = cubaUiComponents.create(TextField.class)

        then:
        component.value == null

        when:
        component.datatype = Datatypes.getNN(Integer)
        component.value = 10

        then:
        component.value == 10
    }


    def testSetToReadonly() {
        when:
        TextField component = cubaUiComponents.create(TextField.class)

        component.editable = false

        then:
        !component.editable

        when:
        component.value = 'OK'

        then:
        component.value == 'OK'
        !component.editable
    }


    def testSetToReadonlyFromValueListener() {
        when:
        TextField component = cubaUiComponents.create(TextField.class)

        then:
        component.editable

        when:
        component.addValueChangeListener({ e -> component.setEditable(false) })

        component.value = 'OK'

        then:
        component.value == 'OK'
        !component.editable
    }


    def testDatasource() {
        when:
        TextField component = cubaUiComponents.create(TextField.class)

        //noinspection unchecked
        def testDs = new DsBuilder()
                .setId('testDs')
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource() as Datasource<User>

        testDs.setItem(new User())
        ((DatasourceImpl) testDs).valid()

        then:
        component.value == null

        when:
        testDs.getItem().setLogin('Ok')

        component.setValue('none')
        component.setDatasource(testDs, 'login')

        then:
        component.value == 'Ok'

        when:
        component.setValue('user')

        then:
        testDs.item.login == 'user'

        when:
        testDs.item.login = 'login'

        then:
        component.value == 'login'
    }


    def testValueChangeListener() {
        when:
        TextField component = cubaUiComponents.create(TextField.class)

        AtomicInteger counter = new AtomicInteger(0)

        Consumer<HasValue.ValueChangeEvent> okListener = { e -> counter.addAndGet(1) }
        Subscription subscription = component.addValueChangeListener(okListener)
        component.setValue('OK')

        then:
        counter.get() == 1

        when:
        subscription.remove()

        component.setValue('Test')

        then:
        counter.get() == 1

        when:

        //noinspection unchecked
        def testDs = new DsBuilder()
                .setId('testDs')
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource() as Datasource<User>

        testDs.setItem(new User())
        ((DatasourceImpl) testDs).valid()

        Consumer<HasValue.ValueChangeEvent> dsLoadListener = { e ->
            counter.addAndGet(1)
        }
        subscription = component.addValueChangeListener(dsLoadListener)
        component.setDatasource(testDs, 'login')

        then:
        counter.get() == 2

        when:
        subscription.remove()

        Consumer<HasValue.ValueChangeEvent> dsListener = { e ->
            counter.addAndGet(1)
        }
        component.addValueChangeListener(dsListener)
        testDs.getItem().setLogin('dsValue')

        then:
        counter.get() == 3
    }
}
