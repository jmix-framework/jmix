/*
 * Copyright 2024 Haulmont.
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

package component.generic_component.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

import java.util.List;
import java.util.Set;

@Tag("test-component")
public class TestComponent extends Component {

    List<String> stringsList;
    Set<String> stringsSet;
    String[] stringsArray;
    String[] strings;

    public List<String> getStringsList() {
        return stringsList;
    }

    public void setStringsList(List<String> strings) {
        this.stringsList = strings;
    }

    public Set<String> getStringsSet() {
        return stringsSet;
    }

    public void setStringsSet(Set<String> stringsSet) {
        this.stringsSet = stringsSet;
    }

    public String[] getStringsArray() {
        return stringsArray;
    }

    public void setStringsArray(String[] stringsArray) {
        this.stringsArray = stringsArray;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String... strings) {
        this.strings = strings;
    }
}
