package com.github.pqian.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientConnectionManagerFactory
{
    public static final Logger LOG = LoggerFactory.getLogger(HttpClientConnectionManagerFactory.class);

    private HttpClientConnectionManagerFactory()
    {}

    /**
     * Creates a new {@link ClientConnectionManager}.
     * 
     * @return
     */
    public static HttpClientConnectionManager newInstance()
    {
        return newInstance(false);
    }

    /**
     * Returns an existing {@link ClientConnectionManager} if possible, otherwise create a new.
     * 
     * @param reuseExistingConnMgrIfPossible
     * @return
     */
    public static HttpClientConnectionManager newInstance(final boolean reuseExistingConnMgrIfPossible)
    {
        return newInstance(reuseExistingConnMgrIfPossible, null);
    }

    /**
     * Creates a new {@link ClientConnectionManager} that is monitored by a {@link HttpClientConnectionManagerSettings} MBean with the given name.
     * 
     * @param mbeanName
     * @return
     */
    public static HttpClientConnectionManager newInstance(final String mbeanName)
    {
        return newInstance(false, mbeanName);
    }

    /**
     * Returns a {@link ClientConnectionManager} monitored by a {@link HttpClientConnectionManagerSettings} MBean with the given name.
     * 
     * @param reuseExistingConnMgrIfPossible
     *            return a existing {@link ClientConnectionManager} if true
     * @param mbeanName
     * @return
     */
    public static HttpClientConnectionManager newInstance(final boolean reuseExistingConnMgrIfPossible, final String mbeanName)
    {
        if (reuseExistingConnMgrIfPossible)
        {
            final HttpClientConnectionManager mgr = MBeanRegistrar.findClientConnMgrByMbeanName(mbeanName);
            if (mgr != null)
            {
                LOG.info("Reuse clientConnectionManager {} being monitered by MBean", mgr);
                return mgr;
            }
        }

        final PoolingHttpClientConnectionManager newMgr = new PoolingHttpClientConnectionManager();
        newMgr.setDefaultMaxPerRoute(HttpSettings.INSTANCE.getDefaultMaxConnectionsPerRoute());
        newMgr.setMaxTotal(HttpSettings.INSTANCE.getDefaultMaxTotalConnections());
        final String objectName = MBeanRegistrar.registerClientConnMgrSettings(newMgr, mbeanName);
        LOG.info("ClientConnectionManager {} is being monitered by Mbean {}", newMgr, objectName);
        return newMgr;
    }
}
