package com.dm.bomber.services.core;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public abstract class FormService extends Service {

    protected final String url;
    protected final String method;

    protected Request.Builder request;
    protected FormBody.Builder builder;

    public FormService(String url, String method, int... countryCodes) {
        super(countryCodes);

        this.url = url;
        this.method = method;
    }

    public FormService(String url, int... countryCodes) {
        this(url, "POST", countryCodes);
    }

    public void run(OkHttpClient client, Callback callback, Phone phone) {
        request = new Request.Builder();
        builder = new FormBody.Builder();

        buildBody(phone);

        request.url(url);
        request.method(method, builder.build());

        client.newCall(request.build()).enqueue(callback);
    }

    public abstract void buildBody(Phone phone);
}
