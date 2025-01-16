package ibu.edu.ba.aiapplication.rest.configuration;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import ibu.edu.ba.aiapplication.core.model.Material;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenAIConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIConfiguration.class);

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private Integer maxTokens;

    @Value("${openai.temperature}")
    private Double temperature;

    @Value("${openai.timeout}")
    private Integer timeout;

    @Value("${openai.api.key}")
    private String apiKey;

    private OpenAiService openAiService;

    @Bean
    public OpenAiService openAiService() {
        logger.info("Initializing OpenAI service...");
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("OpenAI API key is not set!");
            throw new IllegalStateException("OpenAI API key is not configured.");
        }
        
        openAiService = new OpenAiService(apiKey.trim(), Duration.ofSeconds(timeout));
        logger.info("OpenAI service initialized successfully.");
        return openAiService;
    }

    public String generateContent(Material material) {
        logger.info("Generating content for material: {}", material.getSubject());
        List<ChatMessage> messages = new ArrayList<>();
        
        // System message to set the context
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
            "You are a helpful educational content creator. Create content appropriate for the given grade level."));
        
        // User message with the material details
        String prompt = String.format(
            "Create educational content for:\n" +
            "Subject: %s\n" +
            "Grade: %s\n" +
            "Topic: %s\n" +
            "Type: %s\n" +
            "Language: %s\n\n" +
            "%s",
            material.getSubject(),
            material.getGrade(),
            material.getLessonUnit(),
            material.getMaterialType(),
            material.getLanguage(),
            material.getMaterialType().equals("Test") ? 
                "Generate a test with multiple choice questions and problem-solving tasks appropriate for this grade level." :
                "Generate creative educational material with examples and interactive exercises appropriate for this grade level."
        );
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

        // Create the completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
            .messages(messages)
            .model(model)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .build();

        try {
            // Get the response from OpenAI
            String response = openAiService.createChatCompletion(completionRequest)
                .getChoices().get(0).getMessage().getContent();
            logger.info("Generated content length: {} characters", response.length());
            return response;
        } catch (Exception e) {
            logger.error("Error generating content: {}", e.getMessage());
            throw new RuntimeException("Failed to generate content", e);
        }
    }

    public String generateCategoryForMaterial(Material material) {
        logger.info("Generating category for material: {}", material.getSubject());
        List<ChatMessage> messages = new ArrayList<>();
        
        // System message to set the context
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
            "You are a helpful assistant that categorizes educational materials. " +
            "Based on the subject and lesson unit, suggest a single, most appropriate category."));
        
        // User message with the material details
        String prompt = String.format("Subject: %s\nLesson Unit: %s\nGrade: %s\n\nSuggest a single category for this material.", 
            material.getSubject(), 
            material.getLessonUnit(),
            material.getGrade());
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

        // Create the completion request
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
            .messages(messages)
            .model(model)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .build();

        try {
            // Get the response from OpenAI
            String response = openAiService.createChatCompletion(completionRequest)
                .getChoices().get(0).getMessage().getContent();
            logger.info("Generated category: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error generating category: {}", e.getMessage());
            throw new RuntimeException("Failed to generate category", e);
        }
    }
}
