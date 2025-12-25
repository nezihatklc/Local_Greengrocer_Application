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

         boolean success = messageDAO.sendMessage(message);
         if (!success) {
            throw new IllegalStateException("Message could not be sent.");
         }
    }

    

    /**
     * Retrieves all messages sent to the owner.
     * 
     * @return List of messages for the owner.
     */
    // ASSIGNED TO: Carrier
    public List<Message> getMessagesForOwner() {
               if (!sessionManager.isOwner()) {
            throw new IllegalStateException("Only owner can view incoming messages.");
        }

        return messageDAO.getMessagesForReceiver(sessionManager.getCurrentUser().getId());
 
    }

    /**
     * Retrieves all messages sent to a specific customer.
     * 
     * @param userId The ID of the customer.
     * @return List of messages for the customer.
     */
    // ASSIGNED TO: Carrier
    public List<Message> getMessagesForCustomer(int userId) {
                User currentUser = sessionManager.getCurrentUser();

        if (sessionManager.isCustomer()) {
            List<User> owners = userDAO.findUsersByRole(Role.OWNER);
            if (owners.isEmpty()) {
                throw new IllegalStateException("No owner found in the system.");
            }
            return messageDAO.getMessagesBetweenUsers(currentUser.getId(), owners.get(0).getId());
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

        
        Message reply = new Message();
        reply.setSenderId(sessionManager.getCurrentUser().getId()); // Owner
        reply.setReceiverId(originalMessage.getSenderId());        // Customer
        reply.setContent(replyContent);

        reply.setSentAt(new Timestamp(System.currentTimeMillis()));
        reply.setRead(false);

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
        throw new IllegalStateException("Access denied: You can only mark your own messages as read.");
    }

    boolean success = messageDAO.markAsRead(messageId);
    if (!success) {
        throw new IllegalStateException("Message could not be marked as read.");
      }

    }

    

}
