/*
 * Copyright 2022 Haulmont.
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

package formatter.screen;

import com.vaadin.flow.router.Route;
import io.jmix.core.TimeSource;
import io.jmix.flowui.component.formatter.NumberFormatter;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Route(value = "formatter-view")
@ViewController("FormatterView")
@ViewDescriptor("formatter-view.xml")
public class FormatterView extends StandardView {

    @ViewComponent
    public JmixValuePicker<Date> dateFormatterField;

    @ViewComponent
    public JmixValuePicker<Date> dateTimeFormatterField;

    @ViewComponent
    public JmixValuePicker<Long> numberFormatterField;

    @ViewComponent
    public JmixValuePicker<Long> defaultNumberFormatterField;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected ObjectProvider<NumberFormatter> numberFormatterObjectProvider;

    @Subscribe
    protected void onInit(InitEvent event) {
        defaultNumberFormatterField.setFormatter(numberFormatterObjectProvider.getObject());

        dateFormatterField.setValue(timeSource.currentTimestamp());
        dateTimeFormatterField.setValue(timeSource.currentTimestamp());
        numberFormatterField.setValue(timeSource.currentTimeMillis());
        defaultNumberFormatterField.setValue(timeSource.currentTimeMillis());
    }
}
