package sumcoda.boardbuddy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sumcoda.boardbuddy.entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Boolean existsByUsername(String username);

    Boolean existsByNickname(String nickname);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByNickname(String nickname);
}
