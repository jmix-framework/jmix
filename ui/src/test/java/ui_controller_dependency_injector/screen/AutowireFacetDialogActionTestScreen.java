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

package ui_controller_dependency_injector.screen;

import io.jmix.ui.component.ActionsAwareDialogFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@UiController
@UiDescriptor("autowire-facet-dialog-action-test-screen.xml")
public class AutowireFacetDialogActionTestScreen extends Screen {

    @Named("inputDialog.okAction")
    public ActionsAwareDialogFacet.DialogAction okInputDialog;
    @Named("optionDialog.okAction")
    public ActionsAwareDialogFacet.DialogAction okOptionDialog;

    @Named("testFragment.fragmentInputDialog.okAction")
    public ActionsAwareDialogFacet.DialogAction okFragmentInputDialog;
    @Named("testFragment.fragmentOptionDialog.okAction")
    public ActionsAwareDialogFacet.DialogAction okFragmentOptionDialog;

    @Autowired
    public AutowireFacetDialogActionTestFragment testFragment;
}
