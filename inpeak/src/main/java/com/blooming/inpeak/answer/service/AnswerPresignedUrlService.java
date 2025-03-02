package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.dto.response.AnswerPresignedUrlResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private static final Map<String, String> EXT_TO_CONTENT_TYPE = Map.of(
        ".mp4", "video/mp4",
        ".mov", "video/quicktime"
    );

    /**
     * Presigned URL 발급 메서드
     *
     * @param startDate 인터뷰 시작 날짜
     * @param fileName  확장자가 포함된 파일명 (예: "video.mp4")
     * @return Presigned URL
     */
    public AnswerPresignedUrlResponse getPreSignedUrl(Long memberId, LocalDate startDate, String fileName) {
        // 파일명에서 확장자 추출 및 Object Key 생성
        String extension = extractExtension(fileName);
        String key = generateObjectKey(memberId, startDate, extension);

        // 확장자에 따른 Content-Type 추출
        String contentType = extensionToContentType(extension);

        // S3 PUT 요청 시 필요한 메타데이터 설정
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .acl("public-read")
            .contentType(contentType)
            .build();

        // Presigned URL 생성 요청 객체 생성
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(15)) // 만료 시간(15분)
            .build();

        // Presigned URL 생성
        PresignedPutObjectRequest presignedPutObjectRequest =
            s3Presigner.presignPutObject(presignRequest);
        s3Presigner.close();

        String url = presignedPutObjectRequest.url().toString();

        return AnswerPresignedUrlResponse.of(url);
    }

    /**
     * 파일명에서 확장자 추출
     *
     * @param fileName 파일명
     * @return "."을 포함한 확장자
     */
    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1) {
            throw new RuntimeException("확장자가 없는 파일명입니다.");
        }

        return fileName.substring(dotIndex);
    }

    /**
     * S3 Object Key 생성
     *
     * @param memberId  사용자 ID
     * @param startDate 인터뷰 시작 날짜
     * @param extension 확장자 (예: ".mp4")
     * @return "videos/123/20250228/uuid.extension" 형태의 파일명
     */
    private String generateObjectKey(Long memberId, LocalDate startDate, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String dateString = startDate.format(formatter);
        String uuid = UUID.randomUUID().toString();
        return String.format("videos/%d/%s/%s%s", memberId, dateString, uuid, extension);
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
            throw new RuntimeException("지원하지 않는 파일 형식입니다.");
        }

        return EXT_TO_CONTENT_TYPE.getOrDefault(lowerExt, "application/octet-stream");
    }
}
