package com.group18.greengrocer.main;

import com.group18.greengrocer.model.*;
import com.group18.greengrocer.service.*;
import com.group18.greengrocer.util.Constants;
import java.util.List;
import java.util.Date;

/* 
public class BackendTest {

    private static final UserService userService = new UserService();
    private static final ProductService productService = new ProductService();
    private static final OrderService orderService = new OrderService();
    private static final AuthenticationService authService = new AuthenticationService();
    private static final ReportService reportService = new ReportService();
    private static final MessageService messageService = new MessageService();

    public static void main(String[] args) {
        System.out.println("=== STARTING BACKEND COMPLIANCE TEST ===");
        
        try {
            testUserFlow();
            testProductManagement();
            testOrderCycle();
            testReportService();
            System.out.println("\n✅ ALL TESTS COMPLETED SUCCESSFULLY.");
        } catch (Exception e) {
            System.err.println("\n❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testUserFlow() {
        System.out.println("\n-- Testing User Flow --");
        String uniqueUser = "testuser_" + System.currentTimeMillis();
        
        // 1. Register
        User newUser = new User();
        newUser.setUsername(uniqueUser);
        newUser.setPassword("Pass1234"); // Strong password req
        newUser.setRole(Role.CUSTOMER);
        newUser.setAddress("123 Test Lane");
        newUser.setPhoneNumber("5551234567");
        
        authService.register(newUser);
        System.out.println("1. Registration successful for: " + uniqueUser);

        // 2. Login
        User loggedIn = authService.login(uniqueUser, "Pass1234");
        if (loggedIn == null) throw new RuntimeException("Login failed.");
        System.out.println("2. Login successful.");

        // 3. Update Profile
        loggedIn.setAddress("456 New Addr");
        userService.updateUser(loggedIn);
        User updated = userService.getUserById(loggedIn.getId());
        if (!"456 New Addr".equals(updated.getAddress())) throw new RuntimeException("Profile update failed.");
        System.out.println("3. Profile update successful.");
    }

    private static void testProductManagement() {
        System.out.println("\n-- Testing Product Management --");
        String prodName = "TestFruit_" + System.currentTimeMillis();
        
        Product p = new Product();
        p.setName(prodName);
        p.setCategory(Category.FRUIT);
        p.setPrice(10.0);
        p.setStock(100.0);
        p.setThreshold(5.0);
        
        // Owner logic simulation
        productService.addProduct(p);
        System.out.println("1. Product added: " + prodName);
        
        List<Product> products = productService.searchProducts(prodName);
        if (products.isEmpty()) throw new RuntimeException("Product not found after add.");
        
        Product created = products.get(0);
        productService.updateStock(created.getId(), -10.0);
        
        Product updated = productService.getProductById(created.getId());
        if (updated.getStock() != 90.0) throw new RuntimeException("Stock update failed.");
        System.out.println("2. Stock update verified.");
    }

    private static void testOrderCycle() {
        System.out.println("\n-- Testing Order Cycle --");
        
        // Setup: Need a customer and a carrier and a product
        // Using existing dummy users from schema if possible, or creating new ones.
        // For safety, let's create fresh ones to ensure IDs exist.
        
        // Customer
        String custName = "cust_" + System.currentTimeMillis();
        User customer = new User();
        customer.setUsername(custName);
        customer.setPassword("Pass1234");
        customer.setRole(Role.CUSTOMER);
        customer.setAddress("Home");
        customer.setPhoneNumber("5551234567");
        authService.register(customer);
        customer = authService.login(custName, "Pass1234"); // get ID
        
        // Carrier (Owner adds carrier)
        String carrName = "carr_" + System.currentTimeMillis();
        User carrier = new User();
        carrier.setUsername(carrName);
        carrier.setPassword("Pass1234");
        carrier.setRole(Role.CARRIER); 
        carrier.setAddress("Depot");
        carrier.setPhoneNumber("5551234567");
        try {
            userService.addCarrier(carrier); // assuming this works without owner session check in service (it checks valid logic only)
        } catch (Exception e) {
            // Might fail if username taken, but we used timestamp.
            throw e;
        }
        User carrierDb = new com.group18.greengrocer.dao.UserDAO().findUserByUsername(carrName);

        // Product
        Product p = productService.getAllProducts().get(0); // Pick first available
        
        // 1. Add to Cart
        orderService.addToCart(customer.getId(), p.getId(), 2.0);
        Order cart = orderService.getCart(customer.getId());
        if (cart.getItems().isEmpty()) throw new RuntimeException("Add to cart failed.");
        System.out.println("1. Added to cart.");

        // 2. Checkout
        orderService.checkout(cart);
        int orderId = cart.getId(); // ID should be set after checkout
        System.out.println("2. Checkout complete. Order ID: " + orderId);
        
        // 3. Carrier sees order
        List<Order> available = orderService.getPendingOrders();
        boolean found = available.stream().anyMatch(o -> o.getId() == orderId);
        if (!found) throw new RuntimeException("Order not found in pending list.");
        System.out.println("3. Order visible to carrier.");

        // 4. Carrier selects order
        orderService.assignOrderToCarrier(orderId, carrierDb.getId());
        Order selected = new com.group18.greengrocer.dao.OrderDAO().findOrderById(orderId);
        if (selected.getStatus() != Order.Status.SELECTED) throw new RuntimeException("Order selection failed.");
        System.out.println("4. Order selected by carrier.");

        // 5. Carrier completes order
        orderService.completeOrder(orderId, new Date());
        Order completed = new com.group18.greengrocer.dao.OrderDAO().findOrderById(orderId);
        if (completed.getStatus() != Order.Status.COMPLETED) throw new RuntimeException("Order completion failed.");
        System.out.println("5. Order completed.");

        // 6. Rate Order
        orderService.rateOrder(orderId, 5, "Great service!");
        // Verify rating
        double rating = userService.getCarrierRating(carrierDb.getId());
        if (rating == 0.0) throw new RuntimeException("Rating not reflected.");
        System.out.println("6. Rating submitted and verified. Average: " + rating);
    }

    private static void testReportService() {
        System.out.println("\n-- Testing Report Service --");
        List<ReportData> daily = reportService.getDailySales(7);
        if (daily == null) throw new RuntimeException("Daily sales report returned null.");
        System.out.println("1. Daily sales report generated. Rows: " + daily.size());
        
        List<ReportData> prodRev = reportService.getRevenueByProduct();
        if (prodRev == null) throw new RuntimeException("Product revenue report returned null.");
        System.out.println("2. Product revenue report generated.");
    }
}
*/