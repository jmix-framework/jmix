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

package component.date_picker.screen;

import io.jmix.ui.component.DatePicker;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@UiController
@UiDescriptor("date-picker-datatype-test-screen.xml")
public class DatePickerDatatypeTestScreen extends Screen {
    @Autowired
    private DatePicker<Date> datePicker;
    @Autowired
    private DatePicker<Date> dateTimePicker;
    @Autowired
    private DatePicker<LocalDate> localDatePicker;
    @Autowired
    private DatePicker<LocalDateTime> localDateTimePicker;
    @Autowired
    private DatePicker<OffsetDateTime> offsetDateTimePicker;

    @Subscribe
    protected void onInit(InitEvent event) {
        Date now = new Date();
        datePicker.setValue(new java.sql.Date(now.getTime()));
        dateTimePicker.setValue(now);
        localDatePicker.setValue(LocalDate.now());
        localDateTimePicker.setValue(LocalDateTime.now());
        offsetDateTimePicker.setValue(OffsetDateTime.now());
    }
}
