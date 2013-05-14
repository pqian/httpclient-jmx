package pqian.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanRegistrar
{
    private static final Logger LOG = LoggerFactory.getLogger(MBeanRegistrar.class);

    private static final MBeanServer MBEAN_SERVER = findMBeanServer(null);

    private static final WeakHashMap<ClientConnectionManager, String> CONN_MGR_MAP = new WeakHashMap<ClientConnectionManager, String>();

    private MBeanRegistrar()
    {}

    /**
     * Registers an unique MBean to manage global HTTP client settings.
     * 
     * @return
     */
    public static String registerHttpSettings()
    {
        final String objectName = "pqian.http:name=HttpSettings";
        registerMBean(new HttpSettings(), objectName);
        LOG.info("Http global settings is usable via MBean {}", objectName);
        return objectName;
    }

    /**
     * Registers a MBean to manage the specified {@link ClientConnectionManager}.
     * 
     * @param connMgr
     * @return
     */
    public static String registerClientConnMgrSettings(final PoolingClientConnectionManager connMgr)
    {
        return registerClientConnMgrSettings(connMgr, null);
    }

    /**
     * Registers a named MBean to manage the specified {@link ClientConnectionManager}.
     * 
     * @param connMgr
     * @param name
     * @return
     */
    public static String registerClientConnMgrSettings(final PoolingClientConnectionManager connMgr, final String name)
    {
        final String objectName = "pqian.http:type=ClientConnMgrSettings,name=" + createName(name);
        final ClientConnMgrSettings connMgrSettings = new ClientConnMgrSettings(connMgr, objectName);
        registerMBean(connMgrSettings, objectName);
        CONN_MGR_MAP.put(connMgr, objectName);
        return objectName;
    }

    /**
     * Registers a MBean to manage the specified {@link HttpClient}.
     * 
     * @param client
     * @return
     */
    public static String registerHttpClientSettings(final HttpClient client)
    {
        return registerHttpClientSettings(client, null);
    }

    /**
     * Registers a named MBean to manage the specified {@link HttpClient}.
     * 
     * @param client
     * @param name
     * @return
     */
    public static String registerHttpClientSettings(final HttpClient client, final String name)
    {
        final String objectName = "pqian.http:type=HttpClientSettings,name=" + createName(name);
        final HttpClientSettings clientSettings = new HttpClientSettings(client, objectName);
        registerMBean(clientSettings, objectName);
        return objectName;
    }

    /**
     * Base register method.
     * 
     * @param mbean
     * @param objectName
     */
    public static void registerMBean(final Object mbean, final String objectName)
    {
        try
        {
            MBEAN_SERVER.registerMBean(mbean, new ObjectName(objectName));
            LOG.debug("MBean registered, {}", objectName);
        }
        catch (final InstanceAlreadyExistsException e)
        {
            LOG.warn("MBean has been registered before, {}", objectName);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Registering MBean failed, " + objectName, e);
        }
    }

    /**
     * Base unregister method.
     * 
     * @param objectName
     */
    public static void unregisterMBean(final String objectName)
    {
        try
        {
            MBEAN_SERVER.unregisterMBean(new ObjectName(objectName));
            LOG.debug("MBean unregistered, {}", objectName);
        }
        catch (final InstanceNotFoundException e)
        {
            LOG.warn("MBean not found to unregister, {}", objectName);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Unregistering MBean failed, " + objectName, e);
        }
    }

    /**
     * Checks whether objectName is registered with.
     * 
     * @param objectName
     * @return
     */
    public static boolean isRegistered(final String objectName)
    {
        try
        {
            return MBEAN_SERVER.isRegistered(new ObjectName(objectName));
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Checking registered objectName failed", e);
        }
    }

    /**
     * Returns bound {@link ClientConnectionManager} by the given objectName.
     * 
     * @param objectName
     * @return
     */
    public static ClientConnectionManager findClientConnMgrByObjectName(final String objectName)
    {
        if (CONN_MGR_MAP.isEmpty()) { return null; }
        if (objectName == null) { return CONN_MGR_MAP.entrySet().iterator().next().getKey(); }
        for (final Entry<ClientConnectionManager, String> entry : CONN_MGR_MAP.entrySet())
        {
            if (objectName.equals(entry.getValue())) { return entry.getKey(); }
        }
        return null;
    }

    /**
     * Checks whether the specified {@link ClientConnectionManager} is Bound.
     * 
     * @param connMgr
     * @return
     */
    public static boolean isMapped(final ClientConnectionManager connMgr)
    {
        if (CONN_MGR_MAP.isEmpty()) { return false; }
        return CONN_MGR_MAP.containsKey(connMgr);
    }

    private static MBeanServer findMBeanServer(final String agentId)
    {
        MBeanServer found = null;
        final List<MBeanServer> serverList = MBeanServerFactory.findMBeanServer(agentId);
        for (final MBeanServer server : serverList)
        {
            if (server != null)
            {
                found = server;
                LOG.warn("MBean server found by agentId {}", agentId);
                break;
            }
        }
        if (found == null)
        {
            if (agentId != null)
            {
                LOG.warn("MBean server not found by agentId {}", agentId);
            }
            found = MBeanServerFactory.createMBeanServer();
            LOG.info("A new MBean server created, {}", found);
        }
        return found;
    }

    private static String createName(final String name)
    {
        if (name == null || name.isEmpty()) { return Thread.currentThread().getId() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()); }
        return name;
    }

}
