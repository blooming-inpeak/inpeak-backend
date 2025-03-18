package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.repository.AnswerRepository;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerVideoCleanupService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AnswerRepository answerRepository;
    private final S3Client s3Client;

    /**
     * 특정 회원의 모든 S3 객체 삭제
     *
     * @param memberId 회원 ID
     */
    public void deleteAllS3Objects(Long memberId) {
        List<String> videoUrls = answerRepository.findAllVideoUrlsByMemberId(memberId);

        if (videoUrls.isEmpty()) {
            return;
        }

        // S3 키 추출 후 객체 삭제
        List<ObjectIdentifier> objectsToDelete = videoUrls.stream()
            .map(this::extractObjectKey)
            .map(key -> ObjectIdentifier.builder().key(key).build())
            .collect(Collectors.toList());

        try {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objectsToDelete).build())
                .build();
            s3Client.deleteObjects(deleteRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("S3 객체 삭제에 실패했습니다.", e);
        }
    }

    // S3 URL에서 객체 키 추출
    private String extractObjectKey(String s3Url) {
        try {
            URI uri = new URI(s3Url);
            return uri.getPath().substring(1);
        } catch (Exception e) {
            throw new RuntimeException("S3 URL 형식이 알맞지 않습니다: " + s3Url, e);
        }
    }
}
