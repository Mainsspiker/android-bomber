package com.dm.bomber.worker;

import com.dm.bomber.services.core.Callback;
import com.dm.bomber.services.core.Phone;
import com.dm.bomber.services.core.Service;

import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FakeService extends Service {

    private final String[] fakeServices = {
            "https://www.yahoo.com",
            "https://www.amazon.com",
            "https://www.roblox.com",
            "https://www.ebay.com",
            "https://www.msn.com",
            "https://www.pinterest.com"};

    @Override
    public void run(OkHttpClient client, Callback callback, Phone phone) {
        client.newCall(new Request.Builder()
                        .url(fakeServices[new Random().nextInt(fakeServices.length)])
                        .get()
                        .build())
                .enqueue(callback);
    }
}
