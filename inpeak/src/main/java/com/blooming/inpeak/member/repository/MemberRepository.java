package com.blooming.inpeak.member.repository;

import com.blooming.inpeak.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);

    Optional<Member> findByKakaoId(Long kakaoId);
}
