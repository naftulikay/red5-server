/**
 * 
 */
package org.red5.server.adapter;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.red5.server.WebScope;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;

import com.sun.xml.internal.bind.v2.model.core.Adapter;

/**
 * Unit test code for MultiThreadedApplicationAdapter
 * 
 * @author dominickaccattato
 *
 */
public class MultiThreadedApplicationAdapterTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#start(org.red5.server.api.IScope)}.
	 */
//	@Test
//	public void testStart() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#stop(org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testStop() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#connect(org.red5.server.api.IConnection, org.red5.server.api.IScope, java.lang.Object[])}.
//	 */
//	@Test
//	public void testConnect() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#disconnect(org.red5.server.api.IConnection, org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testDisconnect() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#join(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testJoin() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#leave(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testLeave() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#addListener(org.red5.server.adapter.IApplication)}.
//	 */
//	@Test
//	public void testAddListener() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#removeListener(org.red5.server.adapter.IApplication)}.
//	 */
//	@Test
//	public void testRemoveListener() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getListeners()}.
//	 */
//	@Test
//	public void testGetListeners() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#registerStreamPublishSecurity(org.red5.server.api.stream.IStreamPublishSecurity)}.
//	 */
//	@Test
//	public void testRegisterStreamPublishSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#unregisterStreamPublishSecurity(org.red5.server.api.stream.IStreamPublishSecurity)}.
//	 */
//	@Test
//	public void testUnregisterStreamPublishSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getStreamPublishSecurity()}.
//	 */
//	@Test
//	public void testGetStreamPublishSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#registerStreamPlaybackSecurity(org.red5.server.api.stream.IStreamPlaybackSecurity)}.
//	 */
//	@Test
//	public void testRegisterStreamPlaybackSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#unregisterStreamPlaybackSecurity(org.red5.server.api.stream.IStreamPlaybackSecurity)}.
//	 */
//	@Test
//	public void testUnregisterStreamPlaybackSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getStreamPlaybackSecurity()}.
//	 */
//	@Test
//	public void testGetStreamPlaybackSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#registerSharedObjectSecurity(org.red5.server.api.so.ISharedObjectSecurity)}.
//	 */
//	@Test
//	public void testRegisterSharedObjectSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#unregisterSharedObjectSecurity(org.red5.server.api.so.ISharedObjectSecurity)}.
//	 */
//	@Test
//	public void testUnregisterSharedObjectSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getSharedObjectSecurity()}.
//	 */
//	@Test
//	public void testGetSharedObjectSecurity() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#rejectClient()}.
//	 */
//	@Test
//	public void testRejectClient() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#rejectClient(java.lang.Object)}.
//	 */
//	@Test
//	public void testRejectClientObject() {
//		fail("Not yet implemented");
//	}
//
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appStart(org.red5.server.api.IScope)}.
	 */
	@Test
	public void testAppStartTrue() {
		// create ApplicationLifeCycle and overide appConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean appStart(IScope app) {
				return true;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.appStart(null);
			
		assertTrue(actual);
	}
	
	@Test
	public void testAppStartFalse() {
		// create ApplicationLifeCycle and overide appConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean appStart(IScope app) {
				return false;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.appStart(null);
			
		assertFalse(actual);
	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appStop(org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testAppStop() {
//		fail("Not yet implemented");
//	}
//
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomStart(org.red5.server.api.IScope)}.
	 */
	@Test
	public void testRoomStartTrue() {
		// create ApplicationLifeCycle and overide appConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean roomStart(IScope room) {
				return true;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.roomStart(null);
			
		assertTrue(actual);
	}
	
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomStart(org.red5.server.api.IScope)}.
	 */
	@Test
	public void testRoomStartFalse() {
		// create ApplicationLifeCycle and overide appConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean roomStart(IScope room) {
				return false;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.roomStart(null);
			
		assertFalse(actual);
	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomStop(org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testRoomStop() {
//		fail("Not yet implemented");
//	}
//
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appConnect(org.red5.server.api.IConnection, java.lang.Object[])}.
	 */
	@Test
	public void testAppConnectTrue() {
		// create ApplicationLifeCycle and overide appConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean appConnect(IConnection conn, Object[] params) {
				return true;
			}
		};	
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.appConnect(null, null);
			
		assertTrue(actual);
	}
	
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appConnect(org.red5.server.api.IConnection, java.lang.Object[])}.
	 */
	@Test
	public void testAppConnectFalse() {
		// create ApplicationLifeCycle and overide appConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean appConnect(IConnection conn, Object[] params) {
				return false;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.appConnect(null, null);
			
		assertFalse(actual);
	}

	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomConnect(org.red5.server.api.IConnection, java.lang.Object[])}.
	 */
	@Test
	public void testRoomConnectTrue() {
		// create ApplicationLifeCycle and overide roomConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean roomConnect(IConnection conn, Object[] params) {
				return true;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.roomConnect(null, null);
			
		assertTrue(actual);
	}
	
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomConnect(org.red5.server.api.IConnection, java.lang.Object[])}.
	 */
	@Test
	public void testRoomConnectFalse() {
		// create ApplicationLifeCycle and overide roomConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean roomConnect(IConnection conn, Object[] params) {
				return false;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.roomConnect(null, null);
			
		assertFalse(actual);
	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appDisconnect(org.red5.server.api.IConnection)}.
//	 */
//	@Test
//	public void testAppDisconnect() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomDisconnect(org.red5.server.api.IConnection)}.
//	 */
//	@Test
//	public void testRoomDisconnect() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appJoin(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
	 */
	@Test
	public void testAppJoinTrue() {
		// create ApplicationLifeCycle and overide roomConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean appJoin(IClient client, IScope app) {
				return true;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.appJoin(null, null);
			
		assertTrue(actual);
	}
	
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appJoin(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
	 */
	@Test
	public void testAppJoinFalse() {
		// create ApplicationLifeCycle and overide roomConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean appJoin(IClient client, IScope app) {
				return false;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.appJoin(null, null);
			
		assertFalse(actual);
	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#appLeave(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testAppLeave() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomJoin(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
	 */
	@Test
	public void testRoomJoin() {
		// create ApplicationLifeCycle and overide roomConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean roomJoin(IClient client, IScope room) {
				return true;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.roomJoin(null, null);
			
		assertTrue(actual);
	}
	
	/**
	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomJoin(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
	 */
	@Test
	public void testRoomFalse() {
		// create ApplicationLifeCycle and overide roomConnect
		ApplicationLifecycle app = new ApplicationLifecycle() {
			public boolean roomJoin(IClient client, IScope room) {
				return false;
			}
		};
		
		// create MultiThreadedApplicationAdapter which loops through
		// the listeners and returns their result
		MultiThreadedApplicationAdapter adapter = new MultiThreadedApplicationAdapter();
		adapter.addListener((IApplication) app);
		boolean actual = adapter.roomJoin(null, null);
			
		assertFalse(actual);
	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#roomLeave(org.red5.server.api.IClient, org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testRoomLeave() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#measureBandwidth()}.
//	 */
//	@Test
//	public void testMeasureBandwidth() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#measureBandwidth(org.red5.server.api.IConnection)}.
//	 */
//	@Test
//	public void testMeasureBandwidthIConnection() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#createSharedObject(org.red5.server.api.IScope, java.lang.String, boolean)}.
//	 */
//	@Test
//	public void testCreateSharedObject() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getSharedObject(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testGetSharedObjectIScopeString() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getSharedObject(org.red5.server.api.IScope, java.lang.String, boolean)}.
//	 */
//	@Test
//	public void testGetSharedObjectIScopeStringBoolean() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getSharedObjectNames(org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testGetSharedObjectNames() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#hasSharedObject(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testHasSharedObject() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#hasBroadcastStream(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testHasBroadcastStream() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getBroadcastStream(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testGetBroadcastStream() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getBroadcastStreamNames(org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testGetBroadcastStreamNames() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#hasOnDemandStream(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testHasOnDemandStream() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getOnDemandStream(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testGetOnDemandStream() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getSubscriberStream(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testGetSubscriberStream() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#addScheduledJob(int, org.red5.server.api.scheduling.IScheduledJob)}.
//	 */
//	@Test
//	public void testAddScheduledJob() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#addScheduledOnceJob(long, org.red5.server.api.scheduling.IScheduledJob)}.
//	 */
//	@Test
//	public void testAddScheduledOnceJobLongIScheduledJob() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#addScheduledOnceJob(java.util.Date, org.red5.server.api.scheduling.IScheduledJob)}.
//	 */
//	@Test
//	public void testAddScheduledOnceJobDateIScheduledJob() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#addScheduledJobAfterDelay(int, org.red5.server.api.scheduling.IScheduledJob, int)}.
//	 */
//	@Test
//	public void testAddScheduledJobAfterDelay() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#pauseScheduledJob(java.lang.String)}.
//	 */
//	@Test
//	public void testPauseScheduledJob() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#resumeScheduledJob(java.lang.String)}.
//	 */
//	@Test
//	public void testResumeScheduledJob() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#removeScheduledJob(java.lang.String)}.
//	 */
//	@Test
//	public void testRemoveScheduledJob() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getScheduledJobNames()}.
//	 */
//	@Test
//	public void testGetScheduledJobNames() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getStreamLength(java.lang.String)}.
//	 */
//	@Test
//	public void testGetStreamLength() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#clearSharedObjects(org.red5.server.api.IScope, java.lang.String)}.
//	 */
//	@Test
//	public void testClearSharedObjects() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getClientTTL()}.
//	 */
//	@Test
//	public void testGetClientTTL() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#setClientTTL(int)}.
//	 */
//	@Test
//	public void testSetClientTTL() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#getGhostConnsCleanupPeriod()}.
//	 */
//	@Test
//	public void testGetGhostConnsCleanupPeriod() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#setGhostConnsCleanupPeriod(int)}.
//	 */
//	@Test
//	public void testSetGhostConnsCleanupPeriod() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#scheduleGhostConnectionsCleanup()}.
//	 */
//	@Test
//	public void testScheduleGhostConnectionsCleanup() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#cancelGhostConnectionsCleanup()}.
//	 */
//	@Test
//	public void testCancelGhostConnectionsCleanup() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#killGhostConnections()}.
//	 */
//	@Test
//	public void testKillGhostConnections() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#FCPublish(java.lang.String)}.
//	 */
//	@Test
//	public void testFCPublish() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#FCUnpublish()}.
//	 */
//	@Test
//	public void testFCUnpublish() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamBroadcastClose(org.red5.server.api.stream.IBroadcastStream)}.
//	 */
//	@Test
//	public void testStreamBroadcastClose() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamBroadcastStart(org.red5.server.api.stream.IBroadcastStream)}.
//	 */
//	@Test
//	public void testStreamBroadcastStart() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamPlaylistItemPlay(org.red5.server.api.stream.IPlaylistSubscriberStream, org.red5.server.api.stream.IPlayItem, boolean)}.
//	 */
//	@Test
//	public void testStreamPlaylistItemPlay() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamPlaylistItemStop(org.red5.server.api.stream.IPlaylistSubscriberStream, org.red5.server.api.stream.IPlayItem)}.
//	 */
//	@Test
//	public void testStreamPlaylistItemStop() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamPlaylistVODItemPause(org.red5.server.api.stream.IPlaylistSubscriberStream, org.red5.server.api.stream.IPlayItem, int)}.
//	 */
//	@Test
//	public void testStreamPlaylistVODItemPause() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamPlaylistVODItemResume(org.red5.server.api.stream.IPlaylistSubscriberStream, org.red5.server.api.stream.IPlayItem, int)}.
//	 */
//	@Test
//	public void testStreamPlaylistVODItemResume() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamPlaylistVODItemSeek(org.red5.server.api.stream.IPlaylistSubscriberStream, org.red5.server.api.stream.IPlayItem, int)}.
//	 */
//	@Test
//	public void testStreamPlaylistVODItemSeek() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamPublishStart(org.red5.server.api.stream.IBroadcastStream)}.
//	 */
//	@Test
//	public void testStreamPublishStart() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamRecordStart(org.red5.server.api.stream.IBroadcastStream)}.
//	 */
//	@Test
//	public void testStreamRecordStart() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamSubscriberClose(org.red5.server.api.stream.ISubscriberStream)}.
//	 */
//	@Test
//	public void testStreamSubscriberClose() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.MultiThreadedApplicationAdapter#streamSubscriberStart(org.red5.server.api.stream.ISubscriberStream)}.
//	 */
//	@Test
//	public void testStreamSubscriberStart() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#setScope(org.red5.server.api.IScope)}.
//	 */
//	@Test
//	public void testSetScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getScope()}.
//	 */
//	@Test
//	public void testGetScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getAttribute(java.lang.String)}.
//	 */
//	@Test
//	public void testGetAttributeString() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getAttribute(java.lang.String, java.lang.Object)}.
//	 */
//	@Test
//	public void testGetAttributeStringObject() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getAttributeNames()}.
//	 */
//	@Test
//	public void testGetAttributeNames() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getAttributes()}.
//	 */
//	@Test
//	public void testGetAttributes() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#hasAttribute(java.lang.String)}.
//	 */
//	@Test
//	public void testHasAttribute() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#removeAttribute(java.lang.String)}.
//	 */
//	@Test
//	public void testRemoveAttribute() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#removeAttributes()}.
//	 */
//	@Test
//	public void testRemoveAttributes() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#setAttribute(java.lang.String, java.lang.Object)}.
//	 */
//	@Test
//	public void testSetAttribute() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#setAttributes(org.red5.server.api.IAttributeStore)}.
//	 */
//	@Test
//	public void testSetAttributesIAttributeStore() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#setAttributes(java.util.Map)}.
//	 */
//	@Test
//	public void testSetAttributesMapOfStringObject() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#createChildScope(java.lang.String)}.
//	 */
//	@Test
//	public void testCreateChildScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getChildScope(java.lang.String)}.
//	 */
//	@Test
//	public void testGetChildScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getChildScopeNames()}.
//	 */
//	@Test
//	public void testGetChildScopeNames() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getClients()}.
//	 */
//	@Test
//	public void testGetClients() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getConnections()}.
//	 */
//	@Test
//	public void testGetConnections() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getContext()}.
//	 */
//	@Test
//	public void testGetContext() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getDepth()}.
//	 */
//	@Test
//	public void testGetDepth() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getName()}.
//	 */
//	@Test
//	public void testGetName() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getParent()}.
//	 */
//	@Test
//	public void testGetParent() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getPath()}.
//	 */
//	@Test
//	public void testGetPath() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#hasChildScope(java.lang.String)}.
//	 */
//	@Test
//	public void testHasChildScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#hasParent()}.
//	 */
//	@Test
//	public void testHasParent() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#lookupConnections(org.red5.server.api.IClient)}.
//	 */
//	@Test
//	public void testLookupConnections() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getResources(java.lang.String)}.
//	 */
//	@Test
//	public void testGetResources() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.StatefulScopeWrappingAdapter#getResource(java.lang.String)}.
//	 */
//	@Test
//	public void testGetResource() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#setCanStart(boolean)}.
//	 */
//	@Test
//	public void testSetCanStart() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#setCanCallService(boolean)}.
//	 */
//	@Test
//	public void testSetCanCallService() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#setCanConnect(boolean)}.
//	 */
//	@Test
//	public void testSetCanConnect() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#setJoin(boolean)}.
//	 */
//	@Test
//	public void testSetJoin() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#serviceCall(org.red5.server.api.IConnection, org.red5.server.api.service.IServiceCall)}.
//	 */
//	@Test
//	public void testServiceCall() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#addChildScope(org.red5.server.api.IBasicScope)}.
//	 */
//	@Test
//	public void testAddChildScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#removeChildScope(org.red5.server.api.IBasicScope)}.
//	 */
//	@Test
//	public void testRemoveChildScope() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.red5.server.adapter.AbstractScopeAdapter#handleEvent(org.red5.server.api.event.IEvent)}.
//	 */
//	@Test
//	public void testHandleEvent() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#Object()}.
//	 */
//	@Test
//	public void testObject() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#getClass()}.
//	 */
//	@Test
//	public void testGetClass() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#hashCode()}.
//	 */
//	@Test
//	public void testHashCode() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
//	 */
//	@Test
//	public void testEquals() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#clone()}.
//	 */
//	@Test
//	public void testClone() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#toString()}.
//	 */
//	@Test
//	public void testToString() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#notify()}.
//	 */
//	@Test
//	public void testNotify() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#notifyAll()}.
//	 */
//	@Test
//	public void testNotifyAll() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#wait(long)}.
//	 */
//	@Test
//	public void testWaitLong() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#wait(long, int)}.
//	 */
//	@Test
//	public void testWaitLongInt() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#wait()}.
//	 */
//	@Test
//	public void testWait() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link java.lang.Object#finalize()}.
//	 */
//	@Test
//	public void testFinalize() {
//		fail("Not yet implemented");
//	}

}
