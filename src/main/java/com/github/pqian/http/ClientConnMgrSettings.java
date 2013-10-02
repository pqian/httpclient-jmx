package com.github.pqian.http;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnMgrSettings implements ClientConnMgrSettingsMBean
{
    private static final Logger LOG = LoggerFactory.getLogger(ClientConnMgrSettings.class);

    private static final Timer DAEMON = new Timer("ClientConnMgrSettingsUnregistrar");

    private final String objectName;
    private final WeakReference<PoolingHttpClientConnectionManager> connMgrRef;

    public ClientConnMgrSettings(final PoolingHttpClientConnectionManager connMgr, final String objectName)
    {
        this.objectName = objectName;
        connMgrRef = new WeakReference<PoolingHttpClientConnectionManager>(connMgr);

        final TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if (connMgrRef.get() == null)
                {
                    this.cancel();
                    MBeanRegistrar.unregisterMBean(objectName);
                    LOG.info("ClientConnMgrSettings unregistered {}", objectName);
                }
            }
        };
        DAEMON.schedule(task, 30 * 1000, 30 * 1000);
    }

    @Override
    public int getDefaultMaxPerRoute()
    {
        return connMgrRef.get().getDefaultMaxPerRoute();
    }

    @Override
    public void setDefaultMaxPerRoute(final int defaultMaxPerRoute)
    {
        connMgrRef.get().setDefaultMaxPerRoute(defaultMaxPerRoute);
        LOG.info("{}: set defaultMaxPerRoute with {}", objectName, defaultMaxPerRoute);
    }

    @Override
    public int getMaxTotal()
    {
        return connMgrRef.get().getMaxTotal();
    }

    @Override
    public void setMaxTotal(final int maxTotal)
    {
        connMgrRef.get().setMaxTotal(maxTotal);
        LOG.info("{}: set maxTotal with {}", objectName, maxTotal);
    }

}
