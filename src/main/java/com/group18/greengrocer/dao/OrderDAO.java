package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.CartItem;
import com.group18.greengrocer.model.Category;
import com.group18.greengrocer.model.Order;
import com.group18.greengrocer.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing Order entities.
 * Handles database operations for OrderInfo and OrderItems tables.
 */
public class OrderDAO {

    private DatabaseAdapter dbAdapter;

    public OrderDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Creates a new order in the database.
     * This method uses a transaction to ensure both OrderInfo and OrderItems are saved atomically.
     *
     * @param order The order to create.
     * @return true if the order was successfully created, false otherwise.
     */
    // ASSIGNED TO: Customer (Places Order)
    public boolean createOrder(Order order) {
        String insertOrderSql = "INSERT INTO OrderInfo (customer_id, carrier_id, ordertime, deliverytime, requested_delivery_date, status, totalcost, used_coupon_id, invoice) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO OrderItems (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;

        try {
            conn = dbAdapter.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Insert OrderInfo
            orderStmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getCustomerId());
            
            if (order.getCarrierId() != null) {
                orderStmt.setInt(2, order.getCarrierId());
            } else {
                orderStmt.setNull(2, Types.INTEGER);
            }
            
            orderStmt.setTimestamp(3, order.getOrderTime());
            orderStmt.setTimestamp(4, order.getDeliveryTime());
            orderStmt.setTimestamp(5, order.getRequestedDeliveryDate());
            orderStmt.setString(6, order.getStatus().name());
            orderStmt.setDouble(7, order.getTotalCost());
            
            if (order.getUsedCouponId() != null) {
                orderStmt.setInt(8, order.getUsedCouponId());
            } else {
                orderStmt.setNull(8, Types.INTEGER);
            }
            
            orderStmt.setString(9, order.getInvoice());

            int affectedRows = orderStmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // Retrieve generated ID
            try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getInt(1));
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Insert OrderItems
            itemStmt = conn.prepareStatement(insertItemSql);
            for (CartItem item : order.getItems()) {
                itemStmt.setInt(1, order.getId());
                itemStmt.setInt(2, item.getProduct().getId());
                itemStmt.setDouble(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPriceAtPurchase());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            // Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (orderStmt != null) try { orderStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (itemStmt != null) try { itemStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Note: DatabaseAdapter.getConnection might return a pooled connection, 
            // so closing it typically returns it to the pool. Use the pattern used in UserDAO (try-with-resources usually).
            // But here we needed transaction control so we manually managed it.
        }
    }

    /**
     * Finds an order by its ID, including all its items.
     *
     * @param id The ID of the order.
     * @return The Order object if found, otherwise null.
     */
    // ASSIGNED TO: Shared (Used by all roles)
    public Order findOrderById(int id) {
        String sql = "SELECT * FROM OrderInfo WHERE id = ?";
        Order order = null;

        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    order = mapOrder(rs);
                    // Load items for this order
                    loadOrderItems(order, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    /**
     * Retrieves all orders for a specific customer.
     *
     * @param customerId The ID of the customer.
     * @return A list of orders belonging to the customer.
     */
    // ASSIGNED TO: Customer (Order History)
    public List<Order> findOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM OrderInfo WHERE customer_id = ? ORDER BY ordertime DESC";

        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    loadOrderItems(order, conn);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    
    /**
     * Retrieves all orders selected by or completed by a specific carrier.
     *
     * @param carrierId The ID of the carrier.
     * @return A list of orders associated with the carrier.
     */
    // ASSIGNED TO: Carrier (Work History)
    public List<Order> findOrdersByCarrierId(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM OrderInfo WHERE carrier_id = ? ORDER BY ordertime DESC";

        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    loadOrderItems(order, conn);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Retrieves all available orders (Status = AVAILABLE).
     * These are orders waiting to be picked up by a carrier.
     *
     * @return A list of available orders.
     */
    // ASSIGNED TO: Carrier (Job Board)
    public List<Order> findAvailableOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM OrderInfo WHERE status = 'AVAILABLE' ORDER BY ordertime ASC";
        System.out.println("DEBUG DAO: Executing SQL: " + sql);

        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                System.out.println("DEBUG DAO: Found Order ID " + rs.getInt("id"));
                Order order = mapOrder(rs);
                loadOrderItems(order, conn);
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("DEBUG DAO: SQL Error in findAvailableOrders:");
            e.printStackTrace();
        }
        return orders;
    }
    
    /**
     * Retrieves all orders in the database (for Owner).
     * 
     * @return A list of all orders.
     */
    // ASSIGNED TO: Owner (Admin View)
    public List<Order> findAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM OrderInfo ORDER BY ordertime DESC";

        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = mapOrder(rs);
                loadOrderItems(order, conn);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Selects an order for a carrier.
     * Sets status to SELECTED and assigns the carrier ID.
     * Checks concurrency to ensure order is still AVAILABLE.
     *
     * @param orderId   The ID of the order to select.
     * @param carrierId The ID of the carrier selecting the order.
     * @return true if successful, false if order is no longer available.
     */
    // ASSIGNED TO: Carrier
    public boolean selectOrder(int orderId, int carrierId) {
        String sql = "UPDATE OrderInfo SET carrier_id = ?, status = 'SELECTED' WHERE id = ? AND status = 'AVAILABLE'";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Completes an order (Delivered).
     * Sets status to COMPLETED and updates delivery time.
     *
     * @param orderId The ID of the order to complete.
     * @param deliveryTime The time when the order was delivered (entered by carrier).
     * @return true if successful.
     */
    // ASSIGNED TO: Carrier
    public boolean completeOrder(int orderId, Timestamp deliveryTime) {
        String sql = "UPDATE OrderInfo SET status = 'COMPLETED', deliverytime = ? WHERE id = ? AND status = 'SELECTED'";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, deliveryTime);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cancels an order.
     * Can only cancel if the order is not yet COMPLETED.
     * 
     * @param orderId The ID of the order to cancel.
     * @return true if the cancellation was successful.
     */
    // ASSIGNED TO: Customer
    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE OrderInfo SET status = 'CANCELLED' WHERE id = ? AND status != 'COMPLETED'";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Updates the invoice content of an order.
     *
     * @param orderId  The ID of the order.
     * @param invoiceContent The invoice content (text or path).
     * @return true if successful.
     */
    // ASSIGNED TO: Carrier (After delivery)
    public boolean updateInvoice(int orderId, String invoiceContent) {
        String sql = "UPDATE OrderInfo SET invoice = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, invoiceContent);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
             e.printStackTrace();
        }
        return false;
    }

    // --- Helper Methods ---

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));
        
        int carrierId = rs.getInt("carrier_id");
        if (!rs.wasNull()) {
            order.setCarrierId(carrierId);
        }
        
        order.setOrderTime(rs.getTimestamp("ordertime"));
        order.setDeliveryTime(rs.getTimestamp("deliverytime"));
        order.setRequestedDeliveryDate(rs.getTimestamp("requested_delivery_date"));
        
        try {
            order.setStatus(Order.Status.valueOf(rs.getString("status")));
        } catch (IllegalArgumentException e) {
            // Default or error handling
            order.setStatus(Order.Status.AVAILABLE);
        }
        
        order.setTotalCost(rs.getDouble("totalcost"));
        
        int couponId = rs.getInt("used_coupon_id");
        if (!rs.wasNull()) {
            order.setUsedCouponId(couponId);
        }
        
        order.setInvoice(rs.getString("invoice"));
        return order;
    }

    private void loadOrderItems(Order order, Connection conn) {
        String sql = "SELECT oi.quantity, oi.price_at_purchase, p.id, p.name, p.category, p.type, p.price, p.stock, p.threshold, p.unit, p.imagelocation " +
                     "FROM OrderItems oi " +
                     "JOIN ProductInfo p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    try {
                        product.setCategory(Category.valueOf(rs.getString("category")));
                    } catch(Exception e) {
                        product.setCategory(Category.FRUIT); // Fallback
                    }
                    product.setType(rs.getString("type"));
                    product.setPrice(rs.getDouble("price")); // This is current price, technically we care about price_at_purchase in the item
                    product.setStock(rs.getDouble("stock"));
                    product.setThreshold(rs.getDouble("threshold"));
                    product.setUnit(rs.getString("unit"));
                    product.setImage(rs.getBytes("imagelocation"));

                    double quantity = rs.getDouble("quantity");
                    double purchasePrice = rs.getDouble("price_at_purchase");
                    
                    CartItem item = new CartItem(product, quantity);
                    item.setPriceAtPurchase(purchasePrice);
                    
                    order.addItem(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
