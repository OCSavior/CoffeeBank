package com.coffeebank.portal.account.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;
    private String accountOwner;

    @Column()
    private int beanBalance;

    public Account() {}

    public Account(String accountNumber, String accountOwner, int beanBalance) {
        this.accountNumber = accountNumber;
        this.accountOwner = accountOwner;
        this.beanBalance = beanBalance;
    }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountOwner() { return accountOwner; }
    public int getBeanBalance() { return beanBalance; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountOwner(String accountOwner) { this.accountOwner = accountOwner; }
    public void setBeanBalance(int beanBalance) { this.beanBalance = beanBalance; }

}
