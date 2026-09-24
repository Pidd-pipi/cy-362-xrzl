package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.SessionRegistration;

@Mapper
public interface RegistrationMapper {
  @Insert("INSERT INTO session_registrations (session_id, player_name, status, wait_position) "
    + "VALUES (#{sessionId}, #{playerName}, #{status}, #{waitPosition})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(SessionRegistration registration);

  @Select("SELECT id, session_id, player_name, status, wait_position, created_at, updated_at "
    + "FROM session_registrations WHERE session_id = #{sessionId} AND player_name = #{playerName}")
  SessionRegistration find(@Param("sessionId") Long sessionId,
                           @Param("playerName") String playerName);

  @Select("SELECT COUNT(*) FROM session_registrations WHERE session_id = #{sessionId} AND status = #{status}")
  int countByStatus(@Param("sessionId") Long sessionId, @Param("status") String status);

  @Select("SELECT id, session_id, player_name, status, wait_position, created_at, updated_at "
    + "FROM session_registrations WHERE session_id = #{sessionId} AND status = 'WAITING' "
    + "ORDER BY id")
  List<SessionRegistration> findWaiting(@Param("sessionId") Long sessionId);

  @Select("SELECT player_name FROM session_registrations "
    + "WHERE session_id = #{sessionId} AND status = 'SEATED' ORDER BY updated_at, id")
  List<String> findSeatedPlayers(@Param("sessionId") Long sessionId);

  @Select("SELECT id, session_id, player_name, status, wait_position, created_at, updated_at "
    + "FROM session_registrations WHERE session_id = #{sessionId} AND status = 'WAITING' "
    + "ORDER BY id LIMIT 1")
  SessionRegistration findFirstWaiting(@Param("sessionId") Long sessionId);

  @Select("SELECT COUNT(*) FROM session_registrations "
    + "WHERE session_id = #{sessionId} AND status = 'WAITING' AND id < #{registrationId}")
  int countWaitingAhead(@Param("sessionId") Long sessionId,
                        @Param("registrationId") Long registrationId);

  @Update("UPDATE session_registrations SET status = 'SEATED', wait_position = NULL, "
    + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
  int promoteToSeated(@Param("id") Long id);

  @Update("UPDATE session_registrations SET wait_position = #{position}, "
    + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
  int updateWaitPosition(@Param("id") Long id, @Param("position") int position);

  @Delete("DELETE FROM session_registrations WHERE id = #{id}")
  int deleteById(@Param("id") Long id);
}
