package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.Session;

@Mapper
public interface SessionMapper {
  @Select("SELECT id, title, script_name, dm_name, start_time, capacity, last_event FROM sessions ORDER BY id")
  List<Session> findAll();

  @Select("SELECT id, title, script_name, dm_name, start_time, capacity, last_event FROM sessions WHERE id = #{id}")
  Session findById(@Param("id") Long id);

  @Select("SELECT id, title, script_name, dm_name, start_time, capacity, last_event FROM sessions WHERE id = #{id} FOR UPDATE")
  Session findByIdForUpdate(@Param("id") Long id);

  @Update("UPDATE sessions SET last_event = #{lastEvent} WHERE id = #{id}")
  int updateLastEvent(@Param("id") Long id, @Param("lastEvent") String lastEvent);
}
