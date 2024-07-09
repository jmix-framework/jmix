package ${project_rootPackage}.view.bankaccount;

import ${project_rootPackage}.entity.BankAccount;

import ${project_rootPackage}.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "bankAccounts", layout = MainView.class)
@ViewController("${normalizedPrefix_underscore}BankAccount.list")
@ViewDescriptor("bank-account-list-view.xml")
@LookupComponent("bankAccountsDataGrid")
@DialogMode(width = "64em")
public class BankAccountListView extends StandardListView<BankAccount> {
}