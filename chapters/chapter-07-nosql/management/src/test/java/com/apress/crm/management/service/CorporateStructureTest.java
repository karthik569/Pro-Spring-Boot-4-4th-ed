package com.apress.crm.management.service;

import com.apress.crm.management.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CorporateStructureTest extends BaseTest {

    @Autowired
    private CorporateService corporateService;

    @Test
    void shouldLinkAndRetrieveCorporateStructure() {
        // Link some companies
        corporateService.linkSubsidiary("Apress", "Spring");
        corporateService.linkSubsidiary("Apress", "CockroachLabs");
        corporateService.linkSubsidiary("Spring", "ProjectReactor");

        // Verify count
        Long count = corporateService.countCompanies();
        assertThat(count).isEqualTo(4);

        // Verify tree
        Collection<Map<String, Object>> tree = corporateService.getCorporateTree("Apress");
        assertThat(tree).hasSize(3); // Spring, CockroachLabs, ProjectReactor (via Spring)
        
        System.out.println("  DEBUG: Corporate Tree from Apress: " + tree);
    }
}
