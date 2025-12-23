package com.apress.crm.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "communications")
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID communicationId;

    private UUID customerId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Value is required")
    @Column(name = "comm_value")
    private String commValue;

    public Communication() {
    }

    public Communication(UUID communicationId, UUID customerId, String type, String commValue) {
        this.communicationId = communicationId;
        this.customerId = customerId;
        this.type = type;
        this.commValue = commValue;
    }

    public UUID getCommunicationId() {
        return communicationId;
    }

    public void setCommunicationId(UUID communicationId) {
        this.communicationId = communicationId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommValue() {
        return commValue;
    }

    public void setCommValue(String commValue) {
        this.commValue = commValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Communication that = (Communication) o;
        return Objects.equals(communicationId, that.communicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(communicationId);
    }

    @Override
    public String toString() {
        return "Communication{"
                + "communicationId=" + communicationId +
                ", type='" + type + "'" + 
                '}';
    }
}