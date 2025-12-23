package test_support.listeners;

import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import test_support.entity.events.on_delete_stack_overflow.History;
import test_support.entity.events.on_delete_stack_overflow.Payment;
import test_support.entity.events.on_delete_stack_overflow.Product;

import java.util.UUID;


@Component
public class OnDeleteStackOverflowListener {
    @Autowired
    private DataManager dataManager;

    @EventListener
    public void onPaymentChangedBeforeCommit(final EntityChangedEvent<Payment> event) {
        History history = dataManager.create(History.class);
        history.setObjId((UUID) event.getEntityId().getValue());
        history.setEvent(event.toString());
        history.setEventType(event.getType().toString());
        dataManager.save(history);
    }

    @EventListener
    public void onProductChangedBeforeCommit(final EntityChangedEvent<Product> event) {
        History history = dataManager.create(History.class);
        history.setObjId((UUID) event.getEntityId().getValue());
        history.setEvent(event.toString());
        history.setEventType(event.getType().toString());
        dataManager.save(history);
    }
}