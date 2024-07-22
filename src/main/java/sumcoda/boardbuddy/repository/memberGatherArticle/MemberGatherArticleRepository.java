package sumcoda.boardbuddy.repository.memberGatherArticle;

import org.springframework.data.jpa.repository.JpaRepository;
import sumcoda.boardbuddy.entity.MemberGatherArticle;

public interface MemberGatherArticleRepository extends JpaRepository<MemberGatherArticle, Long>, MemberGatherArticleRepositoryCustom {

    boolean existsByGatherArticleIdAndMemberUsername(Long gatherArticleId, String username);
}
