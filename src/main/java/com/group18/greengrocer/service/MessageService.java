package com.group18.greengrocer.service;

import com.group18.greengrocer.model.Message;
import java.util.List;

public class MessageService {

    /**
     * Sends a message from a customer to the owner.
     * 
     * @param message The message object containing content and sender details.
     * @throws IllegalArgumentException if the sender is not a customer.
     */
    public void sendMessage(Message message) {
    }

    /**
     * Retrieves all messages sent to the owner.
     * 
     * @return List of messages for the owner.
     */
    public List<Message> getMessagesForOwner() {
        return null;
    }

    /**
     * Retrieves all messages sent to a specific customer.
     * 
     * @param userId The ID of the customer.
     * @return List of messages for the customer.
     */
    public List<Message> getMessagesForCustomer(int userId) {
        return null;
    }

    /**
     * Replies to a customer message.
     * ONLY owner can reply.
     * Reply should be stored as a new message linked to the original one.
     */
    public void replyToMessage(int originalMessageId, String replyContent) {
    }
}
