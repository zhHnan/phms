package com.phms.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.phms.common.constant.Constants;
import com.phms.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限配置
 * 实现权限和角色的获取
 *
 * @author PHMS
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionService permissionService;

    /**
     * 返回指定账号id所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissions = new ArrayList<>();
        
        try {
            Object roleType = StpUtil.getSession().get("roleType");
            if (roleType == null) {
                return permissions;
            }

            int role = (Integer) roleType;

            // 从数据库查询角色对应的权限（role_type 即 role_id）
            permissions = permissionService.getPermCodesByRoleId(role);
            
            // 超管添加通配符权限
            if (role == Constants.ROLE_ADMIN && !permissions.contains("*")) {
                permissions.add(0, "*");
            }
        } catch (Exception ignored) {
            // 获取权限失败时返回空列表
        }

        return permissions;
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        
        try {
            Object roleType = StpUtil.getSession().get("roleType");
            if (roleType == null) {
                return roles;
            }

            int role = (Integer) roleType;

            switch (role) {
                case Constants.ROLE_ADMIN:
                    roles.add("admin");
                    break;
                case Constants.ROLE_MANAGER:
                    roles.add("manager");
                    break;
                case Constants.ROLE_STAFF:
                    roles.add("staff");
                    break;
                default:
                    roles.add("user");
                    break;
            }
        } catch (Exception ignored) {
            // 获取角色失败时返回空列表
        }

        return roles;
    }
}
