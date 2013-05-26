package com.github.pqian.http;

import javax.management.MXBean;

@MXBean
public interface HttpSettingsMBean
{
    int getDefaultConnectionTimeout();

    void setDefaultConnectionTimeout(final int defaultConnectionTimeout);

    int getDefaultSocketTimeout();

    void setDefaultSocketTimeout(final int defaultSocketTimeout);

    int getDefaultMaxConnectionsPerRoute();

    void setDefaultMaxConnectionsPerRoute(final int defaultMaxConnectionsPerRoute);

    int getDefaultMaxTotalConnections();

    void setDefaultMaxTotalConnections(final int defaultMaxTotalConnections);
}
