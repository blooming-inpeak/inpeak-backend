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

    @Value("${openai.models.format}")
    private String format;

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
                    Map.of("data", audioFile, "format", format))
            ))
        );
    }

    /**
     * GPT API를 이용하여 지원자의 음성 데이터를 텍스트로 변환하고, 면접 질문에 대한 답변을 생성한다.
     *
     * @param userAnswer       지원자의 텍스트 응답
     * @param questionContent 면접 질문
     * @return 면접 질문에 대한 답변
     */
    public String makeGPTResponse2(String userAnswer, String questionContent) {
        GPTRequest request = GPTRequest.of(model, makePromptMessages2(userAnswer, questionContent));

        GPTResponse response = restTemplate.postForObject(apiUrl, request, GPTResponse.class);

        return (String) response.choices().get(0).message().content();
    }

    private List<Message> makePromptMessages2(String userAnswer, String questionContent) {
        return List.of(
            new Message("system", "      당신은 기술 면접 답변을 분석하고 평가하는 AI입니다.\n"
                + "      다음 순서를 명확히 지켜서 답변해주세요.\n"
                + "\n"
                + "      1. 지원자의 답변을 받아 면접 질문에 대한 답변으로 적절한지 평가하세요.\n"
                + "         - 답변이 질문과 정확히 일치하거나 논리적으로 적절하면 `CORRECT`\n"
                + "         - 답변이 질문과 관련이 없거나 부정확하면 `INCORRECT`라고 표시하세요.\n"
                + "         - 평가 기준은 정확해야 합니다. 핵심을 다루고 있지 않다면 INCORRECT로 평가하세요.\n"
                + "\n"
                + "      2. 이후 @ 기호를 하나 달아주세요. 앞 뒤에 아무것도 없이 정확히 @ 기호만 적어주세요.\n"
                + "         - 이 기호는 정답과 피드백을 구분하는 역할을 합니다.\n"
                + "\n"
                + "      3. 마지막으로, 지원자의 답변에 대한 피드백을 제공하세요.\n"
                + "         - 답변의 논리성, 구체적인 예시 사용 여부, 개선할 점 등을 포함하세요.\n"
                + "         - 더 나은 답변을 할 수 있도록 구체적인 조언을 추가하세요.\n"
                + "         - 피드백만 제공해야 합니다. 따옴표나 다른 형식을 추가하지 마세요.\n"
                + "         - 답변 내용은 자세하게 작성해주세요.\n"
                + "         - 밑 예시와는 다르게 길고 자세하게 작성해주세요.\n"
                + "    \n"
                + "      4. 답변 예시입니다. 아래 형식을 꼭 지켜주세요.\n"
                + "         - 정답 여부 @ 피드백 내용\n"
                + "            - 예시: CORRECT @ 답변이 질문과 잘 맞습니다. 하지만 구체적인 경험을 추가하면 더 좋을 것 같습니다.\n"
                + "         - 위 예시말고 다른 형식을 임의로 추가하지 마세요. "),
            new Message("user", List.of(
                Map.of("type", "text", "text", "면접 질문: " + questionContent),
                Map.of("type", "text", "text", "유저 답변: " + userAnswer)
            ))
        );
    }
}
