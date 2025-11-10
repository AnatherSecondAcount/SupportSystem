package model;

import java.time.LocalDateTime;

public class Ticket {

    private long id;
    private String title;
    private String description;
    private Status status;
    private long createdByUserId;
    private LocalDateTime createdAt;
    private Integer categoryId; // Используем Integer, так как они могут быть NULL

    public Integer getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    private Integer priorityId;

    public enum Status {
        OPEN,
        IN_PROGRESS,
        CLOSED
    }

    // --- ПУСТОЙ КОНСТРУКТОР ---
    public Ticket() {
    }

    // --- КОНСТРУКТОР С ПОЛЯМИ ---
    public Ticket(String title, String description, Status status, long createdByUserId, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ ---
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}