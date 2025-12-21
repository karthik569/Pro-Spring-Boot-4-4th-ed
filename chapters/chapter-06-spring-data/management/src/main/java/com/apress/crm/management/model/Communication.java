package com.apress.crm.management.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "communication")
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID communicationId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotBlank(message = "Communication type is required")
    private String communicationType;

    @NotBlank(message = "Communication value is required")
    private String communicationValue;

    public Communication() {
    }

    public Communication(UUID communicationId, Customer customer, String communicationType, String communicationValue) {
        this.communicationId = communicationId;
        this.customer = customer;
        this.communicationType = communicationType;
        this.communicationValue = communicationValue;
    }

    public UUID getCommunicationId() {
        return communicationId;
    }

    public void setCommunicationId(UUID communicationId) {
        this.communicationId = communicationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
    }

    public String getCommunicationValue() {
        return communicationValue;
    }

    public void setCommunicationValue(String communicationValue) {
        this.communicationValue = communicationValue;
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
}
