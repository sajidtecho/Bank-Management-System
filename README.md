# Bank Management System 🏦

A professional, enterprise-grade Core Java terminal-based application implementing a **Layered Architecture**. This system demonstrates the rigorous application of Object-Oriented Programming (OOP) principles, SOLID design standards, robust file-based persistence, custom exception handling, and streams API. It is tailored for campus placement portfolios (TCS, Cognizant, Infosys, Wipro, Accenture, Deloitte, etc.).

---

## 📖 Project Overview

The **Bank Management System** provides secure, terminal-based banking solutions. The application handles accounts creation, secure authentication, financial transactions (deposits, withdrawals, and bank transfers), transaction logging, data sorting, and robust file operations without relying on external databases.

### 🌟 Key Highlights
- **100% Core Java:** Built using standard library constructs with Java 17 features (compatible down to Java 8).
- **SQLite SQL Database Persistence:** Uses JDBC to communicate with an embedded, zero-configuration local database file (`bank.db`).
- **Layered Architecture:** Enforces separation of concerns by segregating Presentation (Menu), Business Logic (Service), Persistence (Repository), and Domain Models.
- **Visual Terminal Tables:** Displays sorted queries, account summaries, and transaction logs using custom terminal table formatters.

---

## 🛠️ Architecture & System Design

The project strictly follows a **Layered Architecture** pattern to guarantee decoupling, high testability, and clear separation of concerns.

### 🔄 Relationship & Dependency Diagram

The diagram below shows how the components interact across different architectural boundaries:

```mermaid
graph TD
    %% Define styles
    classDef default fill:#f9f9f9,stroke:#333,stroke-width:2px;
    classDef app fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef ui fill:#e8f5e9,stroke:#388e3c,stroke-width:2px;
    classDef service fill:#fff8e1,stroke:#ffa000,stroke-width:2px;
    classDef repo fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px;
    classDef model fill:#ffebee,stroke:#d32f2f,stroke-width:2px;
    classDef util fill:#eceff1,stroke:#455a64,stroke-width:2px;

    %% Define packages/layers
    subgraph app_bootstrapper["App Bootstrapper"]
        Main[Main.java]:::app
    end

    subgraph presentation_layer["Presentation Layer"]
        ConsoleMenu[ConsoleMenu.java]:::ui
        ConsoleTable[ConsoleTable.java]:::ui
    end

    subgraph service_layer["Service Layer (Business Logic)"]
        BankService["&laquo;interface&raquo;<br>BankService"]:::service
        BankServiceImpl[BankServiceImpl.java]:::service
    end

    subgraph repository_layer["Repository Layer (Data Persistence)"]
        AccountRepository["&laquo;interface&raquo;<br>AccountRepository"]:::repo
        AccountRepositoryImpl[AccountRepositoryImpl.java]:::repo
    end

    subgraph model_layer["Model Layer (Domain Entities)"]
        Account[Account.java]:::model
        Transaction[Transaction.java]:::model
        AccountType["&laquo;enum&raquo;<br>AccountType"]:::model
        TransactionType["&laquo;enum&raquo;<br>TransactionType"]:::model
    end

    subgraph utility_exceptions["Utility & Exceptions"]
        ValidationUtil[ValidationUtil.java]:::util
        Exceptions["Custom Exceptions<br>- InsufficientBalance<br>- AccountNotFound<br>- InvalidAmount<br>- DuplicateAccount<br>- InvalidPin"]:::util
    end

    %% Dependency Arrows
    Main --> ConsoleMenu
    ConsoleMenu --> BankService
    ConsoleMenu --> ConsoleTable
    BankServiceImpl -- implements --> BankService
    BankServiceImpl --> AccountRepository
    BankServiceImpl --> ValidationUtil
    BankServiceImpl --> Exceptions
    AccountRepositoryImpl -- implements --> AccountRepository
    AccountRepositoryImpl --> Account
    AccountRepositoryImpl --> Transaction
    AccountRepositoryImpl --> utility_exceptions
    Account --> Transaction
    Account --> AccountType
    Transaction --> TransactionType

    style app_bootstrapper fill:#eceff1,stroke:#37474f,stroke-width:1px,stroke-dasharray: 5 5;
    style presentation_layer fill:#efebe9,stroke:#4e342e,stroke-width:1px,stroke-dasharray: 5 5;
    style service_layer fill:#fbe9e7,stroke:#d84315,stroke-width:1px,stroke-dasharray: 5 5;
    style repository_layer fill:#ede7f6,stroke:#4a148c,stroke-width:1px,stroke-dasharray: 5 5;
    style model_layer fill:#fce4ec,stroke:#880e4f,stroke-width:1px,stroke-dasharray: 5 5;
```

---

## 🚦 Interactive Flowchart (User Journey)

The interactive logic flow shows the lifecycle of the application, starting from loading system data to processing user commands:

```mermaid
flowchart TD
    %% Node definitions
    Start([Application Started]) --> LoadData[Initialize SQLite Database]
    LoadData --> MainMenu{Main Menu}
    
    %% Main Menu Options
    MainMenu -->|Option 1| CreateAcc[Create New Account]
    MainMenu -->|Option 2| LoginScreen[Enter Account No & PIN]
    MainMenu -->|Option 3| SearchAcc[Search Account]
    MainMenu -->|Option 4| ViewAll[View All Accounts]
    MainMenu -->|Option 5| ExitApp[Save Data & Exit]
    
    %% Create Account Path
    CreateAcc --> InputDetails[Input Name, Age, Email, Phone, Address, Type, PIN, Min Balance]
    InputDetails --> ValDetails{Validate Inputs}
    ValDetails -->|Invalid| InputDetails
    ValDetails -->|Valid| GenerateAccNo[Generate Unique Account No]
    GenerateAccNo --> SaveNew[Add to InMemory DB & Sync File]
    SaveNew --> MainMenu
    
    %% Login Screen & Attempts
    LoginScreen --> CheckCredentials{Validate Account & PIN}
    CheckCredentials -->|Fail < 3 Times| IncrementAttempts[Increment Failed Attempts] --> LoginScreen
    CheckCredentials -->|Fail >= 3 Times| LockAcc[Lock Account] --> MainMenu
    CheckCredentials -->|Success| DashMenu{User Dashboard}
    
    %% Dashboard Options
    DashMenu -->|Option 1| Deposit[Deposit Funds]
    DashMenu -->|Option 2| Withdraw[Withdraw Funds]
    DashMenu -->|Option 3| Transfer[Transfer Funds]
    DashMenu -->|Option 4| History[View Transaction History]
    DashMenu -->|Option 5| Update[Update Profile Details]
    DashMenu -->|Option 6| Delete[Delete Account]
    DashMenu -->|Option 7| Logout[Logout]
    
    %% Operations Execution
    Deposit --> ValAmount{Amount > 0?} -->|Yes| UpdateBalD[Add Balance & Save Transaction] --> DashMenu
    Withdraw --> ValBalW{Balance - MinBalance >= Amount?} -->|Yes| UpdateBalW[Deduct Balance & Save Transaction] --> DashMenu
    Transfer --> ValReceiver{Receiver Exists?} -->|Yes| ValTransferBal{Sender Balance Available?} -->|Yes| ExecTransfer[Deduct Sender & Credit Receiver] --> DashMenu
    History --> RenderTable[Fetch Transaction List & Print Table] --> DashMenu
    Update --> EditFields[Edit Address / Phone / Email / PIN] --> UpdateSave[Sync File] --> DashMenu
    Delete --> ConfirmDel{Are you sure?} -->|Yes| RemoveAcc[Remove Account & Logout] --> MainMenu
    Logout --> MainMenu

    %% Errors paths
    ValAmount -->|No| ThrowErr1[Throw InvalidAmountException] --> DashMenu
    ValBalW -->|No| ThrowErr2[Throw InsufficientBalanceException] --> DashMenu
    ValReceiver -->|No| ThrowErr3[Throw AccountNotFoundException] --> DashMenu

    %% Styling
    classDef startEnd fill:#ffe0b2,stroke:#fb8c00,stroke-width:2px;
    classDef process fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef decision fill:#fff9c4,stroke:#fbc02d,stroke-width:2px;
    classDef error fill:#ffebee,stroke:#c62828,stroke-width:2px;

    class Start,ExitApp startEnd;
    class LoadData,InputDetails,GenerateAccNo,SaveNew,IncrementAttempts,LockAcc,UpdateBalD,UpdateBalW,ExecTransfer,RenderTable,UpdateSave,RemoveAcc process;
    class MainMenu,DashMenu,ValDetails,CheckCredentials,ValAmount,ValBalW,ValReceiver,ValTransferBal,ConfirmDel decision;
    class ThrowErr1,ThrowErr2,ThrowErr3 error;
```

---

## ✨ Features Checklist

| Module | Sub-feature | Description / Validation | Status |
| :--- | :--- | :--- | :---: |
| **System** | SQLite Persistence | Auto-loads/saves account and transaction data automatically from/to local SQL tables. | `Planned` |
| **Account** | Create Account | Generates 10-digit unique numbers. Fields: Name, Age, Email, Gender, Phone, Address, Type, Balance, PIN. | `Planned` |
| **Auth** | Login | Account Number + PIN. Locks account on **3 consecutive incorrect attempts**. | `Planned` |
| **Transactions** | Deposit | Validates positive amount, logs transaction. | `Planned` |
| | Withdraw | Prevents overdraft, maintains minimum balance guidelines, logs transaction. | `Planned` |
| | Transfer | Atomically deducts sender, credits receiver, validates receiver. Logs transactions. | `Planned` |
| | History | Tabular print with Date, Time, Type, Amount, and Running Balance. | `Planned` |
| **Management** | Search | Search records by Account Number, Name, or Phone using Java Streams. | `Planned` |
| | Update | Edit Phone, Email, Address, or PIN. | `Planned` |
| | Delete | Confirmation prompt. Removes record and triggers auto-save. | `Planned` |
| | View All | Displays accounts in tabular structure. Multi-sorting using Custom `Comparators`. | `Planned` |

---

## 🧩 Java Concepts Applied

- **OOP Paradigms:**
  - **Encapsulation:** Class variables in `Account` are private and managed using getters, setters, and state-modifying methods.
  - **Inheritance & Polymorphism:** Implementation of interface models (`AccountRepositoryImpl` implements `AccountRepository`).
  - **Abstraction:** Hiding storage complexity through clean interfaces.
- **Custom Exception Handling:** Built specialized exception classes extending `Exception` to signal application-specific validation failures.
- **Java Collections & Streams:** Used `ArrayList` and `HashMap` for storing and managing accounts. Heavy reliance on Java Streams API for filtering, searching, and sorting records.
- **Modern Java API:** Used `LocalDate` and `LocalDateTime` for handling creation times and transactions timestamps.
- **Database Persistence & JDBC:** Employs JDBC drivers and SQLite databases. Uses `PreparedStatement`, transactions (`commit`/`rollback`), connection management, and SQL query syntax.

---

## 🗂️ Project Directory Structure

```text
BankManagementSystem/
│
├── README.md
├── pom.xml
├── .gitignore
├── LICENSE
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           ├── app/
│   │   │           │   └── Main.java
│   │   │           ├── exception/
│   │   │           │   ├── AccountNotFoundException.java
│   │   │           │   ├── DuplicateAccountException.java
│   │   │           │   ├── InsufficientBalanceException.java
│   │   │           │   ├── InvalidAmountException.java
│   │   │           │   └── InvalidPinException.java
│   │   │           ├── menu/
│   │   │           │   └── ConsoleMenu.java
│   │   │           ├── model/
│   │   │           │   ├── Account.java
│   │   │           │   ├── AccountType.java
│   │   │           │   ├── Transaction.java
│   │   │           │   └── TransactionType.java
│   │   │           ├── repository/
│   │   │           │   ├── AccountRepository.java
│   │   │           │   └── AccountRepositoryImpl.java
│   │   │           ├── service/
│   │   │           │   ├── BankService.java
│   │   │           │   └── BankServiceImpl.java
│   │   │           └── utility/
│   │   │               ├── ConsoleTable.java
│   │   │               ├── DatabaseUtil.java
│   │   │               └── ValidationUtil.java
│   │   └── resources/
```

---

## 🚀 How to Run

### Prerequisites
- JDK 17 (or newer)
- Apache Maven

### Installation & Execution
1. Clone the repository:
   ```bash
   git clone https://github.com/sajidtecho/Bank-Management-System.git
   cd Bank-Management-System
   ```
2. Build the project:
   ```bash
   mvn clean compile
   ```
3. Run the application:
   ```bash
   mvn exec:java
   ```

---

## 🔮 Future Enhancements
- Upgrade to client-server RDBMS databases (MySQL/PostgreSQL) or configure Spring Data JPA.
- Build a graphical user interface (GUI) using JavaFX or a Web Dashboard using Spring Boot and Thymeleaf.
- Secure customer PINs using hash algorithms (e.g., BCrypt).
- Introduce multi-currency accounts and loan interest rate calculation modules.

---

## 🎓 Learning Outcomes
- Advanced understanding of **Layered Architectures** in Java.
- Implementing database-handling techniques using SQL and JDBC while preserving transactional safety.
- Hand-on experience resolving design rules using SOLID and Clean Coding standards.
- Practiced parsing objects to strings (serialization) and reading strings back into object instances (deserialization).
