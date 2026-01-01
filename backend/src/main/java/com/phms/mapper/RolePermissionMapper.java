package com.phms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phms.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联 Mapper
 *
 * @author PHMS
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 批量插入角色权限关联
     *
     * @param roleId  角色ID
     * @param permIds 权限ID列表
     * @return 插入数量
     */
    default int batchInsert(Integer roleId, List<Long> permIds) {
        int count = 0;
        for (Long permId : permIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermId(permId);
            count += insert(rp);
        }
        return count;
    }
}
