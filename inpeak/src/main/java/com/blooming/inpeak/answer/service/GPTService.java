package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.dto.command.Message;
import com.blooming.inpeak.answer.dto.request.GPTRequest;
import com.blooming.inpeak.answer.dto.response.GPTResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GPTService {

    @Value("${openai.models.audio}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.prompt}")
    private String prompt;

    private final RestTemplate restTemplate;

    /**
     * GPT API를 이용하여 지원자의 음성 데이터를 텍스트로 변환하고, 면접 질문에 대한 답변을 생성한다.
     *
     * @param audioFile       지원자의 음성 데이터
     * @param questionContent 면접 질문
     * @return 면접 질문에 대한 답변
     */
    public String makeGPTResponse(String audioFile, String questionContent) {
        GPTRequest request = GPTRequest.of(model, makePromptMessages(audioFile, questionContent));

        GPTResponse response = restTemplate.postForObject(apiUrl, request, GPTResponse.class);

        return (String) response.choices().get(0).message().content();
    }

    private List<Message> makePromptMessages(String audioFile, String questionContent) {
        return List.of(
            new Message("system", prompt),
            new Message("user", List.of(
                Map.of("type", "text", "text", "면접 질문: " + questionContent),
                Map.of("type", "input_audio", "input_audio",
                    Map.of("data", audioFile, "format", "wav"))
            ))
        );
    }
}
