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

package io.jmix.samples.restservice.app;

import io.jmix.core.DataManager;
import io.jmix.rest.annotation.RestMethod;
import io.jmix.rest.annotation.RestService;
import io.jmix.samples.restds.common.entity.Product;
import io.jmix.samples.restds.common.entity.ProductGroup;
import io.jmix.samples.restds.common.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestService("app_Products")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private DataManager dataManager;

    @RestMethod
    @Override
    public List<Product> getProducts(ProductGroup productGroup) {
        return dataManager.load(Product.class)
                .query("select p from common_Product p where p.group = :group")
                .parameter("group", productGroup)
                .list();
    }
}
