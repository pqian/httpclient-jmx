package com.github.pqian.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.pqian.http.ClientConnMgrFactory;
import com.github.pqian.http.HttpClientFactory;
import com.github.pqian.http.HttpSettings;
import com.github.pqian.http.MBeanRegistrar;

public class MBeanRegistrarTest
{

    @Before
    public void setUp() throws Exception
    {
        HttpSettings.INSTANCE.setDefaultConnectionTimeout(1000);
        HttpSettings.INSTANCE.setDefaultSocketTimeout(5000);
        HttpSettings.INSTANCE.setDefaultMaxConnectionsPerRoute(10);
        HttpSettings.INSTANCE.setDefaultMaxTotalConnections(100);
    }

    @Test
    public void testUsage()
    {
        // register httpSettings, but not necessary here, just for jconsole checking
        MBeanRegistrar.registerHttpSettings();

        // create a connMgr
        final ClientConnectionManager mgr = ClientConnMgrFactory.newInstance();
        assertTrue(mgr instanceof PoolingClientConnectionManager);
        final PoolingClientConnectionManager pmgr = (PoolingClientConnectionManager) mgr;
        assertEquals(10, pmgr.getDefaultMaxPerRoute());
        assertEquals(100, pmgr.getMaxTotal());

        // reuse connMgr created above
        assertSame(mgr, ClientConnMgrFactory.newInstance(true));

        // create a httpClient by default with an EXISTING connMgr
        final HttpClient clt = HttpClientFactory.newInstance();
        assertSame(mgr, clt.getConnectionManager());
        assertEquals(1000, HttpConnectionParams.getConnectionTimeout(clt.getParams()));
        assertEquals(5000, HttpConnectionParams.getSoTimeout(clt.getParams()));

        // create a httpClient with a NEW connMgr that is implicitly created
        Assert.assertNotSame(mgr, HttpClientFactory.newInstance(false).getConnectionManager());

        // change HTTP global settings
        HttpSettings.INSTANCE.setDefaultConnectionTimeout(1001);
        HttpSettings.INSTANCE.setDefaultSocketTimeout(5001);
        HttpSettings.INSTANCE.setDefaultMaxConnectionsPerRoute(11);
        HttpSettings.INSTANCE.setDefaultMaxTotalConnections(101);

        // connMgr and httpClient created before are not affected with the changes above
        assertEquals(10, pmgr.getDefaultMaxPerRoute());
        assertEquals(100, pmgr.getMaxTotal());
        assertEquals(1000, HttpConnectionParams.getConnectionTimeout(clt.getParams()));
        assertEquals(5000, HttpConnectionParams.getSoTimeout(clt.getParams()));

        // create a NEW connMgr explicitly
        final PoolingClientConnectionManager pmgr2 = (PoolingClientConnectionManager) ClientConnMgrFactory.newInstance();
        assertNotSame(pmgr, pmgr2);
        // settings is fresh data
        assertEquals(11, pmgr2.getDefaultMaxPerRoute());
        assertEquals(101, pmgr2.getMaxTotal());

        // create httpClient using a specified connMgr
        final HttpClient clt2 = HttpClientFactory.newInstance(pmgr2);
        assertSame(pmgr2, clt2.getConnectionManager());
        // settings is fresh data
        assertEquals(1001, HttpConnectionParams.getConnectionTimeout(clt2.getParams()));
        assertEquals(5001, HttpConnectionParams.getSoTimeout(clt2.getParams()));

    }

    @Test
    public void testMBeansAutoUnregister() throws InterruptedException
    {
        PoolingClientConnectionManager mgr = new PoolingClientConnectionManager();
        final String mgrObjectName = MBeanRegistrar.registerClientConnMgrSettings(mgr);
        assertTrue(MBeanRegistrar.isRegistered(mgrObjectName));

        HttpClient clt = new DefaultHttpClient(mgr);
        final String cltObjectName = MBeanRegistrar.registerHttpClientSettings(clt);
        assertTrue(MBeanRegistrar.isRegistered(cltObjectName));

        mgr = null;
        clt = null;
        System.gc();
        // s must >= Timer's period, see HttpClientSettings or ClientConnMgrSettings
        int s = 30;
        while (--s >= 0)
        {
            System.out.println(s);
            Thread.sleep(1000);
        }
        assertFalse(MBeanRegistrar.isRegistered(mgrObjectName));
        assertFalse(MBeanRegistrar.isRegistered(cltObjectName));
    }
}
