package sumcoda.boardbuddy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.entity.GatherArticle;

import java.util.List;

@Repository
public interface GatherArticleRepository extends JpaRepository<GatherArticle, Long>, GatherArticleRepositoryCustom {
    List<MemberResponse.GatherArticleDTO> findGatherArticleDTOByUsername(String username);
}
