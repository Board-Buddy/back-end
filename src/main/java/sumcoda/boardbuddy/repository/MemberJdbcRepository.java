package sumcoda.boardbuddy.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MemberJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void updateMemberRankScores(Map<Long, Double> memberRankScores) {
        String sql = "UPDATE member SET rank_score = ? WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, memberRankScores.entrySet(), memberRankScores.size(), (ps, entry) -> {
          ps.setDouble(1, entry.getValue());
          ps.setLong(2, entry.getKey());
        });
    }

    @Transactional
    public void updateMemberRanks(Map<Long, Integer> memberRanks) {
        String sql = "UPDATE member SET rank = ? WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, memberRanks.entrySet(), memberRanks.size(), (ps, entry) -> {
          if (entry.getValue() == null) {
            ps.setNull(1, java.sql.Types.INTEGER);
          } else {
            ps.setInt(1, entry.getValue());
          }
          ps.setLong(2, entry.getKey());
        });
    }

    @Transactional
    public void resetMonthlyCounts() {
        String sql = "UPDATE member SET monthly_excellent_count = 0, monthly_good_count = 0, monthly_bad_count = 0, monthly_no_show_count = 0, monthly_send_review_count = 0";
        jdbcTemplate.update(sql);
    }
}
