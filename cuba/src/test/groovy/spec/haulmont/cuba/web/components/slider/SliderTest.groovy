/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.components.slider

import com.haulmont.cuba.core.model.common.ScheduledTask
import io.jmix.core.metamodel.annotations.JavaClass
import io.jmix.core.metamodel.datatypes.Datatype
import io.jmix.core.metamodel.datatypes.impl.BigDecimalDatatype
import io.jmix.core.metamodel.datatypes.impl.DoubleDatatype
import io.jmix.core.metamodel.datatypes.impl.IntegerDatatype
import io.jmix.core.metamodel.datatypes.impl.LongDatatype
import io.jmix.ui.components.Slider
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.slider.screens.SliderScreen
import spock.lang.Ignore

@Ignore
@SuppressWarnings("GroovyAssignabilityCheck")
class SliderTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.slider.screens', 'com.haulmont.cuba.web.app.main'])
    }

    def "Datatype is applied from the screen descriptor"(String id, Class<Datatype> datatypeClass) {
        showMainScreen()

        def sliderScreen = screens.create(SliderScreen)
        sliderScreen.show()

        when:

        def slider = (Slider) sliderScreen.getWindow().getComponentNN(id)

        then:

        slider.getDatatype().getClass() == datatypeClass
        slider.getValue().getClass() == datatypeClass.getAnnotation(JavaClass).value()

        where:

        id              | datatypeClass
        "sliderDefault" | DoubleDatatype
        "sliderDouble"  | DoubleDatatype
        "sliderInt"     | IntegerDatatype
        "sliderDecimal" | BigDecimalDatatype
        "sliderLong"    | LongDatatype
    }

    def "Value is propagated to ValueSource from Slider"() {
        showMainScreen()

        def sliderScreen = screens.create(SliderScreen)
        sliderScreen.show()

        def item = (ScheduledTask) sliderScreen.taskDc.getItem()
        def slider = (Slider) sliderScreen.getWindow().getComponentNN("sliderWithContainer")

        when: 'Value is set to Slider'
        slider.setValue(10)

        then: 'ValueSource is updated'
        item.getPeriod() == slider.getValue()
    }

    def "Value is propagated to Slider from ValueSource"() {
        showMainScreen()

        def sliderScreen = screens.create(SliderScreen)
        sliderScreen.show()

        def item = (ScheduledTask) sliderScreen.taskDc.getItem()
        def slider = (Slider) sliderScreen.getWindow().getComponentNN("sliderWithContainer")

        when: 'Value is set to ValueSource'
        item.setPeriod(10)

        then: 'Slider is updated'
        item.getPeriod() == slider.getValue()
    }
}
