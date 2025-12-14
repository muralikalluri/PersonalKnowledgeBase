package com.knowledge.chat.rag.controller;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping
    public ResponseEntity<String> chat(@RequestParam("query") String query) {
        // 1. Semantic Search
        // Search for top 4 most similar chunks
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(4));

        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        // 2. Construct Prompt (RAG)
        String systemText = """
                You are a helpful AI assistant answering questions based on the provided context.

                Context:
                {context}

                If the answer is not in the context, say you don't know.
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));

        Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(query)));

        // 3. Call LLM
        String response = chatClient.call(prompt).getResult().getOutput().getContent();

        return ResponseEntity.ok(response);
    }
}
