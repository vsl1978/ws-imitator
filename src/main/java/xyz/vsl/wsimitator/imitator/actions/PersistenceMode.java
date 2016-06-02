package xyz.vsl.wsimitator.imitator.actions;

import xyz.vsl.wsimitator.util.StringUtils;

import java.util.Objects;

/**
 * Created by vsl on 19.05.2016.
 */
public enum PersistenceMode {
    enabled, disabled, cli;

    public static PersistenceMode valueOf(String value, PersistenceMode defaultValue) {
        value = StringUtils.nullSafeLowerCase(StringUtils.trimToNull(value));
        if (Objects.equals(value, enabled.name()))
            return enabled;
        if (Objects.equals(value, disabled.name()))
            return disabled;
        if (Objects.equals(value, cli.name()))
            return cli;
        return defaultValue;
    }
}
