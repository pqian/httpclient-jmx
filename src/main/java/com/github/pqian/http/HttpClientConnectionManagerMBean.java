package com.github.pqian.http;

import javax.management.MXBean;

@MXBean
public interface HttpClientConnectionManagerMBean
{
    int getDefaultMaxPerRoute();

    void setDefaultMaxPerRoute(final int defaultMaxPerRoute);

    int getMaxTotal();

    void setMaxTotal(final int maxTotal);
}
