package com.apress.crm.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the CRM Assistant.
 *
 * Users can customize the assistant's behavior by setting these properties
 * in their application.yml or application.properties file.
 *
 * Example configuration:
 * <pre>
 * crm.assistant.enabled=true
 * crm.assistant.chat-model=gpt-4o
 * crm.assistant.vector-table-name=crm_vectors
 * crm.assistant.audit.enabled=true
 * </pre>
 */
@ConfigurationProperties("crm.assistant")
public class CrmAssistantProperties {

    /**
     * Whether the CRM Assistant auto-configuration is enabled.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * The chat model to use for the AI assistant.
     * Default: gpt-4o
     */
    private String chatModel = "gpt-4o";

    /**
     * The name of the vector store table in PostgreSQL.
     * Default: crm_vectors
     */
    private String vectorTableName = "crm_vectors";

    /**
     * Audit logging configuration.
     */
    private Audit audit = new Audit();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public String getVectorTableName() {
        return vectorTableName;
    }

    public void setVectorTableName(String vectorTableName) {
        this.vectorTableName = vectorTableName;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    /**
     * Audit logging properties.
     */
    public static class Audit {
        /**
         * Whether audit logging is enabled for CRM Assistant calls.
         * Default: false
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
