package sumcoda.boardbuddy.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RankScorePoints {

  EXCELLENT_REVIEW_SCORE(5.0),
  GOOD_REVIEW_SCORE(3.0),
  BAD_REVIEW_SCORE(-1.0),
  NOSHOW_REVIEW_SCORE(-3.0),
  SEND_REVIEW_SCORE(2.0),
  GATHER_ARTICLE_SCORE(3.0),
  COMMENT_SCORE(0.5);

  private final double score;

}
