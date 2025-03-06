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
        String systemPrompt = """
                당신은 기술 면접 답변을 분석하고 평가하는 AI입니다. 다음 순서를 명확히 지켜서 답변해주세요.

                1️⃣ 지원자의 음성 데이터를 받아 텍스트로 변환하세요.
                   - 발음 실수 또는 음성 인식 오류로 인해 잘못된 단어가 포함될 수 있습니다.
                   - 문맥을 고려하여 자연스럽게 수정하세요.
                   - 말투는 지원자의 특성을 그대로 유지해야 합니다.
                   - 내용을 임의로 추가해서는 안 됩니다.
                   - 딱 변환한 내용만 적어주세요. 따옴표나 다른 멘트를 추가하지 마세요.

                2️⃣ 이후 `@` 기호를 하나 달아주세요.

                3️⃣ 지원자가 보낸 면접 질문과 비교하여 지원자의 답변이 적절한지 평가하세요.
                   - 답변이 질문과 정확히 일치하거나 논리적으로 적절하면 `CORRECT`
                   - 답변이 질문과 관련이 없거나 부정확하면 `INCORRECT`라고 표시하세요.
                   - 평가 기준은 정확해야 합니다. 핵심을 다루고 있지 않다면 INCORRECT로 평가하세요.

                4️⃣ 이후 `@` 기호를 하나 달아주세요.

                5️⃣ 마지막으로, 지원자의 답변에 대한 피드백을 제공하세요.
                   - 답변의 논리성, 구체적인 예시 사용 여부, 개선할 점 등을 포함하세요.
                   - 더 나은 답변을 할 수 있도록 구체적인 조언을 추가하세요.
                   - 피드백만 제공해야 합니다. 따옴표나 다른 형식을 추가하지 마세요.
                   - 답변 내용은 자세하게 작성해주세요.
            """;

        return List.of(
            new Message("system", systemPrompt),
            new Message("user", List.of(
                Map.of("type", "text", "text", "면접 질문: " + questionContent),
                Map.of("type", "input_audio", "input_audio",
                    Map.of("data", audioFile, "format", "mp3"))
            ))
        );
    }
}
