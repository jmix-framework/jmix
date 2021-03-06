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

package spec.haulmont.cuba.web.components.datefield.screens;

import com.haulmont.cuba.gui.components.DateField;
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
@UiDescriptor("datefield-datatype-screen.xml")
public class DateFieldDatatypeScreen extends Screen {
    @Autowired
    private DateField<java.sql.Date> dateField;
    @Autowired
    private DateField<Date> dateTimeField;
    @Autowired
    private DateField<LocalDate> localDateField;
    @Autowired
    private DateField<LocalDateTime> localDateTimeField;
    @Autowired
    private DateField<OffsetDateTime> offsetDateTimeField;

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
