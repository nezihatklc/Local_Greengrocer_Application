# CMPE343 – Local Greengrocer Application  
## Project Rules & Development Guidelines

This document defines **mandatory rules and constraints** for the development of the
CMPE343 Project #3 – Local Greengrocer Application.

All developers and AI assistants MUST strictly follow these rules.
Any violation may result in logical errors, demo failure, or grading penalties.

---

## 1. Architectural Rules

### 1.1 Layered Architecture (MANDATORY)

The project MUST follow a strict layered architecture. **Strict separation of concerns** is required.

#### **Layer Responsibilities:**

1.  **UI / Controller Layer (`controller` package)**
    *   **Role:** "Traffic Police". Handles user interaction (clicks, inputs).
    *   **Allowed:** Basic UI validation (e.g., "Is text field empty?", "Is format correct?").
    *   **FORBIDDEN:** SQL queries, complex business logic, direct database access.
    *   *Action:* Calls **Service** layer methods.

2.  **Service Layer (`service` package)**
    *   **Role:** "The Brain". Contains all business logic and rules.
    *   **Allowed:** Complex validation (e.g., "Is password strong?", "Is stock sufficient?", "Check threshold").
    *   **Action:** Processes data and calls **DAO** layer.

3.  **DAO / Database Layer (`dao` package)**
    *   **Role:** "Storage Access". Handles raw database operations.
    *   **Allowed:** JDBC connections, PreparedStatement, ResultSet handling.
    *   **FORBIDDEN:** Business logic (e.g., determining if a discount applies).

4.  **Model Layer (`model` package)**
    *   **Role:** Data carriers (POJOs/Entities). Simple classes with Getters/Setters.

---

### 1.2 Package & Directory Structure (STANDARDIZED)

**Java Source Code (`src/main/java`):**
com.group18.greengrocer
├── controller
├── service
├── dao
├── model
├── util
└── main

**Resources (`src/main/resources`):**
com.group18.greengrocer
├── fxml
├── css
└── images

---


## 2. Coding Standards

### 2.1 Java Rules
- Java 8 or higher
- Each class MUST have a single responsibility
- All public classes and methods MUST have JavaDoc
- Magic numbers are FORBIDDEN (use constants)

---

### 2.2 Validation & Error Handling (CRITICAL)

**Validation Location:**
1.  **Format/Type Checks:** MUST use `util.ValidatorUtil` (e.g., regex, isNumeric).
2.  **Business Logic Checks:** MUST be inside `Service` classes (e.g., check stock against DB).

The following cases MUST be handled explicitly using validation or try-catch:

- Zero or negative product amount
- Non-numeric (non-double) product amount
- Adding a product with insufficient stock
- Threshold values less than or equal to zero
- Duplicate product entries in the shopping cart
- Multiple carriers selecting the same order

⚠️ Failure to handle these MAY cause early termination of the demo.

---

## 3. User & Role Rules

### 3.1 Supported Roles
- CUSTOMER
- CARRIER
- OWNER

Each role MUST have:
- A dedicated UI
- A dedicated controller
- Role-based access control

---

### 3.2 Authentication & Registration
- Username MUST be unique
- Password MUST satisfy strong password requirements
- Failed login MUST show an alert
- Successful login MUST close the login stage

---

---

### 3.3 Messaging System
- Customers can send messages to the Owner.
- Owner can view and reply to messages.
- Messages must be stored in the database.

---

## 4. Customer Interface Rules

### 4.1 Product Display & Categories
- Products MUST be categorized as `FRUIT` or `VEGETABLE` (Use Enum).
- Minimum: 12 vegetables + 12 fruits.
- Sorted alphabetically by product name.
- Products with zero stock MUST NOT be displayed.
- Product images MUST be stored as BLOBs.

### 4.4 Discounts & Loyalty
- **Coupons:** Fixed discount amount or percentage.
- **Loyalty:** Specific rules based on past purchases (e.g., > 5 orders get 10% off).
- Logic MUST be handled in `DiscountService`.

---

### 4.2 Shopping Cart
- Implemented in a separate Stage
- Same product MUST be merged into a single cart entry
- Total price MUST include VAT
- Threshold rule:
  - If stock ≤ threshold → price is doubled

---

### 4.3 Purchase Flow
- Minimum cart value MUST be enforced
- Delivery date MUST be within 48 hours
- Coupons and loyalty discounts MUST be clearly defined
- Invoice MUST:
  - Be generated as PDF
  - Be stored as CLOB in the database
  - Be shown/shared with the customer

---

## 5. Carrier Interface Rules

### 5.1 Order States
- Available
- Selected / Current
- Completed

An order MUST NOT be selected by more than one carrier.

---

### 5.2 Delivery Completion
- Carrier MUST enter delivery date
- Order MUST be marked as delivered
- Customer MUST be able to rate the carrier

---

## 6. Owner Interface Rules

The owner MUST be able to:
- Add / update / remove products
- Set product thresholds
- Employ / fire carriers
- View all orders
- Read and reply to customer messages
- Set coupons and loyalty rules
- View carrier ratings
- View reports using charts (JavaFX Charts)

---

## 7. Database Rules

### 1.2 Package & Directory Structure (STANDARDIZED)

**Java Source Code (`src/main/java`):**
com.group18.greengrocer
├── ...

---

## 4. Customer Interface Rules

### 4.1 Product Display & Categories
- Products MUST be categorized as `FRUIT` or `VEGETABLE` (Use Enum).
- Images stored as BLOBs.

### 4.4 Discounts & Loyalty
- **Coupons:** Managed via `Coupons` table and `DiscountService`.

---

## 5. Carrier Interface Rules

### 5.1 Order States
- `AVAILABLE` (Pending, Purchased)
- `SELECTED` (Taken by Carrier)
- `COMPLETED` (Delivered)
- `CANCELLED`

### 5.2 Delivery & Rating
- `CarrierRatings` table MUST be used for ratings (Separated from OrderInfo).
- Invoice stored as `LONGTEXT` (CLOB) in `OrderInfo`.

---

## 7. Database Rules

### 7.1 Mandatory Tables
- UserInfo
- ProductInfo
- OrderInfo
- OrderItems (Normalization)
- Messages
- Coupons
- CarrierRatings

---

### 7.2 Database Access
- A single Database Adapter class MUST be used
- PreparedStatements are MANDATORY
- SQL queries MUST NOT appear in controllers

### 7.3 Data Export & Backup
- Database MUST be exported as a `.sql` file (`GroupXX.sql`).
- Must include schema and AT LEAST 25 rows for each mandatory table.

---

## 8. GUI & UX Rules

- Initial window size: 960x540
- Application MUST open centered
- UI MUST be responsive when resized
- SceneBuilder MUST be used for FXML files
- At least 6 distinct event handlers MUST exist

---

## 9. Git & Collaboration Rules

- Main branch MUST be protected
- Feature development MUST use branches
- Pull Requests are REQUIRED
- Commit message format:

End of Rules.
