package com.apress.crm.management;

import com.apress.crm.management.service.ManagementService;
import com.apress.crm.management.repository.AddressRepository;
import com.apress.crm.management.repository.CommunicationRepository;
import com.apress.crm.management.repository.CompanyRepository;
import com.apress.crm.management.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class ServiceSpyTests extends BaseIntegrationTest {

    @MockitoSpyBean
    ManagementService managementService;

    @Test
    void shouldVerifySpecificMethodCall() {
        UUID id = UUID.randomUUID();
        
        // We only mock one specific method
        doReturn(Mono.empty()).when(managementService).triggerBackgroundSync(any());

        managementService.getCustomerDetails(id);

        // We can verify interactions on the real bean
        verify(managementService, times(1)).getCustomerDetails(id);
    }
}
