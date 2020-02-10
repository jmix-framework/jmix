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

package spec.haulmont.cuba.web.components.slider.screens;

import com.haulmont.cuba.core.model.common.ScheduledTask;
import io.jmix.core.Metadata;
import io.jmix.ui.components.Slider;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;
import java.math.BigDecimal;

@UiController
@UiDescriptor("slider-screen.xml")
public class SliderScreen extends Screen {
    @Inject
    protected Metadata metadata;

    @Inject
    public InstanceContainer<ScheduledTask> taskDc;
    @Inject
    protected Slider sliderDefault;
    @Inject
    protected Slider<Double> sliderDouble;
    @Inject
    protected Slider<Integer> sliderInt;
    @Inject
    protected Slider<BigDecimal> sliderDecimal;
    @Inject
    protected Slider<Long> sliderLong;

    @SuppressWarnings("unchecked")
    @Subscribe
    protected void onInit(InitEvent event) {
        ScheduledTask task = metadata.create(ScheduledTask.class);
        taskDc.setItem(task);

        sliderDefault.setValue(100d);
        sliderDouble.setValue(100d);
        sliderInt.setValue(100);
        sliderDecimal.setValue(BigDecimal.valueOf(100));
        sliderLong.setValue(100L);
    }
}
