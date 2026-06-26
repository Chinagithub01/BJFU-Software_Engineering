package org.example.mapper;

import org.example.entity.Review;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface ReviewMapper {
    int insertReview(Review review);
    Review selectByPraId(@Param("praId") Integer praId);
    int insertReviewScore(@Param("reviewId") Integer reviewId,
                          @Param("rubricItemId") Integer rubricItemId,
                          @Param("score") Double score,
                          @Param("comment") String comment);
    int updateReviewTotalScore(@Param("reviewId") Integer reviewId,
                               @Param("totalScore") Double totalScore);
    int submitReview(@Param("reviewId") Integer reviewId);
    List<java.util.Map<String, Object>> selectScoresByReviewId(@Param("reviewId") Integer reviewId);
}
