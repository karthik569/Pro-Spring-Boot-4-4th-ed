package com.apress.crm.management.repository.redis;

import com.apress.crm.management.model.CustomerSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSessionRepository extends CrudRepository<CustomerSession, String> {
}