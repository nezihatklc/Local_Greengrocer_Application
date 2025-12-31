package com.group18.greengrocer.main;
/* 
import com.group18.greengrocer.dao.CarrierRatingDAO;
import com.group18.greengrocer.dao.ProductDAO;
import com.group18.greengrocer.dao.UserDAO;
import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Product;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;
import java.util.List;

public class DBTest {
    public static void main(String[] args) {
        System.out.println("=== Testing UserDAO ===");
        
        UserDAO userDAO = new UserDAO();
        
        // 1. Test Create User
        System.out.println("\n--- Test Create User ---");
        User newUser = new User();
        newUser.setUsername("testuser_" + System.currentTimeMillis());
        newUser.setPassword("password123");
        newUser.setRole(Role.CUSTOMER);
        newUser.setAddress("Test Address 123");
        newUser.setPhoneNumber("555-9999");
        
        boolean created = userDAO.createUser(newUser);
        System.out.println("Create User Result: " + created);
        if (created) {
            System.out.println("Created User ID: " + newUser.getId());
        }

        // 2. Test Find By ID
        System.out.println("\n--- Test Find User By ID ---");
        User foundById = userDAO.findUserById(newUser.getId());
        if (foundById != null) {
            System.out.println("Found User: " + foundById.getUsername() + ", Role: " + foundById.getRole());
        } else {
            System.out.println("User not found by ID.");
        }

        // 3. Test Find By Username
        System.out.println("\n--- Test Find User By Username ---");
        User foundByUsername = userDAO.findUserByUsername(newUser.getUsername());
        if (foundByUsername != null) {
            System.out.println("Found User: " + foundByUsername.getUsername());
        } else {
            System.out.println("User not found by Username.");
        }

        // 4. Test Update User
        System.out.println("\n--- Test Update User ---");
        if (foundById != null) {
            foundById.setAddress("Updated Address");
            boolean updated = userDAO.updateUser(foundById);
            System.out.println("Update Result: " + updated);
            
            User updatedUser = userDAO.findUserById(foundById.getId());
            System.out.println("Updated Address: " + updatedUser.getAddress());
        }

        // 5. Test Find All
        System.out.println("\n--- Test Find All Users ---");
        List<User> allUsers = userDAO.findAll();
        System.out.println("Total Users: " + allUsers.size());
        if (!allUsers.isEmpty()) {
            System.out.println("First user in list: " + allUsers.get(0).getUsername());
        }

        // 6. Test Find By Role
        System.out.println("\n--- Test Find Users By Role (CARRIER) ---");
        List<User> carriers = userDAO.findUsersByRole(Role.CARRIER);
        System.out.println("Total Carriers: " + carriers.size());
        for (User c : carriers) {
            System.out.println("Carrier: " + c.getUsername());
        }

        // 7. Test Carrier Rating (Using New CarrierRatingDAO)
        System.out.println("\n--- Test Get Carrier Rating ---");
        CarrierRatingDAO ratingDAO = new CarrierRatingDAO();
        if (!carriers.isEmpty()) {
            int carrierId = carriers.get(0).getId();
            double rating = ratingDAO.getAverageRatingForCarrier(carrierId);
            System.out.println("Carrier ID " + carrierId + " Rating: " + rating);
        }
        
        // 8. Test Update Password Specific
        System.out.println("\n--- Test Specific Password Update ---");
        if (foundById != null) {
            boolean passUpdated = userDAO.updatePassword(foundById.getId(), "newSecurePass123");
            System.out.println("Password Update Result: " + passUpdated);
            // Verify
            User reloadedUser = userDAO.findUserById(foundById.getId());
            if(reloadedUser != null && "newSecurePass123".equals(reloadedUser.getPassword())) {
                 System.out.println("Password verified as updated.");
            } else {
                 System.out.println("Password verification failed.");
            }
        }

        // 9. Test Delete User
        System.out.println("\n--- Test Delete User ---");
        if (newUser.getId() > 0) {
            boolean deleted = userDAO.deleteUser(newUser.getId());
            System.out.println("Delete Result: " + deleted);
            
            User deletedUser = userDAO.findUserById(newUser.getId());
            System.out.println("User found after delete: " + (deletedUser != null));
        }
        
        System.out.println("\n=== UserDAO Test Complete ===");
        
        System.out.println("\n=== Testing ProductDAO ===");
        ProductDAO productDAO = new ProductDAO();
        
        // 1. Insert Product
        System.out.println("\n--- Test Insert Product ---");
        Product newProduct = new Product();
        newProduct.setName("Test Apple " + System.currentTimeMillis());
        newProduct.setCategory(Category.FRUIT);
        newProduct.setType("Gala");
        newProduct.setPrice(12.50);
        newProduct.setStock(100.0);
        newProduct.setThreshold(10.0);
        newProduct.setUnit("kg");
        newProduct.setImage(new byte[0]); // Empty blob for test
        
        boolean pCreated = productDAO.insert(newProduct);
        System.out.println("Product Insert Result: " + pCreated);
        if (pCreated) {
            System.out.println("Created Product ID: " + newProduct.getId());
        }
        
        // 2. Find Product By ID
        System.out.println("\n--- Test Find Product By ID ---");
        Product foundProduct = productDAO.findById(newProduct.getId());
        if (foundProduct != null) {
            System.out.println("Found Product: " + foundProduct.getName() + " (" + foundProduct.getCategory() + ")");
        } else {
            System.out.println("Product not found by ID.");
        }
        
        // 3. Update Product
        System.out.println("\n--- Test Update Product ---");
        if (foundProduct != null) {
            foundProduct.setPrice(15.00);
            foundProduct.setStock(90.0);
            boolean pUpdated = productDAO.update(foundProduct);
            System.out.println("Product Update Result: " + pUpdated);
            
            Product updatedProduct = productDAO.findById(foundProduct.getId());
            System.out.println("Updated Price: " + updatedProduct.getPrice());
        }
        
        // 4. Find All Products
        System.out.println("\n--- Test Find All Products ---");
        List<Product> allProducts = productDAO.findAll();
        System.out.println("Total Products: " + allProducts.size());
        if (!allProducts.isEmpty()) {
            System.out.println("First Product: " + allProducts.get(0).getName());
        }
        
        // 5. Find Available Products
        System.out.println("\n--- Test Find Available Products ---");
        List<Product> availableProducts = productDAO.findAvailableProducts();
        System.out.println("Available Products: " + availableProducts.size());
        
        // 6. Delete Product
        System.out.println("\n--- Test Delete Product ---");
        if (newProduct.getId() > 0) {
            boolean pDeleted = productDAO.delete(newProduct.getId());
            System.out.println("Product Delete Result: " + pDeleted);
            
            Product deletedProduct = productDAO.findById(newProduct.getId());
            System.out.println("Product found after delete: " + (deletedProduct != null));
        }
        
        System.out.println("\n=== ProductDAO Test Complete ===");

        System.out.println("\n=== Testing OrderDAO ===");
        com.group18.greengrocer.dao.OrderDAO orderDAO = new com.group18.greengrocer.dao.OrderDAO();
        
        // Prerequisites for Order Test
        // 1. Need a Customer
        User orderCustomer = new User();
        orderCustomer.setUsername("order_cust_" + System.currentTimeMillis());
        orderCustomer.setPassword("1234");
        orderCustomer.setRole(Role.CUSTOMER);
        userDAO.createUser(orderCustomer);
        System.out.println("Created Customer for Order Test: " + orderCustomer.getId());

        // 2. Need a Carrier
        User orderCarrier = new User();
        orderCarrier.setUsername("order_carr_" + System.currentTimeMillis());
        orderCarrier.setPassword("1234");
        orderCarrier.setRole(Role.CARRIER);
        userDAO.createUser(orderCarrier);
        System.out.println("Created Carrier for Order Test: " + orderCarrier.getId());
        
        // 3. Need a Product
        Product orderProduct = new Product();
        orderProduct.setName("Order Test Prod");
        orderProduct.setCategory(Category.VEGETABLE);
        orderProduct.setPrice(5.0);
        orderProduct.setStock(100.0);
        orderProduct.setThreshold(5.0);
        productDAO.insert(orderProduct);
        System.out.println("Created Product for Order Test: " + orderProduct.getId());
        
        // --- Test Create Order ---
        System.out.println("\n--- Test Create Order ---");
        com.group18.greengrocer.model.Order newOrder = new com.group18.greengrocer.model.Order();
        newOrder.setCustomerId(orderCustomer.getId());
        newOrder.setTotalCost(50.0); // 10 * 5.0
        newOrder.setInvoice("Test Invoice Content PDF/Text");
        
        // Add Item
        com.group18.greengrocer.model.CartItem item = new com.group18.greengrocer.model.CartItem(orderProduct, 10.0);
        newOrder.addItem(item);
        
        boolean orderCreated = orderDAO.createOrder(newOrder);
        System.out.println("Create Order Result: " + orderCreated);
        if (orderCreated) {
            System.out.println("Created Order ID: " + newOrder.getId());
        }
        
        // --- Test Find Order By ID ---
        System.out.println("\n--- Test Find Order By ID ---");
        com.group18.greengrocer.model.Order foundOrder = orderDAO.findOrderById(newOrder.getId());
        if (foundOrder != null) {
            System.out.println("Found Order ID: " + foundOrder.getId());
            System.out.println("Order Status: " + foundOrder.getStatus());
            System.out.println("Items count: " + foundOrder.getItems().size());
            if (!foundOrder.getItems().isEmpty()) {
                System.out.println("First Item Product: " + foundOrder.getItems().get(0).getProduct().getName());
            }
        } else {
            System.out.println("Order not found by ID.");
        }
        
        // --- Test Find Available Orders ---
        System.out.println("\n--- Test Find Available Orders ---");
        List<com.group18.greengrocer.model.Order> availableOrders = orderDAO.findAvailableOrders();
        System.out.println("Available Orders Count: " + availableOrders.size());
        boolean isAvailable = availableOrders.stream().anyMatch(o -> o.getId() == newOrder.getId());
        System.out.println("Is created order available? " + isAvailable);
        
        // --- Test Select Order (Carrier) ---
        System.out.println("\n--- Test Select Order (by Carrier) ---");
        boolean selected = orderDAO.selectOrder(newOrder.getId(), orderCarrier.getId());
        System.out.println("Select Order Result: " + selected);
        
        foundOrder = orderDAO.findOrderById(newOrder.getId());
        System.out.println("Order Status after select: " + foundOrder.getStatus());
        System.out.println("Carrier ID in Order: " + foundOrder.getCarrierId());
        
        // --- Test Find Orders By Carrier ---
        System.out.println("\n--- Test Find Orders By Carrier ---");
        List<com.group18.greengrocer.model.Order> carrierOrders = orderDAO.findOrdersByCarrierId(orderCarrier.getId());
        System.out.println("Carrier Orders Count: " + carrierOrders.size());
        
        // --- Test Complete Order ---
        System.out.println("\n--- Test Complete Order ---");
        boolean completed = orderDAO.completeOrder(newOrder.getId(), new java.sql.Timestamp(System.currentTimeMillis()));
        System.out.println("Complete Order Result: " + completed);
        
        foundOrder = orderDAO.findOrderById(newOrder.getId());
        System.out.println("Order Status after complete: " + foundOrder.getStatus());
        System.out.println("Delivery Time: " + foundOrder.getDeliveryTime());
        
        // --- Test Update Invoice ---
        System.out.println("\n--- Test Update Invoice ---");
        boolean invoiceUpdated = orderDAO.updateInvoice(newOrder.getId(), "Updated Final Invoice");
        System.out.println("Invoice Update Result: " + invoiceUpdated);
        foundOrder = orderDAO.findOrderById(newOrder.getId());
        System.out.println("New Invoice Content: " + foundOrder.getInvoice());

        System.out.println("\n=== OrderDAO Test Complete ===");

        System.out.println("\n=== Testing CarrierRatingDAO (Full) ===");
        
        // 1. Create a Rating for the completed order
        System.out.println("\n--- Test Add Carrier Rating ---");
        com.group18.greengrocer.model.CarrierRating rating = new com.group18.greengrocer.model.CarrierRating();
        rating.setOrderId(newOrder.getId());
        rating.setCustomerId(orderCustomer.getId());
        rating.setCarrierId(orderCarrier.getId());
        rating.setRating(5);
        rating.setComment("Excellent service!");
        
        boolean ratingAdded = ratingDAO.addRating(rating);
        System.out.println("Add Rating Result: " + ratingAdded);
        if (ratingAdded) {
            System.out.println("Rating ID: " + rating.getId());
        }

        // 2. Check Average Rating
        System.out.println("\n--- Test Average Rating ---");
        double avgRating = ratingDAO.getAverageRatingForCarrier(orderCarrier.getId());
        System.out.println("Average Rating for Carrier " + orderCarrier.getId() + ": " + avgRating);

        // 3. Check hasRated
        System.out.println("\n--- Test Has Rated ---");
        boolean hasRated = ratingDAO.hasRated(newOrder.getId());
        System.out.println("Has Rated Order " + newOrder.getId() + ": " + hasRated);

        // 4. Retrieve Carrier Ratings
        System.out.println("\n--- Test Get Ratings For Carrier ---");
        List<com.group18.greengrocer.model.CarrierRating> carrierRatings = ratingDAO.getRatingsForCarrier(orderCarrier.getId());
        System.out.println("Total Ratings for Carrier: " + carrierRatings.size());
        if (!carrierRatings.isEmpty()) {
            System.out.println("First Rating Comment: " + carrierRatings.get(0).getComment());
        }
        
        System.out.println("\n=== CarrierRatingDAO Test Complete ===");
        
        System.out.println("\n=== Testing MessageDAO ===");
        com.group18.greengrocer.dao.MessageDAO messageDAO = new com.group18.greengrocer.dao.MessageDAO();
        com.group18.greengrocer.model.Message msg = new com.group18.greengrocer.model.Message(orderCustomer.getId(), orderCarrier.getId(), "Hello, where is my order?");
        boolean msgSent = messageDAO.sendMessage(msg);
        System.out.println("Message Sent Result: " + msgSent);
        
        List<com.group18.greengrocer.model.Message> chatHistory = messageDAO.getMessagesBetweenUsers(orderCustomer.getId(), orderCarrier.getId());
        System.out.println("Chat History Size: " + chatHistory.size());
        if (!chatHistory.isEmpty()) {
            System.out.println("Last Message: " + chatHistory.get(0).getContent());
        }
        System.out.println("=== MessageDAO Test Complete ===");

        System.out.println("\n=== Testing CouponDAO ===");
        com.group18.greengrocer.dao.CouponDAO couponDAO = new com.group18.greengrocer.dao.CouponDAO();
        com.group18.greengrocer.model.Coupon coupon = new com.group18.greengrocer.model.Coupon("TEST10", 10.0, new java.sql.Date(System.currentTimeMillis() + 86400000));
        boolean couponAdded = couponDAO.addCoupon(coupon);
        System.out.println("Coupon Added: " + couponAdded);
        
        com.group18.greengrocer.model.Coupon foundCoupon = couponDAO.findCouponByCode("TEST10");
        if (foundCoupon != null) {
            System.out.println("Found Coupon: " + foundCoupon.getCode() + " Active: " + foundCoupon.isActive());
        }
        System.out.println("=== CouponDAO Test Complete ===");

        System.out.println("\n=== Testing ReportDAO ===");
        com.group18.greengrocer.dao.ReportDAO reportDAO = com.group18.greengrocer.dao.ReportDAO.getInstance();
        List<com.group18.greengrocer.model.ReportData> revenueData = reportDAO.getRevenueByProduct();
        System.out.println("Revenue Report Data Points: " + revenueData.size());
        List<com.group18.greengrocer.model.ReportData> statusData = reportDAO.getOrdersByStatus();
        System.out.println("Status Report Data Points: " + statusData.size());
        System.out.println("=== ReportDAO Test Complete ===");
        
        System.out.println("\n=== Testing OrderDAO Constraints & Cancellation ===");
        // 1. Create another order
        com.group18.greengrocer.model.Order cancelOrder = new com.group18.greengrocer.model.Order();
        cancelOrder.setCustomerId(orderCustomer.getId());
        cancelOrder.setTotalCost(20.0);
        cancelOrder.setInvoice("To be cancelled");
        cancelOrder.addItem(new com.group18.greengrocer.model.CartItem(orderProduct, 2.0));
        orderDAO.createOrder(cancelOrder);
        System.out.println("Created Order to Cancel: " + cancelOrder.getId());
        
        // 2. Try to COMPLETE it directly (Should FAIL because it is AVAILABLE, not SELECTED)
        // This validates the fix "WHERE id = ? AND status = 'SELECTED'"
        boolean failComplete = orderDAO.completeOrder(cancelOrder.getId(), new java.sql.Timestamp(System.currentTimeMillis()));
        System.out.println("Attempt to complete AVAILABLE order (Should be false): " + failComplete);
        
        // 3. Cancel it
        boolean cancelled = orderDAO.cancelOrder(cancelOrder.getId());
        System.out.println("Cancel Order Result: " + cancelled);
        
        com.group18.greengrocer.model.Order finalCheck = orderDAO.findOrderById(cancelOrder.getId());
        System.out.println("Final Status of Order " + cancelOrder.getId() + ": " + finalCheck.getStatus());
        System.out.println("=== Constraints Test Complete ===");
    }
}*//
