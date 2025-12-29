package com.apress.crm.assistant.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * AI Function Tool for executing database queries.
 *
 * This tool allows the AI to execute SQL queries against the R2DBC database
 * and return the results in a format that can be understood by the LLM.
 *
 * IMPORTANT: In a production system, this would need additional security measures:
 * - Query validation and sanitization
 * - Read-only queries
 * - Query timeouts
 * - Rate limiting
 * - Audit logging
 */
public class R2dbcQueryTool {

    private static final Logger log = LoggerFactory.getLogger(R2dbcQueryTool.class);

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    public R2dbcQueryTool(DatabaseClient databaseClient, ObjectMapper objectMapper) {
        this.databaseClient = databaseClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute a SQL query and return the results as JSON.
     *
     * This function is called by the AI when it needs to query the database.
     */
    public Function<QueryRequest, String> executeQuery() {
        return request -> {
            log.info("AI is executing query: {}", request.sql());

            // Security check: Only allow SELECT queries
            String trimmedSql = request.sql().trim().toUpperCase();
            if (!trimmedSql.startsWith("SELECT")) {
                log.warn("Attempted to execute non-SELECT query: {}", request.sql());
                return "Error: Only SELECT queries are allowed for security reasons";
            }

            try {
                List<Map<String, Object>> results = executeQueryInternal(request.sql());
                String json = objectMapper.writeValueAsString(results);
                log.info("Query returned {} rows", results.size());
                return json;
            } catch (JsonProcessingException e) {
                log.error("Error serializing query results", e);
                return "Error formatting query results: " + e.getMessage();
            } catch (Exception e) {
                log.error("Error executing query", e);
                return "Error executing query: " + e.getMessage();
            }
        };
    }

    /**
     * Get the database schema information.
     *
     * This function helps the AI understand what tables and columns are available.
     */
    public Function<Void, String> getSchema() {
        return unused -> {
            log.info("AI is requesting database schema");
            try {
                String schemaQuery = """
                        SELECT
                            table_name,
                            column_name,
                            data_type,
                            is_nullable
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                        ORDER BY table_name, ordinal_position
                        """;

                List<Map<String, Object>> schema = executeQueryInternal(schemaQuery);
                String json = objectMapper.writeValueAsString(schema);
                log.info("Retrieved schema information for {} columns", schema.size());
                return json;
            } catch (JsonProcessingException e) {
                log.error("Error serializing schema", e);
                return "Error formatting schema: " + e.getMessage();
            } catch (Exception e) {
                log.error("Error retrieving schema", e);
                return "Error retrieving schema: " + e.getMessage();
            }
        };
    }

    private List<Map<String, Object>> executeQueryInternal(String sql) {
        return databaseClient.sql(sql)
                .map(rowToMap())
                .all()
                .collectList()
                .block();
    }

    private BiFunction<Row, RowMetadata, Map<String, Object>> rowToMap() {
        return (row, metadata) -> {
            Map<String, Object> map = new HashMap<>();
            metadata.getColumnMetadatas().forEach(columnMetadata -> {
                String columnName = columnMetadata.getName();
                Object value = row.get(columnName);
                map.put(columnName, value);
            });
            return map;
        };
    }

    /**
     * Request object for executing a query.
     */
    public record QueryRequest(String sql) {
    }
}
