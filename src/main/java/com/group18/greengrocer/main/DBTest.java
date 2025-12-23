package com.group18.greengrocer.main;

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
    }
}
