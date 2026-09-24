package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.Registration;

@Mapper
public interface RegistrationMapper {
  @Select("SELECT id, session_id, player_name, status, created_at FROM registrations WHERE session_id = #{sessionId} AND player_name = #{playerName}")
  Registration findBySessionAndPlayer(@Param("sessionId") Long sessionId, @Param("playerName") String playerName);

  @Select("SELECT COUNT(*) FROM registrations WHERE session_id = #{sessionId} AND status = 'SEATED'")
  int countSeated(@Param("sessionId") Long sessionId);

  @Select("SELECT COUNT(*) FROM registrations WHERE session_id = #{sessionId} AND status = 'WAITING' AND id < #{id}")
  int countWaitingAhead(@Param("sessionId") Long sessionId, @Param("id") Long id);

  @Select("SELECT id, session_id, player_name, status, created_at FROM registrations WHERE session_id = #{sessionId} AND status = 'SEATED' ORDER BY id")
  List<Registration> findSeated(@Param("sessionId") Long sessionId);

  @Select("SELECT id, session_id, player_name, status, created_at FROM registrations WHERE session_id = #{sessionId} AND status = 'WAITING' ORDER BY id")
  List<Registration> findWaiting(@Param("sessionId") Long sessionId);

  @Select("SELECT id, session_id, player_name, status, created_at FROM registrations WHERE session_id = #{sessionId} AND status = 'WAITING' ORDER BY id FETCH FIRST 1 ROW ONLY")
  Registration findFirstWaiting(@Param("sessionId") Long sessionId);

  @Insert("INSERT INTO registrations (session_id, player_name, status) VALUES (#{sessionId}, #{playerName}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Registration registration);

  @Update("UPDATE registrations SET status = 'SEATED' WHERE id = #{id} AND status = 'WAITING'")
  int promoteToSeated(@Param("id") Long id);

  @Delete("DELETE FROM registrations WHERE id = #{id}")
  int deleteById(@Param("id") Long id);
}
