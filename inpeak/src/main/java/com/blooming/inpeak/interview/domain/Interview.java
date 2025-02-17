package com.blooming.inpeak.interview.domain;

import com.blooming.inpeak.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "interviews")
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Builder
    private Interview(Long id, Long memberId, LocalDate startDate) {
        this.id = id;
        this.memberId = memberId;
        this.startDate = startDate;
    }

    public static Interview of(Long memberId, LocalDate startDate) {
        return Interview.builder()
            .memberId(memberId)
            .startDate(startDate)
            .build();
    }
}
