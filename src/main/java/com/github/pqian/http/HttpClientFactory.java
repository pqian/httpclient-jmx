package com.github.pqian.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientFactory.class);

    private HttpClientFactory()
    {}

    /**
     * Creates a new {@link HttpClient}
     * 
     * @return
     */
    public static HttpClient newInstance()
    {
        return newInstance(true);
    }

    /**
     * Creates a new {@link HttpClient} using an existing {@link HttpClientConnectionManager} if possible, otherwise using a new created implicitly.
     * 
     * @param reuseExistingConnMgrIfPossible
     * @return
     */
    public static HttpClient newInstance(final boolean reuseExistingConnMgrIfPossible)
    {
        return newInstance(reuseExistingConnMgrIfPossible, null);
    }

    /**
     * Creates a new {@link HttpClient} that is monitored by a {@link HttpClientSettings} MBean with the given name.
     * 
     * @param mbeanName
     * @return
     */
    public static HttpClient newInstance(final String mbeanName)
    {
        return newInstance(true, mbeanName);
    }

    /**
     * Creates a new {@link HttpClient} monitored by a {@link HttpClientSettings} MBean with the given name.
     * 
     * @param reuseExistingConnMgrIfPossible
     *            use new {@link HttpClientConnectionManager} to create {@link HttpClient} if true
     * @param mbeanName
     * @return
     */
    public static HttpClient newInstance(final boolean reuseExistingConnMgrIfPossible, final String mbeanName)
    {
        final HttpClientConnectionManager connMgr = HttpClientConnectionManagerFactory.newInstance(reuseExistingConnMgrIfPossible);
        return createNewInstance(connMgr, mbeanName);
    }

    /**
     * Creates a new {@link HttpClient} using the specified {@link HttpClientConnectionManager}
     * 
     * @param connMgr
     * @return
     */
    public static HttpClient newInstance(final HttpClientConnectionManager connMgr)
    {
        return newInstance(connMgr, null);
    }

    /**
     * Creates a new {@link HttpClient} using the specified {@link HttpClientConnectionManager}, and register a {@link HttpClientSettings} MBean with the given name
     * for this client.
     * 
     * @param connMgr
     * @param mbeanName
     * @return
     */
    public static HttpClient newInstance(final HttpClientConnectionManager connMgr, final String mbeanName)
    {
        if (!MBeanRegistrar.isMapped(connMgr))
        {
            if (connMgr instanceof PoolingHttpClientConnectionManager)
            {
                MBeanRegistrar.registerClientConnMgrSettings((PoolingHttpClientConnectionManager) connMgr);
            }
            else
            {
                LOG.warn("HttpClientConnectionManager {} cannot be monitered via JMX, only PoolingHttpClientConnectionManager is possible", connMgr);
            }
        }
        return createNewInstance(connMgr, mbeanName);
    }

    private static HttpClient createNewInstance(final HttpClientConnectionManager connMgr, final String mbeanName)
    {
        final RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(HttpSettings.INSTANCE.getDefaultConnectionTimeout())
          .setSocketTimeout(HttpSettings.INSTANCE.getDefaultSocketTimeout()).build();
    	final HttpClient client = HttpClientBuilder.create().setConnectionManager(connMgr).setDefaultRequestConfig(config).build();
        
        final String objectName = MBeanRegistrar.registerHttpClientSettings(client, mbeanName);
        LOG.info("HttpClient {} is being monitered by Mbean {}", client, objectName);
        return client;
    }

}
