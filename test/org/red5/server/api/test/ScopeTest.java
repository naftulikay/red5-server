package org.red5.server.api.test;

import static junit.framework.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.red5.server.api.IClient;
import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.ScopeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ScopeTest {

	protected static Log log =
        LogFactory.getLog(ScopeTest.class.getName());
	
	static final String config = "test/org/red5/server/api/test/context.xml";
	static final String host = "localhost";
	static final String path_app = "test";
	static final String path_room = "test/room";
	
	static ApplicationContext spring = null;
	static IContext context = null;

	@BeforeClass public static void setup(){
		spring = new FileSystemXmlApplicationContext(config);
		context = (IContext) spring.getBean("red5.context");
	}
	
	@Test public void scopeResolver(){
		
		// Root 
		IScope root = context.resolveScope(null,null);
		assertTrue("global scope not null", root != null);
		assertTrue("should be global", ScopeUtils.isRoot(root));
		log.debug(root);
		
		// Default Host
		IScope defaultHost = context.resolveScope("",null);
		assertTrue("defaultHost scope not null", defaultHost != null);
		assertTrue("should be host", ScopeUtils.isHost(defaultHost));
		log.debug(defaultHost);
		
		
		// Local Host
		IScope localHost = context.resolveScope(host,null);
		assertTrue("localHost scope not null", localHost != null);
		log.debug(localHost);
		
		// Test App
		IScope testApp = context.resolveScope(host,path_app);
		assertTrue("testApp scope not null", testApp != null);
		log.debug(testApp);
		
		// Test Room
		IScope testRoom = context.resolveScope(host,path_room);
		log.debug(testRoom);

		// Test App Not Found
		try {
			IScope notFoundApp = context.resolveScope(host,path_app+"notfound");
			log.debug(notFoundApp);
			assertTrue("should have thrown an exception", false);
		} catch (RuntimeException e) {
		}
		
	}
	
	@Test public void context(){
		IScope testRoom = context.resolveScope(host,path_room);
		IContext context = testRoom.getContext();
		assertTrue("context should not be null",context!=null);
		log.debug(testRoom.getContext().getResource(""));
		log.debug(testRoom.getResource(""));
		log.debug(testRoom.getParent().getResource(""));
	}
	
	@Test public void client(){
		IClientRegistry reg = context.getClientRegistry();
		IClient client = reg.newClient();
		assertTrue("client should not be null", client!=null);
	}
	
	public static junit.framework.Test suite(){
		return new JUnit4TestAdapter(ScopeTest.class);
	}
	
}