/*
 * Copyright 2026 Haulmont.
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

package cluster_events;

import io.jmix.core.cluster.ClusterApplicationEvent;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.ClusterApplicationEventPublisher;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterApplicationEventPublisherTest {

    @Test
    void publish_withLocalChannel_reachesOnlyPublishingContext() {
        try (ConfigurableApplicationContext first = startContext(LocalChannelConfiguration.class);
             ConfigurableApplicationContext second = startContext(LocalChannelConfiguration.class)) {

            first.getBean(ClusterApplicationEventPublisher.class).publish(new TestClusterEvent(this));

            assertThat(first.getBean(TestClusterEventListener.class).events).hasSize(1);
            assertThat(second.getBean(TestClusterEventListener.class).events).isEmpty();
        }
    }

    @Test
    void contextClose_unsubscribesPublisherFromChannel() {
        ConfigurableApplicationContext context = startContext(TestChannelConfiguration.class);
        TestSubscribableChannel channel = (TestSubscribableChannel) context
                .getBean(ClusterApplicationEventChannelSupplier.class).get();
        assertThat(channel.handlers).hasSize(1);

        context.close();

        assertThat(channel.handlers).isEmpty();
    }

    ConfigurableApplicationContext startContext(Class<?> configurationClass) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configurationClass);
        context.publishEvent(new ApplicationStartedEvent(
                new SpringApplication(configurationClass), new String[0], context, Duration.ZERO));
        return context;
    }

    @Configuration(proxyBeanMethods = false)
    static class LocalChannelConfiguration {

        @Bean
        ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
            return new LocalApplicationEventChannelSupplier();
        }

        @Bean
        ClusterApplicationEventPublisher clusterApplicationEventPublisher(
                ApplicationEventPublisher applicationEventPublisher,
                ClusterApplicationEventChannelSupplier channelSupplier) {
            return new ClusterApplicationEventPublisher(applicationEventPublisher, channelSupplier);
        }

        @Bean
        TestClusterEventListener testClusterEventListener() {
            return new TestClusterEventListener();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestChannelConfiguration {

        @Bean
        ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
            TestSubscribableChannel channel = new TestSubscribableChannel();
            return () -> channel;
        }

        @Bean
        ClusterApplicationEventPublisher clusterApplicationEventPublisher(
                ApplicationEventPublisher applicationEventPublisher,
                ClusterApplicationEventChannelSupplier channelSupplier) {
            return new ClusterApplicationEventPublisher(applicationEventPublisher, channelSupplier);
        }
    }

    static class TestClusterEvent extends ClusterApplicationEvent {

        TestClusterEvent(Object source) {
            super(source);
        }
    }

    static class TestClusterEventListener {

        final List<TestClusterEvent> events = new ArrayList<>();

        @EventListener
        void onTestClusterEvent(TestClusterEvent event) {
            events.add(event);
        }
    }

    @NullMarked
    static class TestSubscribableChannel implements SubscribableChannel {

        final List<MessageHandler> handlers = new ArrayList<>();

        @Override
        public boolean subscribe(MessageHandler handler) {
            return handlers.add(handler);
        }

        @Override
        public boolean unsubscribe(MessageHandler handler) {
            return handlers.remove(handler);
        }

        @Override
        public boolean send(Message<?> message, long timeout) {
            handlers.forEach(handler -> handler.handleMessage(message));
            return true;
        }
    }
}
