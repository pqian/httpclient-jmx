package com.github.pqian.http;

import javax.management.MXBean;

@MXBean
public interface HttpClientSettingsMBean
{
    int getConnectionTimeout();

    void setConnectionTimeout(final int connectionTimeout);

    int getSocketTimeout();

    void setSocketTimeout(final int socketTimeout);
}
