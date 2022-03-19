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

package bean_validation.screen;

import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.model_objects.DateValidationObject;

@UiDescriptor("date-validation-test-screen.xml")
@UiController
public class DateValidationTestScreen extends Screen {

    @Autowired
    protected Metadata metadata;

    @Install(to = "dateValidationObjectLd", target = Target.DATA_LOADER)
    protected DateValidationObject dateValidationObjectLdLoadDelegate1(LoadContext<DateValidationObject> loadContext) {
        return metadata.create(DateValidationObject.class);
    }
}