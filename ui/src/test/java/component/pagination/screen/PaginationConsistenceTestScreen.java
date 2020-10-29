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

package component.pagination.screen;

import io.jmix.ui.component.Pagination;
import io.jmix.ui.component.SimplePagination;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.widget.JmixPagination;
import io.jmix.ui.widget.JmixSimplePagination;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("pagination-consistence-test-screen.xml")
public class PaginationConsistenceTestScreen extends Screen {

    @Autowired
    public Pagination pagination;

    @Autowired
    public SimplePagination simplePagination;

    public JmixPagination getJmixPagination() {
        return pagination.unwrap(JmixPagination.class);
    }

    public JmixSimplePagination getJmixSimplePagination() {
        return simplePagination.unwrap(JmixSimplePagination.class);
    }
}
