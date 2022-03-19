/*
 * Copyright 2021 Haulmont.
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

package spec.haulmont.cuba.web.ui_controller_depenency_injector.screen;

import com.haulmont.cuba.core.app.SchedulingConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import org.slf4j.Logger;

import javax.inject.Inject;

public class AutowireLegacyParamsScreen extends AbstractWindow {

    @Inject
    public SchedulingConfig schedulingConfig;

    @Inject
    public CollectionDatasourceImpl citiesDs;

    @Inject
    public DsContext dsContext;

    @Inject
    public DataSupplier dataSupplier;

    @Inject
    public WindowManager windowManager;

    @Inject
    public Logger logger;
}
