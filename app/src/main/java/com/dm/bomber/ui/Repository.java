package com.dm.bomber.ui;

import com.dm.bomber.BuildVars;
import com.dm.bomber.services.core.ServicesRepository;
import com.dm.bomber.worker.AuthableProxy;

import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;

public interface Repository {
    void setTheme(int mode);

    int getTheme();

    void setLastPhone(String phoneNumber);

    String getLastPhone();

    void setLastCountryCode(int phoneCode);

    int getLastCountryCode();

    void setRawProxy(String proxyStrings);

    String getRawProxy();

    List<AuthableProxy> getProxy();

    List<AuthableProxy> parseProxy(String proxyStrings);

    void setProxyEnabled(boolean enabled);

    boolean isProxyEnabled();

    void setSnowfallEnabled(boolean enabled);

    boolean isSnowfallEnabled();

    void setRemoteServicesEnabled(boolean enabled);

    void setRemoteServicesUrls(Set<String> urls);

    Set<String> getRemoteServicesUrls();

    boolean isRemoteServicesEnabled();

    List<ServicesRepository> getAllRepositories(OkHttpClient client);

    void setAttackSpeed(BuildVars.AttackSpeed attackSpeed);

    BuildVars.AttackSpeed getAttackSpeed();
}
