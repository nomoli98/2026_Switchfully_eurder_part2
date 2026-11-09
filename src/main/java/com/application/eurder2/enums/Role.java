package com.application.eurder2.enums;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
    ADMIN(Permission.CREATE_USER,
          Permission.VIEW_USERS,
          Permission.CREATE_ITEM,
          Permission.UPDATE_ITEM,
          Permission.CREATE_ORDER,
          Permission.VIEW_ORDERS,
          Permission.VIEW_MY_REPORT,
          Permission.VIEW_STOCK_OVERVIEW),
    CUSTOMER(Permission.REGISTER,
             Permission.CREATE_ORDER,
             Permission.VIEW_MY_REPORT,
             Permission.VIEW_MY_ORDERS);

    private final Set<Permission> permissions;

    Role(Permission first, Permission... others) {
        this.permissions = EnumSet.of(first, others);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}