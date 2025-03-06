package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.dto.command.Message;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GPTRequest(
    String model,
    List<Message> messages,
    int temperature,
    int maxTokens,
    int topP,
    int frequencyPenalty,
    int presencePenalty
) {
    public static GPTRequest of(String model, List<Message> messages) {
        return GPTRequest.builder()
            .model(model)
            .messages(messages)
            .temperature(1)
            .maxTokens(1000)
            .topP(1)
            .frequencyPenalty(0)
            .presencePenalty(0)
            .build();
    }
}
