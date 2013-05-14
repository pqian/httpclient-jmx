package pqian.http;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
     * Creates a new {@link HttpClient} using an existing {@link ClientConnectionManager} if possible, otherwise using a new created implicitly.
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
     *            use new {@link ClientConnectionManager} to create {@link HttpClient} if true
     * @param mbeanName
     * @return
     */
    public static HttpClient newInstance(final boolean reuseExistingConnMgrIfPossible, final String mbeanName)
    {
        final ClientConnectionManager connMgr = ClientConnMgrFactory.newInstance(reuseExistingConnMgrIfPossible);
        return createNewInstance(connMgr, mbeanName);
    }

    /**
     * Creates a new {@link HttpClient} using the specified {@link ClientConnectionManager}
     * 
     * @param connMgr
     * @return
     */
    public static HttpClient newInstance(final ClientConnectionManager connMgr)
    {
        return newInstance(connMgr, null);
    }

    /**
     * Creates a new {@link HttpClient} using the specified {@link ClientConnectionManager}, and register a {@link HttpClientSettings} MBean with the given name
     * for this client.
     * 
     * @param connMgr
     * @param mbeanName
     * @return
     */
    public static HttpClient newInstance(final ClientConnectionManager connMgr, final String mbeanName)
    {
        if (!MBeanRegistrar.isMapped(connMgr))
        {
            if (connMgr instanceof PoolingClientConnectionManager)
            {
                MBeanRegistrar.registerClientConnMgrSettings((PoolingClientConnectionManager) connMgr);
            }
            else
            {
                LOG.warn("ClientConnectionManager {} cannot be monitered via JMX, only PoolingClientConnectionManager is possible", connMgr);
            }
        }
        return createNewInstance(connMgr, mbeanName);
    }

    private static HttpClient createNewInstance(final ClientConnectionManager connMgr, final String mbeanName)
    {
        final HttpClient client = new DefaultHttpClient(connMgr);
        final HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HttpSettings.INSTANCE.getDefaultConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, HttpSettings.INSTANCE.getDefaultSocketTimeout());
        final String objectName = MBeanRegistrar.registerHttpClientSettings(client, mbeanName);
        LOG.info("HttpClient {} is being monitered by Mbean {}", connMgr, objectName);
        return client;
    }

}
