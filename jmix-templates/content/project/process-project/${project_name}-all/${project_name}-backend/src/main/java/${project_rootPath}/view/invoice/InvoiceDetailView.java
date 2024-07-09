package ${project_rootPackage}.view.invoice;

import ${project_rootPackage}.service.InvoiceService;
import ${project_rootPackage}.entity.Invoice;

import ${project_rootPackage}.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

@Route(value = "invoices/:id", layout = MainView.class)
@ViewController("${normalizedPrefix_underscore}Invoice.detail")
@ViewDescriptor("invoice-detail-view.xml")
@EditedEntityContainer("invoiceDc")
public class InvoiceDetailView extends StandardDetailView<Invoice> {

    @Autowired
    private InvoiceService invoiceService;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Invoice> event) {
        Invoice entity = event.getEntity();
        entity.setPoNumber(UUID.randomUUID());

        long nextValue = invoiceService.getNextValue();
        entity.setNumber(nextValue);
        entity.setDate(LocalDate.now());
    }
}