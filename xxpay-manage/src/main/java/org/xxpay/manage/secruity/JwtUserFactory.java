package org.xxpay.manage.secruity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.SysUser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(String userName, SysUser sysUser, List<GrantedAuthority> grantedAuthorities) {
        return new JwtUser(
                sysUser.getUserId(),
                sysUser.getUserName(),
                sysUser.getStatus(),
                userName,
                sysUser.getPassWord(),
                grantedAuthorities,
                sysUser.getLastPasswordResetTime(),
                sysUser.getIsSuperAdmin()
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(String role) {
        String[] roles = role.split(":");
        List<String> authorities = Arrays.asList(roles);
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}