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

package facet.screen.screen;

import io.jmix.ui.component.Label;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;

@UiController("test_FacetTestScreen")
public class FacetTestScreen extends Screen {

    public boolean boolProp;
    public int intProp;
    public double doubleProp;

    public Label labelProp;
    public InstanceContainer dcProp;

    public void setBoolProp(boolean boolProp) {
        this.boolProp = boolProp;
    }

    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    public void setLabelProp(Label labelProp) {
        this.labelProp = labelProp;
    }

    public void setDcProp(InstanceContainer dcProp) {
        this.dcProp = dcProp;
    }
}
