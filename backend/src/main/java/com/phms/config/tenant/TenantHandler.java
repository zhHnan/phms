package com.phms.config.tenant;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.phms.common.constant.Constants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 多租户处理器（门店数据隔离）
 *
 * @author PHMS
 */
@Component
public class TenantHandler implements TenantLineHandler {

    /**
     * 需要进行租户隔离的表
     */
    private static final List<String> TENANT_TABLES = Arrays.asList(
            "biz_rooms",
            "biz_orders",
            "biz_care_logs"
    );

    /**
     * 获取当前租户ID（门店ID）
     */
    @Override
    public Expression getTenantId() {
        try {
            if (StpUtil.isLogin()) {
                Object hotelId = StpUtil.getSession().get("hotelId");
                if (hotelId != null) {
                    return new LongValue((Long) hotelId);
                }
            }
        } catch (Exception ignored) {
            // 未登录或无session时返回null
        }
        return new NullValue();
    }

    /**
     * 获取租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return "hotel_id";
    }

    /**
     * 判断是否忽略租户过滤
     * 超管可以查看所有门店数据
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 非租户隔离表，忽略
        if (!TENANT_TABLES.contains(tableName)) {
            return true;
        }

        try {
            if (StpUtil.isLogin()) {
                // 超管忽略租户过滤
                Object roleType = StpUtil.getSession().get("roleType");
                if (roleType != null && (Integer) roleType == Constants.ROLE_ADMIN) {
                    return true;
                }
                // 无门店ID时忽略（C端用户）
                Object hotelId = StpUtil.getSession().get("hotelId");
                if (hotelId == null) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // 未登录时忽略
            return true;
        }

        return false;
    }
}
