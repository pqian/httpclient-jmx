package com.github.pqian.http;

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
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanRegistrar
{
    private static final Logger LOG = LoggerFactory.getLogger(MBeanRegistrar.class);

    private static final MBeanServer MBEAN_SERVER = findMBeanServer(null);

    private static final WeakHashMap<HttpClientConnectionManager, String> CONN_MGR_MAP = new WeakHashMap<HttpClientConnectionManager, String>();

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
    public static String registerClientConnMgrSettings(final PoolingHttpClientConnectionManager connMgr)
    {
        return registerClientConnMgrSettings(connMgr, null);
    }

    /**
     * Registers a named MBean to manage the specified {@link ClientConnectionManager}.
     * 
     * @param connMgr
     * @param mbeanName
     * @return
     */
    public static String registerClientConnMgrSettings(final PoolingHttpClientConnectionManager connMgr, final String mbeanName)
    {
        final String objectName = createObjectNameForClientConnMgrSettings(mbeanName);
        final HttpClientConnectionManagerSettings connMgrSettings = new HttpClientConnectionManagerSettings(connMgr, objectName);
        registerMBean(connMgrSettings, objectName);
        CONN_MGR_MAP.put(connMgr, objectName);
        return objectName;
    }
    
    /**
     * Creates object name for instance of {@link HttpClientConnectionManagerSettings}
     * @param mbeanName
     * @return
     */
    public static String createObjectNameForClientConnMgrSettings(String mbeanName) {
      return "pqian.http:type=ClientConnMgrSettings,name=" + createMbeanName(mbeanName);
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
     * @param mbeanName
     * @return
     */
    public static String registerHttpClientSettings(final HttpClient client, final String mbeanName)
    {
        final String objectName = createObjectNameForHttpClientSettings(mbeanName);
        final HttpClientSettings clientSettings = new HttpClientSettings(client, objectName);
        registerMBean(clientSettings, objectName);
        return objectName;
    }

    /**
     * Creates object name for instance of {@link HttpClientSettings}
     * @param mbeanName
     * @return
     */
    public static String createObjectNameForHttpClientSettings(String mbeanName) {
      return "pqian.http:type=HttpClientSettings,name=" + createMbeanName(mbeanName);
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
     * Returns bound {@link ClientConnectionManager} by the given mbeanName.
     * 
     * @param mbeanName
     * @return
     */
    public static HttpClientConnectionManager findClientConnMgrByMbeanName(final String mbeanName)
    {
        if (CONN_MGR_MAP.isEmpty()) { return null; }
        if (mbeanName == null) { return CONN_MGR_MAP.entrySet().iterator().next().getKey(); }
        String objectName = createObjectNameForClientConnMgrSettings(mbeanName);
        for (final Entry<HttpClientConnectionManager, String> entry : CONN_MGR_MAP.entrySet())
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
    public static boolean isMapped(final HttpClientConnectionManager connMgr)
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

    private static String createMbeanName(final String mbeanName)
    {
        if (mbeanName == null || mbeanName.isEmpty()) { return Thread.currentThread().getId() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()); }
        return mbeanName;
    }

}
