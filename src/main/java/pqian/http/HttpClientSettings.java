package pqian.http;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientSettings implements HttpClientSettingsMBean
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientSettings.class);

    private static final Timer DAEMON = new Timer("ClientHttpSettingsUnregistrar");

    private final String objectName;
    private final WeakReference<HttpClient> clientRef;

    public HttpClientSettings(final HttpClient client, final String objectName)
    {
        this.objectName = objectName;
        clientRef = new WeakReference<HttpClient>(client);

        final TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if (clientRef.get() == null)
                {
                    this.cancel();
                    MBeanRegistrar.unregisterMBean(objectName);
                    LOG.info("ClientHttpSettings unregistered {}", objectName);
                }
            }
        };
        DAEMON.schedule(task, 30 * 1000, 30 * 1000);
    }

    @Override
    public int getConnectionTimeout()
    {
        return HttpConnectionParams.getConnectionTimeout(clientRef.get().getParams());
    }

    @Override
    public void setConnectionTimeout(final int connectionTimeout)
    {
        HttpConnectionParams.setConnectionTimeout(clientRef.get().getParams(), connectionTimeout);
        LOG.info("{}: set connectionTimeout with {}", objectName, connectionTimeout);
    }

    @Override
    public int getSocketTimeout()
    {
        return HttpConnectionParams.getSoTimeout(clientRef.get().getParams());
    }

    @Override
    public void setSocketTimeout(final int socketTimeout)
    {
        HttpConnectionParams.setSoTimeout(clientRef.get().getParams(), socketTimeout);
        LOG.info("{}: set socketTimeout with {}", objectName, socketTimeout);
    }

}
