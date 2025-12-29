package com.apress.crm.management.ai;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Initializes the vector store with product documentation.
 *
 * This component runs at application startup and populates the pgvector database
 * with product knowledge that can be used for RAG (Retrieval-Augmented Generation).
 *
 * In a production system, this would load documents from:
 * - External files (PDF, markdown, text)
 * - Documentation websites
 * - Database records
 * - CMS systems
 */
@Component
public class ProductDocumentInitializer {

    private static final Logger log = LoggerFactory.getLogger(ProductDocumentInitializer.class);

    private final VectorStore vectorStore;

    public ProductDocumentInitializer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void initializeProductDocuments() {
        log.info("Initializing product knowledge base...");

        try {
            List<Document> documents = createProductDocuments();
            vectorStore.add(documents);
            log.info("Successfully initialized {} product documents in vector store", documents.size());
        } catch (Exception e) {
            log.error("Failed to initialize product documents", e);
        }
    }

    private List<Document> createProductDocuments() {
        return List.of(
                new Document(
                        """
                        CRM Pro Basic Plan

                        The Basic plan is perfect for small teams getting started with CRM.

                        Features:
                        - Up to 5 users
                        - 1,000 contacts
                        - Basic customer management
                        - Email support
                        - Mobile app access
                        - Standard reporting

                        Pricing: $29/month
                        """,
                        Map.of("product", "Basic Plan", "category", "pricing-tier")
                ),
                new Document(
                        """
                        CRM Pro Professional Plan

                        The Professional plan is designed for growing businesses that need advanced features.

                        Features:
                        - Up to 25 users
                        - 10,000 contacts
                        - Advanced customer segmentation
                        - Sales pipeline management
                        - Email and phone support
                        - Mobile app access
                        - Advanced reporting and analytics
                        - API access
                        - Integration with third-party tools
                        - Custom fields and workflows

                        Pricing: $79/month
                        """,
                        Map.of("product", "Professional Plan", "category", "pricing-tier")
                ),
                new Document(
                        """
                        CRM Pro Enterprise Plan

                        The Enterprise plan provides unlimited scalability and premium support for large organizations.

                        Features:
                        - Unlimited users
                        - Unlimited contacts
                        - Everything in Professional plan
                        - Dedicated account manager
                        - 24/7 priority support
                        - Custom integrations
                        - Advanced security features (SSO, 2FA, audit logs)
                        - SLA guarantees (99.9% uptime)
                        - White-labeling options
                        - On-premises deployment available
                        - Custom training and onboarding

                        Pricing: Custom (starting at $499/month)
                        """,
                        Map.of("product", "Enterprise Plan", "category", "pricing-tier")
                ),
                new Document(
                        """
                        CRM Pro AI Assistant Feature

                        Our AI-powered assistant helps you work smarter with your CRM data.

                        Capabilities:
                        - Natural language queries: Ask questions in plain English
                        - Smart recommendations: Get AI-powered customer insights
                        - Automated data entry: Extract information from emails and calls
                        - Predictive analytics: Forecast sales and identify opportunities
                        - Sentiment analysis: Understand customer satisfaction

                        Available in: Professional and Enterprise plans
                        Additional cost: $15/user/month
                        """,
                        Map.of("product", "AI Assistant", "category", "feature")
                ),
                new Document(
                        """
                        CRM Pro Mobile App

                        Access your CRM data anywhere with our mobile app.

                        Features:
                        - Available for iOS and Android
                        - Offline mode: Work without internet connection
                        - Push notifications for important updates
                        - Quick actions: Log calls, update deals, add notes
                        - Voice-to-text for faster data entry
                        - Biometric authentication
                        - Dark mode support

                        Available in: All plans
                        """,
                        Map.of("product", "Mobile App", "category", "feature")
                ),
                new Document(
                        """
                        CRM Pro Integration Capabilities

                        Connect CRM Pro with your favorite tools.

                        Native Integrations:
                        - Email: Gmail, Outlook, Office 365
                        - Calendar: Google Calendar, Outlook Calendar
                        - Communication: Slack, Microsoft Teams, Zoom
                        - Marketing: Mailchimp, HubSpot, Marketo
                        - E-commerce: Shopify, WooCommerce, Magento
                        - Payment: Stripe, PayPal, Square

                        Custom Integrations:
                        - REST API with comprehensive documentation
                        - Webhooks for real-time events
                        - Zapier support for 3,000+ apps

                        Available in: Professional and Enterprise plans
                        """,
                        Map.of("product", "Integrations", "category", "feature")
                ),
                new Document(
                        """
                        CRM Pro Security and Compliance

                        We take security seriously to protect your customer data.

                        Security Features:
                        - Data encryption at rest and in transit (AES-256)
                        - Regular security audits and penetration testing
                        - Role-based access control (RBAC)
                        - Two-factor authentication (2FA)
                        - Single Sign-On (SSO) with SAML 2.0
                        - IP whitelisting
                        - Audit logs and activity tracking

                        Compliance:
                        - GDPR compliant
                        - SOC 2 Type II certified
                        - HIPAA compliant (Enterprise plan)
                        - ISO 27001 certified

                        Data residency options available for Enterprise customers.
                        """,
                        Map.of("product", "Security", "category", "compliance")
                )
        );
    }
}
