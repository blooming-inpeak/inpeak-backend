package com.blooming.inpeak.interview.service;

import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.dto.response.CalendarResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.dto.response.TotalInterviewsResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;

    /**
     * 사용자의 총 인터뷰 횟수를 가져오는 메서드
     *
     * @param memberId 사용자 ID
     * @return 총 인터뷰 횟수
     */
    public TotalInterviewsResponse getTotalInterviews(Long memberId) {
        return TotalInterviewsResponse.of(interviewRepository.countByMemberId(memberId));
    }

    /**
     * 사용자의 남은 모의면접 횟수를 가져오는 메서드
     *
     * @param memberId 사용자 ID
     * @return 모의면접 잔여 횟수
     */
    public RemainingInterviewsResponse getRemainingInterviews(Long memberId) {
        LocalDate today = LocalDate.now();
        boolean exists = interviewRepository.existsByMemberIdAndStartDate(memberId, today);
        return RemainingInterviewsResponse.of(exists ? 0 : 1);
    }

    public List<CalendarResponse> getCalendar(Long memberId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<Interview> result = interviewRepository.findByMemberIdAndStartDateBetween(memberId,
            startOfMonth, endOfMonth);

        return result.stream().map(CalendarResponse::from).toList();
    }
}
