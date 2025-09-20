/*
 * Copyright 2025 Haulmont.
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

package common;

import io.jmix.core.DataManager;
import io.jmix.samples.restds.common.entity.Product;
import io.jmix.samples.restds.common.entity.ProductGroup;
import io.jmix.samples.restds.common.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestSupport;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonServiceInterfaceTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    ProductService productService;

    @Test
    void test() {
        ProductGroup productGroup = dataManager.load(ProductGroup.class).id(TestSupport.UUID_1).one();

        List<Product> products = productService.getProducts(productGroup);
        assertThat(products).isNotEmpty();
    }
}
