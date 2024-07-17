package sumcoda.boardbuddy.repository.gatherArticle;

import java.util.List;

import static sumcoda.boardbuddy.dto.GatherArticleResponse.*;

public interface GatherArticleRepositoryCustom {
    List<GatherArticleInfosDTO> findGatherArticleInfosByUsername(String username);

    List<GatherArticleInfosDTO> findParticipationsByUsername(String username);
}
