package ru.andrey.kvstorage.utils;

public final class StringUtils {
    public static boolean isEmptyOrNull(String obj) {
        return obj == null || obj.isEmpty();
    }
}