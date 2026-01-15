package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.phms.entity.LoginLog;
import com.phms.vo.LoginLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 登录日志 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    @Select("<script>" +
	    "SELECT " +
	    "  l.id, " +
	    "  CASE WHEN l.login_type = 2 THEN l.staff_id ELSE l.user_id END AS userId, " +
	    "  CASE WHEN l.login_type = 2 THEN COALESCE(s.real_name, s.email) ELSE COALESCE(u.nickname, u.phone) END AS userName, " +
	    "  CASE WHEN l.login_type = 2 THEN 'staff' ELSE 'user' END AS userType, " +
	    "  l.login_ip AS loginIp, " +
	    "  l.device_info AS userAgent, " +
	    "  l.login_status AS status, " +
	    "  CASE WHEN l.login_status = 1 THEN '登录成功' ELSE l.fail_reason END AS message, " +
	    "  l.created_at AS createdAt " +
	    "FROM sys_login_logs l " +
	    "LEFT JOIN sys_users u ON l.user_id = u.id " +
	    "LEFT JOIN sys_staff s ON l.staff_id = s.id " +
	    "WHERE l.is_deleted = 0 " +
	    "<if test='loginType != null'> AND l.login_type = #{loginType} </if>" +
	    "<if test='loginWay != null'> AND l.login_way = #{loginWay} </if>" +
	    "<if test='status != null'> AND l.login_status = #{status} </if>" +
	    "<if test='loginIp != null and loginIp != \"\"'> AND l.login_ip LIKE CONCAT('%', #{loginIp}, '%') </if>" +
	    "<if test='userName != null and userName != \"\"'> " +
	    "  AND (" +
	    "    (l.login_type = 1 AND (u.phone LIKE CONCAT('%', #{userName}, '%') OR u.nickname LIKE CONCAT('%', #{userName}, '%')))" +
	    "    OR " +
	    "    (l.login_type = 2 AND (s.email LIKE CONCAT('%', #{userName}, '%') OR s.real_name LIKE CONCAT('%', #{userName}, '%')))" +
	    "  ) " +
	    "</if>" +
	    "<if test='startTime != null'> AND l.created_at &gt;= #{startTime} </if>" +
	    "<if test='endTime != null'> AND l.created_at &lt;= #{endTime} </if>" +
	    "ORDER BY l.created_at DESC" +
	    "</script>")
    Page<LoginLogVO> selectLoginLogVOPage(Page<LoginLogVO> page,
					 @Param("userName") String userName,
					 @Param("loginIp") String loginIp,
					 @Param("loginType") Integer loginType,
					 @Param("loginWay") Integer loginWay,
					 @Param("status") Integer status,
					 @Param("startTime") LocalDateTime startTime,
					 @Param("endTime") LocalDateTime endTime);
}
