package ${project_rootPackage}.form;

import ${project_rootPackage}.entity.BankAccount;
import ${project_rootPackage}.entity.Invoice;
import ${project_rootPackage}.entity.PaymentStatus;
import io.jmee.client.annotation.ProcessFormConfiguration;
import io.jmee.client.annotation.ProcessFormFieldOptions;
import io.jmee.client.annotation.ProcessFormLoadData;
import io.jmee.client.annotation.ProcessFormSaveData;
import io.jmee.client.entity.ProcessFormEntity;
import io.jmee.client.service.datamodel.FormSelectOption;
import io.jmee.client.sys.TaskContext;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@ProcessFormConfiguration(formId = "approve-invoice-form")
public class ApproveInvoiceFormConfiguration {

    private final DataManager dataManager;
    private final Messages messages;

    public ApproveInvoiceFormConfiguration(DataManager dataManager, Messages messages) {
        this.dataManager = dataManager;
        this.messages = messages;
    }

    @ProcessFormLoadData
    public Function<TaskContext, ProcessFormEntity> load() {
        return (context) -> {
            UUID entityId = UUID.fromString(context.getTaskVariables().get("entityId").toString());
            return dataManager.load(Invoice.class)
                    .query("select i from ${normalizedPrefix_underscore}Invoice i where i.id = :invoiceId")
                    .parameter("invoiceId", entityId)
                    .optional()
                    .orElse(null);
        };
    }

    @ProcessFormFieldOptions(fieldId = "paymentStatuses")
    public Function<TaskContext, List<FormSelectOption>> paymentStatuses() {
        return (context) -> {
            List<FormSelectOption> paymentStatuses = new ArrayList<>();
            paymentStatuses.add(FormSelectOption.create(messages.getMessage(PaymentStatus.APPROVED), PaymentStatus.APPROVED.getId()));
            paymentStatuses.add(FormSelectOption.create(messages.getMessage(PaymentStatus.PENDING), PaymentStatus.PENDING.getId()));
            paymentStatuses.add(FormSelectOption.create(messages.getMessage(PaymentStatus.DECLINED), PaymentStatus.DECLINED.getId()));

            return paymentStatuses;
        };
    }

    @ProcessFormSaveData
    public Consumer<TaskContext> save() {
        return (context) -> {
            UUID entityId = UUID.fromString(context.getTaskVariables().get("entityId").toString());
            Optional<Invoice> invoiceOpt = dataManager.load(Invoice.class)
                    .query("select i from ${normalizedPrefix_underscore}Invoice i where i.id = :invoiceId")
                    .parameter("invoiceId", entityId)
                    .optional();

            if (invoiceOpt.isPresent()) {
                String status = (String) context.getFormVariables().get("status_select");
                Invoice invoice = invoiceOpt.get();
                invoice.setPaymentStatus(PaymentStatus.valueOf(status));
                dataManager.save(invoice);
            }
        };
    }

}
