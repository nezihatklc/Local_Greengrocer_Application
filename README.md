# 🍏 Local Greengrocer Application

A comprehensive Greengrocer management system developed within the scope of the **CMPE 343 "Object-Oriented Analysis and Design"** course (Project 3). This application digitizes the order and delivery processes between customers, couriers, and the business owner (greengrocer).

## 🚀 About the Project

**Local Greengrocer Application** facilitates daily greengrocer operations with a user-friendly desktop interface (JavaFX). It includes stock tracking to ensure product freshness, courier management for fast delivery, and a detailed order system for customer satisfaction.

### 👥 User Roles and Features

The application is built upon three fundamental user roles:

1.  **👨‍💼 Administrator (Owner)**:
    *   **Product Management**: Add new fruits/vegetables, pricing, update stock, and delete products.
    *   **Order Tracking**: View incoming orders, assign couriers, and manage order status.
    *   **Reporting**: Gain insights into sales and stock status.

2.  **🛒 Customer**:
    *   **Shopping**: Filter products by category (Fruit/Vegetable) and add to cart.
    *   **Ordering**: Confirm cart, select delivery time, and use coupons if available.
    *   **Interaction**: Rate/comment on couriers and products, message the Owner.

3.  **🚚 Courier (Carrier)**:
    *   **Delivery**: View "Available" or "Selected" orders and pick them up.
    *   **Status Update**: Mark orders as "On the Way" or "Delivered".

## 🛠️ Technologies

This project was developed using the following technologies:

*   **Language**: Java 21
*   **Interface (GUI)**: JavaFX 21
*   **Database**: MySQL (Connector 8.2.0)
*   **Reporting/Invoice**: Apache PDFBox 2.0.30
*   **Build Tool**: Maven

## ⚙️ Installation and Execution

Follow these steps to run the project on your local machine.

### 1. Prerequisites
*   Java JDK 21
*   MySQL Server
*   Maven

### 2. Database Setup
The project requires a local MySQL database.
1.  Open MySQL Workbench or your command line.
2.  Run the `database_schema.sql` file located in the root directory to create the database.
    *   This script creates a user named `myuser` (password: `1234`) and a database named `greengrocer_db`.
    *   **Note**: If you wish to change database settings, you can edit the `src/main/java/com/group18/greengrocer/util/Constants.java` file.

### 3. Build Project
Navigate to the project root directory in your terminal and run:
```bash
mvn clean install
```

### 4. Run Application
To start the application after building:
```bash
mvn javafx:run
```

## 🔐 Default Login Credentials (Demo Data)

Some users are defined in the database schema (`database_schema.sql`) for testing purposes:

| Role | Username | Password |
| :--- | :--- | :--- |
| **Owner** | `own` | `own` |
| **Carrier** | `carr` | `carr` |
| **Customer** | `cust` | `cust` |

## 📂 Project Structure

*   `src/main/java/com/group18/greengrocer`
    *   `controller`: JavaFX interface controllers.
    *   `dao`: Data Access Objects layer.
    *   `model`: Data models (POJO).
    *   `service`: Business logic and services.
    *   `util`: Utility classes and `Constants`.
    *   `main`: Application launcher (`Main.java`).

## 👩‍💻 Contributors

*   Zeynep Sıla Şimşek
*   Pelin Cömertler
*   Simay Mutlu
*   Nezihat Kılıç
