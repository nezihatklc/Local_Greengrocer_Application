package com.group18.greengrocer.model;

import java.sql.Timestamp;

/**
 * Represents a Message sent between users (Customer <-> Owner).
 * Corresponds to the 'Messages' table in the database.
 */
public class Message {

    /**
     * Unique identifier for the message.
     * Corresponds to 'id'.
     */
    private int id;

    /**
     * The ID of the user who sent the message.
     * Corresponds to 'sender_id'.
     */
    private int senderId;

    /**
     * The ID of the user who receives the message.
     * Corresponds to 'receiver_id'.
     */
    private int receiverId;

    /**
     * The text content of the message.
     * Corresponds to 'content'.
     */
    private String content;

    /**
     * The timestamp when the message was sent.
     * Corresponds to 'sent_at'.
     */
    private Timestamp sentAt;

    /**
     * Indicates if the message has been read by the receiver.
     * Corresponds to 'is_read'.
     */
    private boolean isRead;

    /**
     * Name of the sender (joined field, not directly in Messages table but useful
     * for UI).
     */
    private String senderName;

    /**
     * Default constructor.
     * Initializes timestamp to current time and isRead to false.
     */
    private int conversationId;
    private String conversationStatus; // For UI display purposes (joined data)

    /**
     * Default constructor.
     * Initializes timestamp to current time and isRead to false.
     */
    public Message() {
        this.sentAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }

    /**
     * Constructor for creating a new message.
     *
     * @param senderId   The sender's user ID.
     * @param receiverId The receiver's user ID.
     * @param content    The message content.
     */
    public Message(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = new Timestamp(System.currentTimeMillis());
        this.isRead = false;
    }

    /**
     * Full constructor for retrieving from database.
     *
     * @param id         The message ID.
     * @param senderId   The sender's user ID.
     * @param receiverId The receiver's user ID.
     * @param content    The message content.
     * @param sentAt     The sent timestamp.
     * @param isRead     Read status.
     */
    public Message(int id, int senderId, int receiverId, String content, Timestamp sentAt, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationStatus() {
        return conversationStatus;
    }

    public void setConversationStatus(String conversationStatus) {
        this.conversationStatus = conversationStatus;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", content='" + content + '\'' +
                ", sentAt=" + sentAt +
                ", isRead=" + isRead +
                ", conversationId=" + conversationId +
                '}';
    }

    /**
     * Checks equality based on ID.
     *
     * @param o Object to compare.
     * @return True if IDs match.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Message))
            return false;
        Message message = (Message) o;
        return id == message.id;
    }

    /**
     * HashCode based on ID.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Inner class to represent a Conversation session.
     */
    public static class Conversation {
        private int id;
        private int customerId;
        private String status; // OPEN, CLOSED
        private Timestamp createdAt;
        private Timestamp closedAt;

        public Conversation() {
        }

        public Conversation(int customerId, String status) {
            this.customerId = customerId;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }

        public Timestamp getClosedAt() {
            return closedAt;
        }

        public void setClosedAt(Timestamp closedAt) {
            this.closedAt = closedAt;
        }
    }
}
