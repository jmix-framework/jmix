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

package io.jmix.data.impl.jta;

import org.eclipse.persistence.transaction.JTATransactionController;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.transaction.TransactionManager;

public class JmixJtaTransactionController extends JTATransactionController implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setApplicationContextInternal(applicationContext);
    }

    public JmixJtaTransactionController() {
    }

    public JmixJtaTransactionController(TransactionManager transactionManager) {
        super(transactionManager);
    }

    protected TransactionManager acquireTransactionManager() throws Exception {
        if (transactionManager == null && ctx != null) {
            transactionManager = ctx.getBean(TransactionManager.class);
        }
        return transactionManager;
    }

    protected static void setApplicationContextInternal(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }
}
