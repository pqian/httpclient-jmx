package com.github.pqian.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSettings implements HttpSettingsMBean
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpSettings.class);

    private static int defaultConnectionTimeout = 5000;
    private static int defaultSocketTimeout = 20000;
    private static int defaultMaxConnectionsPerRoute = 1000;
    private static int defaultMaxTotalConnections = 1000;

    public static final HttpSettings INSTANCE = new HttpSettings();

    @Override
    public int getDefaultConnectionTimeout()
    {
        return defaultConnectionTimeout;
    }

    @Override
    public void setDefaultConnectionTimeout(final int defaultConnectionTimeout)
    {
        HttpSettings.defaultConnectionTimeout = defaultConnectionTimeout;
        LOG.info("set defaultConnectionTimeout with {}", defaultConnectionTimeout);
    }

    @Override
    public int getDefaultSocketTimeout()
    {
        return defaultSocketTimeout;
    }

    @Override
    public void setDefaultSocketTimeout(final int defaultSocketTimeout)
    {
        HttpSettings.defaultSocketTimeout = defaultSocketTimeout;
        LOG.info("set defaultSocketTimeout with {}", defaultSocketTimeout);
    }

    @Override
    public int getDefaultMaxConnectionsPerRoute()
    {
        return defaultMaxConnectionsPerRoute;
    }

    @Override
    public void setDefaultMaxConnectionsPerRoute(final int defaultMaxConnectionsPerRoute)
    {
        HttpSettings.defaultMaxConnectionsPerRoute = defaultMaxConnectionsPerRoute;
        LOG.info("set defaultMaxConnectionsPerRoute with {}", defaultMaxConnectionsPerRoute);
    }

    @Override
    public int getDefaultMaxTotalConnections()
    {
        return defaultMaxTotalConnections;
    }

    @Override
    public void setDefaultMaxTotalConnections(final int defaultMaxTotalConnections)
    {
        HttpSettings.defaultMaxTotalConnections = defaultMaxTotalConnections;
        LOG.info("set defaultMaxTotalConnections with {}", defaultMaxTotalConnections);
    }

}
