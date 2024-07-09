package ${project_rootPackage}.view.invoice;

import ${project_rootPackage}.entity.Invoice;
import ${project_rootPackage}.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "invoices", layout = MainView.class)
@ViewController("${normalizedPrefix_underscore}Invoice.list")
@ViewDescriptor("invoice-list-view.xml")
@LookupComponent("invoicesDataGrid")
@DialogMode(width = "64em")
public class InvoiceListView extends StandardListView<Invoice> {
}