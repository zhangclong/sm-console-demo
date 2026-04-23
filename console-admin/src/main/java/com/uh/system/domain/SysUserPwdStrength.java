package com.uh.system.domain;

import java.util.Arrays;

public class SysUserPwdStrength {
    private Integer minLength;
    private Integer maxLength;
    private Integer[] level;

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer[] getLevel() {
        return level;
    }

    public void setLevel(Integer[] level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "SysUserPwdStrength{" +
                "minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", level=" + Arrays.toString(level) +
                '}';
    }
}
