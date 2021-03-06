/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.app.events.AttributeChanges;
import com.haulmont.cuba.core.app.events.EntityChangedEvent;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.model.sales.OrderLine;
import com.haulmont.cuba.core.model.sales.Product;
import com.haulmont.cuba.core.testsupport.CoreTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class DataManagerTransactionalUsageTest {

    @Component("test_SaleProcessor")
    public static class SaleProcessor {

        @Autowired
        private TransactionalDataManager txDataManager;

        @Transactional
        public Id<OrderLine, UUID> sell(String productName, Integer quantity) {
            Product product = txDataManager.load(Product.class)
                    .query("select p from test$Product p where p.name = :name")
                    .parameter("name", productName)
                    .optional()
                    .orElseGet(() -> {
                        Product p = txDataManager.create(Product.class);
                        p.setName(productName);
                        p.setQuantity(100); // initial quantity of a new product
                        return txDataManager.save(p);
                    });

            OrderLine orderLine = txDataManager.create(OrderLine.class);
            orderLine.setProduct(product);
            orderLine.setQuantity(quantity);
            txDataManager.save(orderLine);

            return Id.of(orderLine);
        }

        public Id<OrderLine, UUID> sellSecureWithProgrammaticTx(String productName, Integer quantity) {
            TransactionalDataManager secureDm = txDataManager.secure();
            try (Transaction tx = secureDm.transactions().create()) {
                Product product = secureDm.load(Product.class)
                        .query("select p from test$Product p where p.name = :name")
                        .parameter("name", productName)
                        .optional()
                        .orElseGet(() -> {
                            Product p = txDataManager.create(Product.class);
                            p.setName(productName);
                            p.setQuantity(100); // initial quantity of a new product
                            return secureDm.save(p);
                        });

                OrderLine orderLine = txDataManager.create(OrderLine.class);
                orderLine.setProduct(product);
                orderLine.setQuantity(quantity);
                secureDm.save(orderLine);

                tx.commit();

                return Id.of(orderLine);
            }
        }
    }

    @Component("test_OrderLineChangedListener")
    public static class OrderLineChangedListener {

        @Autowired
        private TransactionalDataManager txDataManager;

        @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT) // same as simple @EventListener
        protected void orderLineChanged(EntityChangedEvent<OrderLine, UUID> event) {
            AttributeChanges changes = event.getChanges();

            if (event.getType() == EntityChangedEvent.Type.DELETED) {
                Id<Product, UUID> productId = changes.getOldReferenceId("product");
                if (productId != null) {
                    Integer oldQuantity = changes.getOldValue("quantity");

                    Product product = txDataManager.load(productId).one();
                    product.setQuantity(product.getQuantity() + oldQuantity);
                    txDataManager.save(product);
                }
            } else {
                OrderLine orderLine = txDataManager.load(event.getEntityId()).view("with-product").one();
                Product product = orderLine.getProduct();
                if (product != null) {
                    if (event.getType() == EntityChangedEvent.Type.UPDATED) {
                        if (changes.isChanged("product")) {
                            Id<Product, UUID> oldProductId = changes.getOldReferenceId("product");
                            if (oldProductId != null) {
                                Product oldProduct = txDataManager.load(oldProductId).one();
                                oldProduct.setQuantity(oldProduct.getQuantity() + orderLine.getQuantity());
                                txDataManager.save(oldProduct);
                            }

                            product.setQuantity(product.getQuantity() - orderLine.getQuantity());
                            txDataManager.save(product);

                        } else if (changes.isChanged("quantity")) {
                            product.setQuantity(product.getQuantity() - orderLine.getQuantity());
                            txDataManager.save(product);
                        }

                    } else if (event.getType() == EntityChangedEvent.Type.CREATED) {
                        product.setQuantity(product.getQuantity() - orderLine.getQuantity());
                        txDataManager.save(product);
                    }
                }
            }
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        protected void notifyAboutChanges(EntityChangedEvent<OrderLine, UUID> event) {
            System.out.println("Changed OrderLine: " + event);
        }
    }

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Persistence persistence;

    @AfterEach
    protected void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("delete from TEST_ORDER_LINE");
        jdbcTemplate.update("delete from TEST_PRODUCT");
    }

    @Test
    public void test() {
        SaleProcessor processor = AppBeans.get("test_SaleProcessor");
        Id<OrderLine, UUID> orderLineId = processor.sell("abc", 10);

        Product product1 = dataManager.load(Product.class)
                .query("select p from test$Product p where p.name = :name")
                .parameter("name", "abc")
                .one();
        assertEquals(90, (int) product1.getQuantity());

        // change Product in OrderLIne

        Product product2 = dataManager.create(Product.class);
        product2.setName("def");
        product2.setQuantity(100);
        dataManager.commit(product2);

        OrderLine orderLine = dataManager.load(orderLineId).view("with-product").one();
        orderLine.setProduct(product2);
        dataManager.commit(orderLine);

        Product changedProduct1 = dataManager.load(Id.of(product1)).one();
        assertEquals(100, (int) changedProduct1.getQuantity());

        Product changedProduct2 = dataManager.load(Id.of(product2)).one();
        assertEquals(90, (int) changedProduct2.getQuantity());

        // remove OrderLine

        OrderLine orderLineToRemove = dataManager.load(orderLineId).view("with-product").one();
        dataManager.remove(orderLineToRemove);

        Product product21 = dataManager.load(Id.of(product2)).one();
        assertEquals(100, (int) product21.getQuantity());
    }

    @Test
    public void testSecureWithProgrammaticTx() {
        SaleProcessor processor = AppBeans.get("test_SaleProcessor");
        Id<OrderLine, UUID> orderLineId = processor.sellSecureWithProgrammaticTx("abc", 10);

        Product product1 = dataManager.load(Product.class)
                .query("select p from test$Product p where p.name = :name")
                .parameter("name", "abc")
                .one();
        assertEquals(90, (int) product1.getQuantity());

        Product product2 = dataManager.create(Product.class);
        product2.setName("def");
        product2.setQuantity(100);
        dataManager.commit(product2);

        OrderLine orderLine = dataManager.load(orderLineId).view("with-product").one();
        orderLine.setProduct(product2);
        dataManager.commit(orderLine);

        Product changedProduct1 = dataManager.load(Id.of(product1)).one();
        assertEquals(100, (int) changedProduct1.getQuantity());

        Product changedProduct2 = dataManager.load(Id.of(product2)).one();
        assertEquals(90, (int) changedProduct2.getQuantity());
    }
}
