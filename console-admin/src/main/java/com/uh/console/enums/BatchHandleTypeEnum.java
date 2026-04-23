package com.uh.console.enums;

public enum BatchHandleTypeEnum {

    NODE_MANAGER("nodeManager", 1L),
    RDS_SERVICE("rdsService", 2L),
    RDS_SENTINEL("rdsSentinel", 3L);
    private String type;
    private Long id;

    BatchHandleTypeEnum(String type, Long id) {
        this.type = type;
        this.id = id;
    }

    public static boolean check(String type) {
        for (BatchHandleTypeEnum value : BatchHandleTypeEnum.values()) {
            if (value.type.equalsIgnoreCase(type)) return true;
        }

        return false;
    }

    public static BatchHandleTypeEnum getTypeEnum(String type) {
        for (BatchHandleTypeEnum value : BatchHandleTypeEnum.values()) {
            if (value.type.equalsIgnoreCase(type)) return value;
        }

        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
