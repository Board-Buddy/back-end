package sumcoda.boardbuddy.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sumcoda.boardbuddy.entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Boolean existsByUsername(String username);

    Boolean existsByNickname(String nickname);

    Boolean existsByPhoneNumber(String phoneNumber);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByNickname(String nickname);
}
