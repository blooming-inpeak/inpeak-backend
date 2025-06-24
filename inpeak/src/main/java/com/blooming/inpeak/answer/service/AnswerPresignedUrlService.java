package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.common.error.exception.BadRequestException;
import com.blooming.inpeak.common.error.exception.DownloadFailureException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerPresignedUrlService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Presigner s3Presigner;
    private final RestTemplate simpleRestTemplate;

    private static final Map<String, String> EXT_TO_CONTENT_TYPE = Map.of(
        "webm", "video/webm",
        "wav", "audio/wav"
    );

    /**
     * Presigned URL 발급 메서드
     *
     * @param startDate 인터뷰 시작 날짜
     * @param extension 확장자 (예: "mp4")
     * @param mediaType  "video" 또는 "audio"
     * @return Presigned URL
     */
    public String getPreSignedUrl(Long memberId, LocalDate startDate, String extension, String mediaType) {
        // Object Key 생성
        String key = generateObjectKey(memberId, startDate, extension, mediaType);

        // 확장자에 따른 Content-Type 추출
        String contentType = extensionToContentType(extension);

        // S3 PUT 요청 시 필요한 메타데이터 설정
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build();

        // Presigned URL 생성 요청 객체 생성
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(10)) // 만료 시간(10분)
            .build();

        // Presigned URL 생성
        PresignedPutObjectRequest presignedPutObjectRequest =
            s3Presigner.presignPutObject(presignRequest);
        s3Presigner.close();

        return presignedPutObjectRequest.url().toString();
    }

    /**
     * S3 Object Key 생성
     *
     * @param memberId  사용자 ID
     * @param startDate 인터뷰 시작 날짜
     * @param extension 확장자 (예: "mp4")
     * @return "videos/123/250228/uuid.extension" 형태의 파일명
     */
    private String generateObjectKey(Long memberId, LocalDate startDate, String extension, String mediaType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String dateString = startDate.format(formatter);
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        return String.format("%ss/%d/%s/%s.%s", mediaType, memberId, dateString, uuid, extension);
    }

    /**
     * 확장자에 따른 Content-Type 반환
     *
     * @param extension 확장자
     * @return Content-Type
     */
    private String extensionToContentType(String extension) {
        String lowerExt = extension.toLowerCase();

        if (!EXT_TO_CONTENT_TYPE.containsKey(lowerExt)) {
            throw new BadRequestException("지원하지 않는 파일 형식입니다.");
        }

        return EXT_TO_CONTENT_TYPE.getOrDefault(lowerExt, "application/octet-stream");
    }

    /**
     * S3에서 Presigned URL을 통해 오디오 파일을 다운로드합니다.
     *
     * @param presignedUrl Presigned URL
     * @return 오디오 파일의 바이트 배열
     */
    public byte[] downloadAudioFromS3(String presignedUrl) {
        ResponseEntity<byte[]> response = simpleRestTemplate.getForEntity(presignedUrl, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new DownloadFailureException("S3 Presigned URL 다운로드 실패: " + presignedUrl);
        }

        return response.getBody();
    }
}
