package com.generated.ldmurdergame.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.Session;

@Mapper
public interface SessionMapper {
  @Select("SELECT id, title, start_time, capacity, last_promoted_player, last_promoted_at, "
    + "last_promotion_note FROM sessions ORDER BY start_time, id")
  List<Session> findAll();

  @Select("SELECT id, title, start_time, capacity, last_promoted_player, last_promoted_at, "
    + "last_promotion_note FROM sessions WHERE id = #{id}")
  Session findById(@Param("id") Long id);

  @Update("UPDATE sessions SET last_promoted_player = #{player}, last_promoted_at = #{promotedAt}, "
    + "last_promotion_note = #{note} WHERE id = #{sessionId}")
  int updateLastPromotion(@Param("sessionId") Long sessionId,
                          @Param("player") String player,
                          @Param("promotedAt") LocalDateTime promotedAt,
                          @Param("note") String note);
}
