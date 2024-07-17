package sumcoda.boardbuddy.repository;

import sumcoda.boardbuddy.dto.GatherArticleResponse;

import java.util.List;

public interface GatherArticleRepositoryCustom {
    List<GatherArticleResponse.GatherArticleInfosDTO> findGatherArticleDTOByUsername(String username);
}