httpclient-jmx
==============

A set of self (un)registerable MBeans to manage instances of ClientConnectionManager and HttpClient.


#### Dependencies
- httpclient 4.3, use new [PoolingHttpClientConnectionManager][1] instead of deprecated [PoolingClientConnectionManager][2]
- httpclient 4.2, use new [PoolingClientConnectionManager][1] instead of deprecated [ThreadSafeClientConnManager][2]
- slf4j-api


#### Features
- A global settings Mbean.
- Each HttpClientConnectionManager instance(i.e. PoolingHttpClientConnectionManager at the moment) created with ClientConnMgrFactory has a settings MBean for it.
- Each HttpClient instance created with HttpClientFactory has a settings Mbean for it.
- Once an instance is collected by Java GC, its settings MBean will be be self unregistered automatically.


#### Examples
    // create a connMgr, and register a setting Mbean for it
    HttpClientConnectionManager mgr = ClientConnMgrFactory.newInstance();

    // reuse connMgr created above
    mgr2 = ClientConnMgrFactory.newInstance(true);
    assertSame(mgr, mg2);

    // create a httpClient by default with an EXISTING connMgr, and register a settings Mbean for it
    HttpClient clt = HttpClientFactory.newInstance();
    assertSame(mgr, clt.getConnectionManager());

    // create a httpClient with a NEW connMgr that is implicitly created
    HttpClient clt2 = HttpClientFactory.newInstance(false);
    assertNotSame(mgr, clt2.getConnectionManager());

    // create a connMgr explicitly, maybe it comes from a legacy system
    HttpClientConnectionManager pmgr = new PoolingHttpClientConnectionManager();
    // create httpClient using a specified connMgr, 
    // if the connMgr is an instance of PoolingHttpClientConnectionManager, register a Mbean for it. 
    HttpClient clt3 = HttpClientFactory.newInstance(pmgr);


  [1]: http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/PoolingClientConnectionManager.html
  [2]: http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/tsccm/ThreadSafeClientConnManager.html
