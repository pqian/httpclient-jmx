package com.github.pqian.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.pqian.http.HttpClientConnectionManagerFactory;
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
        final HttpClientConnectionManager mgr = HttpClientConnectionManagerFactory.newInstance();
        assertTrue(mgr instanceof PoolingHttpClientConnectionManager);
        final PoolingHttpClientConnectionManager pmgr = (PoolingHttpClientConnectionManager) mgr;
        assertEquals(10, pmgr.getDefaultMaxPerRoute());
        assertEquals(100, pmgr.getMaxTotal());

        // reuse connMgr created above
        assertSame(mgr, HttpClientConnectionManagerFactory.newInstance(true));

        // create a httpClient with a NEW connMgr that is implicitly created
        Assert.assertNotSame(mgr, HttpClientFactory.newInstance(false).getConnectionManager());

        // change HTTP global settings
        HttpSettings.INSTANCE.setDefaultConnectionTimeout(1001);
        HttpSettings.INSTANCE.setDefaultSocketTimeout(5001);
        HttpSettings.INSTANCE.setDefaultMaxConnectionsPerRoute(11);
        HttpSettings.INSTANCE.setDefaultMaxTotalConnections(101);

        // create a NEW connMgr explicitly
        final PoolingHttpClientConnectionManager pmgr2 = (PoolingHttpClientConnectionManager) HttpClientConnectionManagerFactory.newInstance();
        assertNotSame(pmgr, pmgr2);
        // settings is fresh data
        assertEquals(11, pmgr2.getDefaultMaxPerRoute());
        assertEquals(101, pmgr2.getMaxTotal());

    }

    @Test
    public void testMBeansAutoUnregister() throws InterruptedException
    {
        PoolingHttpClientConnectionManager mgr = new PoolingHttpClientConnectionManager();
        final String mgrObjectName = MBeanRegistrar.registerClientConnMgrSettings(mgr);
        assertTrue(MBeanRegistrar.isRegistered(mgrObjectName));
        
        HttpClient clt = HttpClientBuilder.create().setConnectionManager(mgr).build();
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
