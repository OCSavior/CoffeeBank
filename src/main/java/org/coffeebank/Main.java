package org.coffeebank;

import org.coffeebank.models.Account;
import org.coffeebank.models.AccountSummary;
import org.coffeebank.models.AccountType;
import org.coffeebank.models.Customer;
import org.coffeebank.service.BankingService;

import java.math.BigDecimal;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // 1. Initialize the Service
        var bank = new BankingService();
        Scanner scanner = new Scanner(System.in);

        // 2. Create a Customer
        var demoCustomer = new Customer("John", "Doe", "john@coffee.com", "555-0123", "123 Espresso Lane");

        // 3. Create Accounts
        var checking = new Account(AccountType.FLAT_WHITE);
        var savings = new Account(AccountType.COLD_BREW);

        // 4. Register them in the bank service
        bank.registerCustomer(demoCustomer);
        bank.registerAccount(checking);
        bank.registerAccount(savings);
        demoCustomer.addAccount(checking);
        demoCustomer.addAccount(savings);

        // 5. TEST THE LOGIC
        bank.deposit(checking.getId(), BigDecimal.valueOf(100.00));
        bank.transfer(checking.getId(), savings.getId(), BigDecimal.valueOf(40));

        Customer currentCustomer = null;

        System.out.println("Welcome to Coffee Bank v1.0");

        while (true) {
            if (currentCustomer == null) {
                // AUTHENTICATION PHASE
                System.out.println("\nPlease enter your email to log in (or type 'exit' to quit):");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Thank you for choosing CoffeeBank. Goodbye!");
                    break;
                }

                Customer customer = bank.getCustomerByEmail(input);
                if (customer != null) {
                    currentCustomer = customer;
                    System.out.println("\n🔓 Login Successful! Welcome back, " + currentCustomer.getFirstName() + ".");
                } else {
                    System.out.println("❌ Account not found. Please try again.");
                }
            }
            else {
                // --- HOME MENU PHASE (Once Logged In) ---
                System.out.println("\n======= COFFEEBANK MAIN MENU =======");
                System.out.println("1. View Balances & History");
                System.out.println("2. Deposit Coffee Beans");
                System.out.println("3. Transfer Coffee");
                System.out.println("4. Logout");
                System.out.print("Please select an option (1-4): ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        // View Balances - [{Account: Value [History]]
                        for (AccountSummary summary : bank.getAccountDetails(currentCustomer)) {
                            System.out.println("\n[" + summary.accountType() + "] Balance: $" + summary.balance());
                            System.out.println("  Transaction History:");
                            if (summary.history().isEmpty()) {
                                System.out.println("    No transactions yet.");
                            } else {
                                for (var tx : summary.history()) {
                                    System.out.println("    - " + tx.direction() + " $" + tx.amount() + " on " + tx.timestamp().toLocalDate());
                                }
                            }
                        }
                        break;
                    case "2":
                        bank.initiateDeposit(currentCustomer, scanner);
                        break;
                    case "3":
                        bank.initiateTransfer(currentCustomer, scanner);
                        break;
                    case "4":
                        System.out.println("🔒 Logged out successfully.");
                        currentCustomer = null; // Clears session state
                        break;
                    default:
                        System.out.println("⚠️ Invalid selection. Choose 1, 2, 3, or 4.");
                }
            }
        }
        scanner.close();
    }
}