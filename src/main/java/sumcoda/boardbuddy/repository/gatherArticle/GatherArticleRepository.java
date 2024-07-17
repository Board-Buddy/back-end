package sumcoda.boardbuddy.repository.gatherArticle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sumcoda.boardbuddy.entity.GatherArticle;

import java.util.List;

import static sumcoda.boardbuddy.dto.GatherArticleResponse.*;

@Repository
public interface GatherArticleRepository extends JpaRepository<GatherArticle, Long>, GatherArticleRepositoryCustom {
    List<GatherArticleInfosDTO> findGatherArticleInfosByUsername(String username);

    List<GatherArticleInfosDTO> findParticipationsByUsername(String username);
}
