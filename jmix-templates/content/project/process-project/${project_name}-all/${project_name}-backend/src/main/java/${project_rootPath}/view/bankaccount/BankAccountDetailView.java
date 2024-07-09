package ${project_rootPackage}.view.bankaccount;

import ${project_rootPackage}.entity.BankAccount;

import ${project_rootPackage}.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "bankAccounts/:id", layout = MainView.class)
@ViewController("${normalizedPrefix_underscore}BankAccount.detail")
@ViewDescriptor("bank-account-detail-view.xml")
@EditedEntityContainer("bankAccountDc")
public class BankAccountDetailView extends StandardDetailView<BankAccount> {
}