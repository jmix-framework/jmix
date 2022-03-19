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

package component.date_field.screen;

import io.jmix.ui.component.DateField;
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
@UiDescriptor("date-field-datatype-test-screen.xml")
public class DateFieldDatatypeTestScreen extends Screen {
    @Autowired
    protected DateField<Date> dateField;
    @Autowired
    protected DateField<Date> dateTimeField;
    @Autowired
    protected DateField<LocalDate> localDateField;
    @Autowired
    protected DateField<LocalDateTime> localDateTimeField;
    @Autowired
    protected DateField<OffsetDateTime> offsetDateTimeField;

    @Subscribe
    protected void onInit(InitEvent event) {
        Date now = new Date();
        dateField.setValue(new java.sql.Date(now.getTime()));
        dateTimeField.setValue(now);
        localDateField.setValue(LocalDate.now());
        localDateTimeField.setValue(LocalDateTime.now());
        offsetDateTimeField.setValue(OffsetDateTime.now());
    }
}
