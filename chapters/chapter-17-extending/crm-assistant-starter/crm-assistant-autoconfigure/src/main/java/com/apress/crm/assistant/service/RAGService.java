package com.apress.crm.assistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * Service for Retrieval-Augmented Generation (RAG) using product knowledge.
 *
 * RAG enhances LLM responses by:
 * 1. Converting the user's question into an embedding vector
 * 2. Finding similar documents in the vector store
 * 3. Including relevant documents in the prompt context
 * 4. Generating a response that's grounded in the retrieved knowledge
 */
public class RAGService {

    private static final Logger log = LoggerFactory.getLogger(RAGService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RAGService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().topK(4).build())
                )
                .build();
    }

    /**
     * Answer a question using RAG with product knowledge from the vector store.
     *
     * @param question the user's question about products
     * @return the AI-generated answer augmented with retrieved product documentation
     */
    public String answerQuestion(String question) {
        log.info("Processing RAG query: {}", question);

        String response = chatClient.prompt()
                .user(question)
                .call()
                .content();

        log.info("RAG response generated successfully");
        return response;
    }

    /**
     * Answer a question with a custom system message.
     *
     * @param question the user's question
     * @param systemMessage custom instructions for the AI
     * @return the AI-generated answer
     */
    public String answerQuestion(String question, String systemMessage) {
        log.info("Processing RAG query with custom system message: {}", question);

        String response = chatClient.prompt()
                .system(systemMessage)
                .user(question)
                .call()
                .content();

        log.info("RAG response generated successfully");
        return response;
    }
}
