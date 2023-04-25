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

package io.jmix.data.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.jta.JtaTransactionManager;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

public class JmixJtaTransactionManager extends JtaTransactionManager implements ApplicationContextAware {
    protected ApplicationContext applicationContext;

    protected String key;

    public JmixJtaTransactionManager(String key,
                                     UserTransaction userTransaction,
                                     TransactionManager transactionManager) {
        super(userTransaction, transactionManager);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
