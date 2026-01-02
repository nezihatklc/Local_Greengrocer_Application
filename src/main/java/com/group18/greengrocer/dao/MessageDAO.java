package com.group18.greengrocer.dao;

import com.group18.greengrocer.model.Message;
import com.group18.greengrocer.model.Message.Conversation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
     * Creates a new Conversation.
     */
    public int createConversation(int customerId) {
        String sql = "INSERT INTO Conversations (customer_id, status) VALUES (?, 'OPEN')";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets the active (OPEN) conversation for a customer.
     * Returns -1 if none exists.
     */
    public int getActiveConversationId(int customerId) {
        String sql = "SELECT id FROM Conversations WHERE customer_id = ? AND status = 'OPEN' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets the latest conversation for a customer (OPEN or CLOSED).
     * Returns -1 if none exists.
     */
    public int getLatestConversationId(int customerId) {
        String sql = "SELECT id FROM Conversations WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Closes a conversation.
     */
    public boolean closeConversation(int conversationId) {
        String sql = "UPDATE Conversations SET status = 'CLOSED', closed_at = NOW() WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sends a new message (Inserts into DB).
     * 
     * @param message The message to send.
     * @return true if successful.
     */
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO Messages (sender_id, receiver_id, content, sent_at, is_read, conversation_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, message.getSentAt());
            stmt.setBoolean(5, message.isRead());
            if (message.getConversationId() > 0) {
                stmt.setInt(6, message.getConversationId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

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
     * Retrieves messages for a specific conversation.
     */
    public List<Message> getMessagesByConversation(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username as sender_name, c.status as conversation_status " +
                "FROM Messages m " +
                "JOIN UserInfo u ON m.sender_id = u.id " +
                "LEFT JOIN Conversations c ON m.conversation_id = c.id " +
                "WHERE m.conversation_id = ? " +
                "ORDER BY sent_at ASC";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conversationId);

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
     * Retrieves "Ticket List" for Owner.
     * Returns the latest message for every conversation, ordered by status (OPEN
     * first) then date.
     */
    public List<Message> getConversationHeadsForOwner() {
        List<Message> messages = new ArrayList<>();
        // Complex query to get latest message per conversation
        // And also include conversation status
        String sql = "SELECT m.*, u.username as sender_name, c.status as conversation_status, c.id as c_id " +
                "FROM Messages m " +
                "JOIN Conversations c ON m.conversation_id = c.id " +
                "JOIN UserInfo u ON m.sender_id = u.id " +
                "WHERE m.id IN (SELECT MAX(id) FROM Messages GROUP BY conversation_id) " +
                "ORDER BY c.status = 'OPEN' DESC, m.sent_at DESC";

        // Note: The order logic puts OPEN (true=1) before CLOSED (false=0) if we treat
        // boolean expression.
        // In MySQL 'OPEN'='OPEN' is true. 'CLOSED'='OPEN' is false.
        // Actually strings: 'OPEN' vs 'CLOSED'. 'O' > 'C', so DESC sort puts OPEN
        // first.

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

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
     * Retrieves conversation history between two users (Legacy or Fallback).
     * Now updated to just return all messages irrespective of conversation, if
     * needed.
     */
    public List<Message> getMessagesBetweenUsers(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
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
     * Retrieves all messages for a receiver (Old logic).
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
        return null;
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setContent(rs.getString("content"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        m.setRead(rs.getBoolean("is_read"));

        try {
            int cId = rs.getInt("conversation_id");
            if (!rs.wasNull())
                m.setConversationId(cId);
        } catch (SQLException e) {
        }

        try {
            m.setSenderName(rs.getString("sender_name"));
        } catch (SQLException e) {
        }

        try {
            m.setConversationStatus(rs.getString("conversation_status"));
        } catch (SQLException e) {
        } // Column might not exist in some queries

        return m;
    }
}
