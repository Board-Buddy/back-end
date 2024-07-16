package sumcoda.boardbuddy.repository;

import sumcoda.boardbuddy.dto.MemberResponse;

import java.util.List;

public interface GatherArticleRepositoryCustom {
    List<MemberResponse.GatherArticleDTO> findGatherArticleDTOByUsername(String username);
}
