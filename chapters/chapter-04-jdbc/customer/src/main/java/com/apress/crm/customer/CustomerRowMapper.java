package com.apress.crm.customer;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CustomerRowMapper implements RowMapper<Customer> {
    @Override
    public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Customer(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}