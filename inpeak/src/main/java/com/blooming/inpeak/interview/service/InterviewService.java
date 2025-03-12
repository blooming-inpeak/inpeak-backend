package com.blooming.inpeak.interview.service;

import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.dto.response.CalendarResponse;
import com.blooming.inpeak.interview.dto.response.RemainingInterviewsResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewRepository interviewRepository;

    /**
     * 사용자의 남은 모의면접 횟수를 가져오는 메서드
     *
     * @param memberId 사용자 ID
     * @return 모의면접 잔여 횟수
     */
    public RemainingInterviewsResponse getRemainingInterviews(Long memberId, LocalDate today) {
        boolean exists = interviewRepository.existsByMemberIdAndStartDate(memberId, today);
        return RemainingInterviewsResponse.of(exists ? 0 : 1);
    }

    /**
     * 캘린더에 인터뷰 기록을 조회하는 메서드
     *
     * @param memberId 사용자 ID
     * @param month    월
     * @param year     년
     * @return 인터뷰 시간, 아이디를 남은 리스트
     */
    public List<CalendarResponse> getCalendar(Long memberId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<Interview> result = interviewRepository.findByMemberIdAndStartDateBetween(memberId,
            startOfMonth, endOfMonth);

        return result.stream().map(CalendarResponse::from).toList();
    }

    /**
     * 인터뷰를 생성하는 메서드
     *
     * @param memberId  사용자 ID
     * @param startDate 인터뷰 시작 날짜
     * @return 인터뷰 ID
     */
    @Transactional
    public Long createInterview(Long memberId, LocalDate startDate) {
        return interviewRepository.save(Interview.of(memberId, startDate)).getId();
    }
}
