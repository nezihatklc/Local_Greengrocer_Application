package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing Messages.
 * Handles database operations for the Messages table.
 */
public class MessageDAO {

    private DatabaseAdapter dbAdapter;

    public MessageDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Sends a new message (Inserts into DB).
     * 
     * @param message The message to send.
     * @return true if successful.
     */
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO Messages (sender_id, receiver_id, content, sent_at, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, message.getSentAt());
            stmt.setBoolean(5, message.isRead());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Retrieves conversation history between two users (e.g., Customer and Owner).
     * 
     * @param userId1 ID of the first user.
     * @param userId2 ID of the second user.
     * @return List of messages sorted by time.
     */
    public List<Message> getMessagesBetweenUsers(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        // Select messages where (sender=u1 AND receiver=u2) OR (sender=u2 AND receiver=u1)
        String sql = "SELECT m.*, u.username as sender_name FROM Messages m " + 
                     "JOIN UserInfo u ON m.sender_id = u.id " +
                     "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                     "ORDER BY sent_at ASC";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    /**
     * Retrieves all messages for a receiver (e.g. Owner viewing all incoming).
     * 
     * @param receiverId ID of the receiver.
     * @return List of messages.
     */
    public List<Message> getMessagesForReceiver(int receiverId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username as sender_name FROM Messages m " + 
                     "JOIN UserInfo u ON m.sender_id = u.id " +
                     "WHERE receiver_id = ? " +
                     "ORDER BY sent_at DESC";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, receiverId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Marks a specific message as read.
     * 
     * @param messageId ID of the message.
     * @return true if successful.
     */
    public boolean markAsRead(int messageId) {
        String sql = "UPDATE Messages SET is_read = TRUE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Retrieves a message by its ID.
     * 
     * @param messageId The ID of the message.
     * @return The Message object, or null if not found.
     */
    public Message getMessageById(int messageId) {
        String sql = "SELECT m.*, u.username as sender_name FROM Messages m " + 
                     "JOIN UserInfo u ON m.sender_id = u.id " +
                     "WHERE m.id = ?";
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapMessage(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Not found
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setContent(rs.getString("content"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        m.setRead(rs.getBoolean("is_read"));
        
        // Sender name from join if available
        try {
            m.setSenderName(rs.getString("sender_name"));
        } catch (SQLException e) {
            // Column might not exist in all queries
        }
        return m;
    }
}
