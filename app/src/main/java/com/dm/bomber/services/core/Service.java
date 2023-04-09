package com.dm.bomber.services.core;

import java.util.Random;

import okhttp3.OkHttpClient;

public abstract class Service {

    private final int[] countryCodes;

    public Service(int... countryCodes) {
        this.countryCodes = countryCodes;
    }

    private static String randomString(char min, char max, int length) {
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++)
            result.append((char) (random.nextInt(max - min + 1) + min));

        return result.toString();
    }

    protected static String getRussianName() {
        return Service.randomString('а', 'я', 5);
    }

    protected static String getUserName() {
        return Service.randomString('a', 'z', 12);
    }

    protected static String getEmail() {
        return getUserName() + "@" + new String[]{"gmail.com", "mail.ru", "yandex.ru"}[new Random().nextInt(3)];
    }

    public int[] getCountryCodes() {
        return countryCodes;
    }

    public abstract void run(OkHttpClient client, Callback callback, Phone phone);
}
