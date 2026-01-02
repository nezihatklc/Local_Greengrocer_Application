package com.group18.greengrocer.service;

import com.group18.greengrocer.dao.MessageDAO;
import com.group18.greengrocer.dao.UserDAO;
import com.group18.greengrocer.model.Message;
import com.group18.greengrocer.model.Role;
import com.group18.greengrocer.model.User;
import com.group18.greengrocer.util.SessionManager;
import java.sql.Timestamp;
import java.util.List;

public class MessageService {
    private final UserDAO userDAO;
    private final MessageDAO messageDAO;
    private final SessionManager sessionManager;

    public MessageService() {
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Sends a message from a customer to the owner.
     * 
     * @param message The message object containing content and sender details.
     * @throws IllegalArgumentException if the sender is not a customer.
     */
    // ASSIGNED TO: Carrier (Communication Module Owner)
    /**
     * Sends a message from a customer to the owner.
     * Starts a new conversation if none exists.
     * 
     * @param message The message object containing content and sender details.
     * @throws IllegalArgumentException if the sender is not a customer.
     */
    // ASSIGNED TO: Carrier (Communication Module Owner)
    public void sendMessage(Message message) {
        if (!sessionManager.isCustomer()) {
            throw new IllegalArgumentException("Only customers can send messages to the owner.");
        }

        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        User customer = sessionManager.getCurrentUser();
        message.setSenderId(customer.getId());

        List<User> owners = userDAO.findUsersByRole(Role.OWNER);
        if (owners.isEmpty()) {
            throw new IllegalStateException("No owner found in the system.");
        }

        message.setReceiverId(owners.get(0).getId());
        message.setSentAt(new Timestamp(System.currentTimeMillis()));
        message.setRead(false);

        // CONVERSATION LOGIC
        int activeConvId = messageDAO.getActiveConversationId(customer.getId());
        if (activeConvId == -1) {
            activeConvId = messageDAO.createConversation(customer.getId());
        }
        message.setConversationId(activeConvId);

        boolean success = messageDAO.sendMessage(message);
        if (!success) {
            throw new IllegalStateException("Message could not be sent.");
        }
    }

    /**
     * Retrieves the list of Support Tickets (Conversations) for the Owner.
     * Returns the latest message for each conversation.
     */
    // ASSIGNED TO: Carrier
    public List<Message> getMessagesForOwner() {
        if (!sessionManager.isOwner()) {
            throw new IllegalStateException("Only owner can view incoming messages.");
        }
        // Use new ticket-based query
        return messageDAO.getConversationHeadsForOwner();
    }

    /**
     * Retrieves specific conversation history.
     */
    public List<Message> getConversation(int conversationId) {
        return messageDAO.getMessagesByConversation(conversationId);
    }

    public void closeConversation(int conversationId) {
        if (!sessionManager.isOwner())
            return; // Security check
        messageDAO.closeConversation(conversationId);
    }

    /**
     * Retrieves active conversation for a customer.
     */
    public List<Message> getActiveConversationForCustomer(int customerId) {
        int activeId = messageDAO.getActiveConversationId(customerId);
        if (activeId != -1) {
            return messageDAO.getMessagesByConversation(activeId);
        }
        return new java.util.ArrayList<>(); // Empty if no active conversation
    }

    /**
     * Retrieves the latest conversation (Active or Closed) for a customer.
     * Ensures history is visible even if closed.
     */
    public List<Message> getLatestConversationForCustomer(int customerId) {
        int id = messageDAO.getLatestConversationId(customerId);
        if (id != -1) {
            return messageDAO.getMessagesByConversation(id);
        }
        return new java.util.ArrayList<>();
    }

    /**
     * Retrieves all messages sent to a specific customer.
     * Modified to return meaningful data if needed, but
     * getActiveConversationForCustomer is preferred.
     */
    // ASSIGNED TO: Carrier
    public List<Message> getMessagesForCustomer(int userId) {
        User currentUser = sessionManager.getCurrentUser();

        if (sessionManager.isCustomer()) {
            // For customer, we prefer the active conversation view
            return getActiveConversationForCustomer(currentUser.getId());
        }

        if (sessionManager.isOwner()) {
            return messageDAO.getMessagesBetweenUsers(currentUser.getId(), userId);
        }

        throw new IllegalStateException("Unauthorized access.");
    }

    /**
     * Replies to a customer message.
     * ONLY owner can reply.
     * Reply should be stored as a new message linked to the original one.
     */
    // ASSIGNED TO: Carrier
    public void replyToMessage(int originalMessageId, String replyContent) {
        if (!sessionManager.isOwner()) {
            throw new IllegalStateException("Only owner can reply to messages.");
        }

        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply content cannot be empty.");
        }

        Message originalMessage = messageDAO.getMessageById(originalMessageId);

        if (originalMessage == null) {
            throw new IllegalArgumentException("Original message not found.");
        }

        // CHECK IF CONVERSATION IS OPEN
        // We can check by querying the message status again or trusting input
        // Ideally the DAO returns joined status
        if ("CLOSED".equalsIgnoreCase(originalMessage.getConversationStatus())) {
            throw new IllegalStateException("Cannot reply to a CLOSED conversation.");
        }

        Message reply = new Message();
        reply.setSenderId(sessionManager.getCurrentUser().getId()); // Owner
        reply.setReceiverId(originalMessage.getSenderId()); // Customer
        reply.setContent(replyContent);

        reply.setSentAt(new Timestamp(System.currentTimeMillis()));
        reply.setRead(false);
        reply.setConversationId(originalMessage.getConversationId());

        boolean success = messageDAO.sendMessage(reply);
        if (!success) {
            throw new IllegalStateException("Reply could not be sent.");
        }
    }

    public void markMessageAsRead(int messageId) {

        if (!sessionManager.isOwner() && !sessionManager.isCustomer()) {
            throw new IllegalStateException("Unauthorized access.");
        }

        User currentUser = sessionManager.getCurrentUser();

        Message msg = messageDAO.getMessageById(messageId);
        if (msg == null) {
            throw new IllegalArgumentException("Message not found.");
        }

        if (msg.getReceiverId() != currentUser.getId()) {
            // Strict check: Receiver must be current user
            // But for Ticket system, Owner might read ANY message in the thread?
            // For now keep strict.
            throw new IllegalStateException("Access denied: You can only mark your own messages as read.");
        }

        boolean success = messageDAO.markAsRead(messageId);
        if (!success) {
            throw new IllegalStateException("Message could not be marked as read.");
        }

    }

}
