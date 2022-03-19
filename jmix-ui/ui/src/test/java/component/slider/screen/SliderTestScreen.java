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

package component.slider.screen;

import io.jmix.core.Metadata;
import io.jmix.ui.component.Slider;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;

import java.math.BigDecimal;

@UiController
@UiDescriptor("slider-test-screen.xml")
public class SliderTestScreen extends Screen {
    @Autowired
    protected Metadata metadata;

    @Autowired
    public InstanceContainer<Order> orderDc;
    @Autowired
    protected Slider sliderDefault;
    @Autowired
    protected Slider<Double> sliderDouble;
    @Autowired
    protected Slider<Integer> sliderInt;
    @Autowired
    protected Slider<BigDecimal> sliderDecimal;
    @Autowired
    protected Slider<Long> sliderLong;

    @SuppressWarnings("unchecked")
    @Subscribe
    protected void onInit(InitEvent event) {
        Order order = metadata.create(Order.class);
        orderDc.setItem(order);

        sliderDefault.setValue(100d);
        sliderDouble.setValue(100d);
        sliderInt.setValue(100);
        sliderDecimal.setValue(BigDecimal.valueOf(100));
        sliderLong.setValue(100L);
    }
}
