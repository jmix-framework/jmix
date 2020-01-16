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

package io.jmix.core.cluster;

import io.jmix.core.cluster.ClusterListener;
import io.jmix.core.cluster.ClusterManager;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component(ClusterManager.NAME)
public class ClusterManagerImpl implements ClusterManager {

    @Override
    public void send(Serializable message) {

    }

    @Override
    public void sendSync(Serializable message) {

    }

    @Override
    public boolean getSyncSendingForCurrentThread() {
        return false;
    }

    @Override
    public void setSyncSendingForCurrentThread(boolean sync) {

    }

    @Override
    public void addListener(Class messageClass, ClusterListener listener) {

    }

    @Override
    public void removeListener(Class messageClass, ClusterListener listener) {

    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public String getCurrentView() {
        return "";
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public int getActiveThreadsCount() {
        return 0;
    }

    @Override
    public int getMessagesCount() {
        return 0;
    }

    @Override
    public String printSharedStateStat() {
        return "";
    }

    @Override
    public String printMessagesStat() {
        return "";
    }

    @Override
    public long getSentMessages(String className) {
        return 0;
    }

    @Override
    public long getSentBytes(String className) {
        return 0;
    }

    @Override
    public long getReceivedMessages(String className) {
        return 0;
    }

    @Override
    public long getReceivedBytes(String className) {
        return 0;
    }
}
