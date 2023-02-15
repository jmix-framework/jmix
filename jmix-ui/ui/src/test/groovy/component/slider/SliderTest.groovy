/*
 * Copyright (c) 2020 Haulmont.
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

package component.slider

import component.slider.screen.SliderTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.annotation.DatatypeDef
import io.jmix.core.metamodel.datatype.Datatype
import io.jmix.core.metamodel.datatype.impl.BigDecimalDatatype
import io.jmix.core.metamodel.datatype.impl.DoubleDatatype
import io.jmix.core.metamodel.datatype.impl.IntegerDatatype
import io.jmix.core.metamodel.datatype.impl.LongDatatype
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Slider
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class SliderTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.slider"])
    }

    def "Datatype is applied from the screen descriptor"(String id, Class<Datatype> datatypeClass) {
        showTestMainScreen()

        def sliderScreen = screens.create(SliderTestScreen)
        sliderScreen.show()

        when:

        def slider = (Slider) sliderScreen.getWindow().getComponentNN(id)

        then:

        slider.getDatatype().getClass() == datatypeClass
        slider.getValue().getClass() == datatypeClass.getAnnotation(DatatypeDef).javaClass()

        where:

        id              | datatypeClass
        "sliderDefault" | DoubleDatatype
        "sliderDouble"  | DoubleDatatype
        "sliderInt"     | IntegerDatatype
        "sliderDecimal" | BigDecimalDatatype
        "sliderLong"    | LongDatatype
    }

    def "Value is propagated to ValueSource from Slider"() {
        showTestMainScreen()

        def sliderScreen = screens.create(SliderTestScreen)
        sliderScreen.show()

        def item = (Order) sliderScreen.orderDc.getItem()
        def slider = (Slider) sliderScreen.getWindow().getComponentNN("sliderWithContainer")

        when: 'Value is set to Slider'
        slider.setValue(new BigDecimal(10))

        then: 'ValueSource is updated'
        item.getAmount() == slider.getValue()
    }

    def "Value is propagated to Slider from ValueSource"() {
        showTestMainScreen()

        def sliderScreen = screens.create(SliderTestScreen)
        sliderScreen.show()

        def item = (Order) sliderScreen.orderDc.getItem()
        def slider = (Slider) sliderScreen.getWindow().getComponentNN("sliderWithContainer")

        when: 'Value is set to ValueSource'
        item.setAmount(new BigDecimal(10))

        then: 'Slider is updated'
        item.getAmount() == slider.getValue()
    }
}
