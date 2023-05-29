/*
 * Copyright 2019 Haulmont.
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

/**
 * Defines parameters of a new transaction.
*/
public class TransactionParams {

    private int timeout;
    private boolean readOnly;

    /**
     * @param timeout Database query timeout in seconds. 0 means undefined.
     * @return this instance for chaining
     */
    public TransactionParams setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @return  Database query timeout in seconds. 0 means undefined.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Indicates that the transaction is read-only and performance optimization can be applied. Default is false.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Indicates that the transaction will be read-only and performance optimization can be applied. Default is false.
     * @return this instance for chaining
     */
    public TransactionParams setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public String toString() {
        return "TransactionParams{" +
                "timeout=" + timeout +
                ", readOnly=" + readOnly +
                '}';
    }
}
