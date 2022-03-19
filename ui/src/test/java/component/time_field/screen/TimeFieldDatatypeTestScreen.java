/*
 * Copyright 2020 Haulmont.
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

package component.time_field.screen;

import io.jmix.ui.component.TimeField;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Date;

@UiController
@UiDescriptor("time-field-datatype-test-screen.xml")
public class TimeFieldDatatypeTestScreen extends Screen {
    @Autowired
    protected TimeField<Time> timeField;
    @Autowired
    protected TimeField<LocalTime> localTimeField;
    @Autowired
    protected TimeField<OffsetTime> offsetTimeField;

    @Subscribe
    protected void onInit(InitEvent event) {
        Date now = new Date();
        timeField.setValue(new Time(now.getTime()));
        localTimeField.setValue(LocalTime.now());
        offsetTimeField.setValue(OffsetTime.now());
    }
}
