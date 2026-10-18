package com.g2rain.model.nexus.utils;

import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.web.PrincipalContextHolder;

import java.util.Objects;

/**
 * 写权限校验。放在本模块 utils，不进 g2rain-common。
 */
public final class OwnerGuard {

    private OwnerGuard() {
    }

    /** 共享资源（MARKET 等）：仅运营方 / 后端可写 */
    public static void requireOperator() {
        Asserts.isTrue(
            PrincipalContextHolder.isBackEnd() || PrincipalContextHolder.isAdminCompany(),
            SystemErrorCode.UNAUTHORIZED, "非运营方");
    }

    /** 私有资源：仅所属机构可写（运营方 / 后端放行） */
    public static void requireOwner(Long ownerOrganId) {
        if (PrincipalContextHolder.isBackEnd() || PrincipalContextHolder.isAdminCompany()) {
            return;
        }
        Asserts.isTrue(Objects.equals(ownerOrganId, PrincipalContextHolder.getOrganId()),
            SystemErrorCode.UNAUTHORIZED, "非所属机构");
    }
}
