package com.apress.crm.management.service;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Service
public class CorporateService {

    private final Neo4jClient neo4jClient;

    public CorporateService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void linkSubsidiary(String parentName, String subsidiaryName) {
        String cypher = """
                MERGE (p:Company {name: $parentName})
                MERGE (s:Company {name: $subsidiaryName})
                MERGE (p)-[:OWNS]->(s)
                """;
        
        neo4jClient.query(cypher)
                .bind(parentName).to("parentName")
                .bind(subsidiaryName).to("subsidiaryName")
                .run();
    }

    public Collection<Map<String, Object>> getCorporateTree(String rootCompanyName) {
        String cypher = """
                MATCH (root:Company {name: $rootName})-[:OWNS*1..]->(subsidiary)
                RETURN root.name as Parent, subsidiary.name as Subsidiary
                """;

        return neo4jClient.query(cypher)
                .bind(rootCompanyName).to("rootName")
                .fetch()
                .all();
    }
    
    // Add a simple verify method to check connectivity
    public Long countCompanies() {
        return neo4jClient.query("MATCH (c:Company) RETURN count(c)")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }
}
