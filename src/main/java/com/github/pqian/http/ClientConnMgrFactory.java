package com.github.pqian.http;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnMgrFactory
{
    public static final Logger LOG = LoggerFactory.getLogger(ClientConnMgrFactory.class);

    private ClientConnMgrFactory()
    {}

    /**
     * Creates a new {@link ClientConnectionManager}.
     * 
     * @return
     */
    public static ClientConnectionManager newInstance()
    {
        return newInstance(false);
    }

    /**
     * Returns an existing {@link ClientConnectionManager} if possible, otherwise create a new.
     * 
     * @param reuseExistingConnMgrIfPossible
     * @return
     */
    public static ClientConnectionManager newInstance(final boolean reuseExistingConnMgrIfPossible)
    {
        return newInstance(reuseExistingConnMgrIfPossible, null);
    }

    /**
     * Creates a new {@link ClientConnectionManager} that is monitored by a {@link ClientConnMgrSettings} MBean with the given name.
     * 
     * @param mbeanName
     * @return
     */
    public static ClientConnectionManager newInstance(final String mbeanName)
    {
        return newInstance(false, mbeanName);
    }

    /**
     * Returns a {@link ClientConnectionManager} monitored by a {@link ClientConnMgrSettings} MBean with the given name.
     * 
     * @param reuseExistingConnMgrIfPossible
     *            return a existing {@link ClientConnectionManager} if true
     * @param mbeanName
     * @return
     */
    public static ClientConnectionManager newInstance(final boolean reuseExistingConnMgrIfPossible, final String mbeanName)
    {
        if (reuseExistingConnMgrIfPossible)
        {
            final ClientConnectionManager mgr = MBeanRegistrar.findClientConnMgrByObjectName(null);
            if (mgr != null)
            {
                LOG.info("Reuse clientConnectionManager {} being monitered by MBean", mgr);
                return mgr;
            }
        }

        final PoolingClientConnectionManager newMgr = new PoolingClientConnectionManager();
        newMgr.setDefaultMaxPerRoute(HttpSettings.INSTANCE.getDefaultMaxConnectionsPerRoute());
        newMgr.setMaxTotal(HttpSettings.INSTANCE.getDefaultMaxTotalConnections());
        final String objectName = MBeanRegistrar.registerClientConnMgrSettings(newMgr, mbeanName);
        LOG.info("ClientConnectionManager {} is being monitered by Mbean {}", newMgr, objectName);
        return newMgr;
    }
}
