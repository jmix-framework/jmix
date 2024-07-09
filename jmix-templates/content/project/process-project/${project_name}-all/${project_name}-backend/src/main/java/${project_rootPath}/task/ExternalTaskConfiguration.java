package ${project_rootPackage}.task;

import ${project_rootPackage}.entity.Invoice;
import ${project_rootPackage}.entity.PaymentStatus;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import io.jmix.core.security.SystemAuthenticator;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Configuration
public class ExternalTaskConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ExternalTaskConfiguration.class);

    public static final String ENTITY_ID_VARIABLE = "entityId";

    private final SystemAuthenticator systemAuthenticator;
    private final DataManager dataManager;

    public ExternalTaskConfiguration(SystemAuthenticator systemAuthenticator, DataManager dataManager) {
        this.systemAuthenticator = systemAuthenticator;
        this.dataManager = dataManager;
    }

    @Bean
    @Authenticated
    @ExternalTaskSubscription(topicName = "update-status-task", processDefinitionKey = "create-invoice-process")
    public ExternalTaskHandler updateStatusTask() {
        return (externalTask, externalTaskService) -> {
            systemAuthenticator.withSystem(() -> {
                String entityId = externalTask.getVariable(ENTITY_ID_VARIABLE);
                Invoice invoice = getInvoiceById(entityId);

                String paymentStatus = invoice.getPaymentStatus().getId();
                VariableMap variableMap = Variables.putValue("status", paymentStatus);

                externalTaskService.complete(externalTask, variableMap);

                log.info("ExternalTask 'update-status-task' with id -'{}' completed successfully", externalTask.getId());

                return "Done";
            });
        };
    }

    private Invoice getInvoiceById(String entityId) {
        return dataManager.load(Invoice.class)
                .query("select e from ${normalizedPrefix_underscore}Invoice e where e.id = :id")
                .parameter("id", UUID.fromString(entityId))
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
    }

    @Bean
    @Authenticated
    @ExternalTaskSubscription(topicName = "register-invoice-task", processDefinitionKey = "create-invoice-process")
    public ExternalTaskHandler registerInvoice() {
        return (externalTask, externalTaskService) -> {
            systemAuthenticator.withSystem(() -> {
                Invoice saved = createInvoice();

                String id = saved.getId().toString();
                VariableMap variableMap = Variables.putValue(ENTITY_ID_VARIABLE, id);

                externalTaskService.complete(externalTask, variableMap);

                log.info("ExternalTask 'register-invoice-task' with id -'{}' completed successfully", externalTask.getId());

                return "Done";
            });
        };
    }

    private Invoice createInvoice() {
        Invoice invoice = dataManager.create(Invoice.class);
        invoice.setNumber(1L);
        invoice.setDate(LocalDate.now());
        invoice.setPoNumber(UUID.randomUUID());
        invoice.setVendor("Vendor 1");
        invoice.setAmount(BigDecimal.valueOf(1000));
        invoice.setPaymentStatus(PaymentStatus.PENDING);

        return dataManager.save(invoice);
    }
}
