package org.red5.server.jmx;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.log4j.Logger;

import com.sun.jdmk.comm.HtmlAdaptorServer;

public class JMXServer {

	private static Logger log = Logger.getLogger(JMXServer.class);

	private static String domain = "org.red5.server";

	private static String htmlAdapterPort = "8082";

	private static MBeanServer mbs;

	static {
		// create the MBeanServer for our domain
		mbs = MBeanServerFactory.createMBeanServer(domain);
	}

	public void init() {
		// setup jmx
		try {
			//instance an html adaptor
			int port = htmlAdapterPort == null ? 9080 : Integer
					.valueOf(htmlAdapterPort);
			HtmlAdaptorServer html = new HtmlAdaptorServer(port);
			ObjectName htmlName = new ObjectName(domain
					+ ":type=HtmlAdaptorServer,port=" + port);
			log.debug("Created HTML adaptor on port: " + port);
			//add the adaptor to the server
			mbs.registerMBean(html, htmlName);
			//start the adaptor
			html.start();

			log.debug("JMX default domain: " + mbs.getDefaultDomain());

		} catch (Exception e) {
			log.error("Error in setup of JMX subsystem", e);
		}
	}

	public static ObjectName createSimpleMBean(String className,
			String objectNameStr) {
		log.info("Create the " + className + " MBean within the MBeanServer");
		log.info("ObjectName = " + objectNameStr);
		try {
			ObjectName objectName = ObjectName.getInstance(objectNameStr);
			mbs.createMBean(className, objectName);
			return objectName;
		} catch (Exception e) {
			log.error("Could not create the " + className + " MBean", e);
		}
		return null;
	}

	public static boolean registerMBean(Object instance, String className,
			Class interfaceClass) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst(
						"[\\.]", "");
			}
			log.debug("Register name: " + cName);
			mbs.registerMBean(new StandardMBean(instance, interfaceClass),
					new ObjectName(domain + ":type=" + cName));
			status = true;
		} catch (Exception e) {
			log.error("Could not register the " + className + " MBean", e);
		}
		return status;
	}

	public static boolean registerMBean(Object instance, String className,
			Class interfaceClass, String name) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst(
						"[\\.]", "");
			}
			log.debug("Register name: " + cName);
			mbs
					.registerMBean(new StandardMBean(instance, interfaceClass),
							new ObjectName(domain + ":type=" + cName + ",name="
									+ name));
			status = true;
		} catch (Exception e) {
			log.error("Could not register the " + className + " MBean", e);
		}
		return status;
	}

	public static boolean registerNewMBean(String className,
			Class interfaceClass) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst(
						"[\\.]", "");
			}
			log.debug("Register name: " + cName);
			mbs.registerMBean(new StandardMBean(Class.forName(className)
					.newInstance(), interfaceClass), new ObjectName(domain
					+ ":type=" + cName));
			status = true;
		} catch (Exception e) {
			log.error("Could not register the " + className + " MBean", e);
		}
		return status;
	}

	public static boolean registerNewMBean(String className,
			Class interfaceClass, String name) {
		boolean status = false;
		try {
			String cName = className;
			if (cName.indexOf('.') != -1) {
				cName = cName.substring(cName.lastIndexOf('.')).replaceFirst(
						"[\\.]", "");
			}
			log.debug("Register name: " + cName);
			mbs.registerMBean(new StandardMBean(Class.forName(className)
					.newInstance(), interfaceClass), new ObjectName(domain
					+ ":type=" + cName + ",name=" + name));
			status = true;
		} catch (Exception e) {
			log.error("Could not register the " + className + " MBean", e);
		}
		return status;
	}

	private static void printMBeanInfo(ObjectName objectName, String className) {
		log.info("Retrieve the management information for the " + className);
		log.info("MBean using the getMBeanInfo() method of the MBeanServer");
		MBeanInfo info = null;
		try {
			info = mbs.getMBeanInfo(objectName);
		} catch (Exception e) {
			log.error("Could not get MBeanInfo object for " + className
					+ " !!!", e);
			return;
		}
		log.info("CLASSNAME: \t" + info.getClassName());
		log.info("DESCRIPTION: \t" + info.getDescription());
		log.info("ATTRIBUTES");
		MBeanAttributeInfo[] attrInfo = info.getAttributes();
		if (attrInfo.length > 0) {
			for (int i = 0; i < attrInfo.length; i++) {
				log.info(" ** NAME: \t" + attrInfo[i].getName());
				log.info("    DESCR: \t" + attrInfo[i].getDescription());
				log.info("    TYPE: \t" + attrInfo[i].getType() + "\tREAD: "
						+ attrInfo[i].isReadable() + "\tWRITE: "
						+ attrInfo[i].isWritable());
			}
		} else
			log.info(" ** No attributes **");
		log.info("CONSTRUCTORS");
		MBeanConstructorInfo[] constrInfo = info.getConstructors();
		for (int i = 0; i < constrInfo.length; i++) {
			log.info(" ** NAME: \t" + constrInfo[i].getName());
			log.info("    DESCR: \t" + constrInfo[i].getDescription());
			log.info("    PARAM: \t" + constrInfo[i].getSignature().length
					+ " parameter(s)");
		}
		log.info("OPERATIONS");
		MBeanOperationInfo[] opInfo = info.getOperations();
		if (opInfo.length > 0) {
			for (int i = 0; i < opInfo.length; i++) {
				log.info(" ** NAME: \t" + opInfo[i].getName());
				log.info("    DESCR: \t" + opInfo[i].getDescription());
				log.info("    PARAM: \t" + opInfo[i].getSignature().length
						+ " parameter(s)");
			}
		} else
			log.info(" ** No operations ** ");
		log.info("NOTIFICATIONS");
		MBeanNotificationInfo[] notifInfo = info.getNotifications();
		if (notifInfo.length > 0) {
			for (int i = 0; i < notifInfo.length; i++) {
				log.info(" ** NAME: \t" + notifInfo[i].getName());
				log.info("    DESCR: \t" + notifInfo[i].getDescription());
				String notifTypes[] = notifInfo[i].getNotifTypes();
				for (int j = 0; j < notifTypes.length; j++) {
					log.info("    TYPE: \t" + notifTypes[j]);
				}
			}
		} else
			log.info(" ** No notifications **");
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		JMXServer.domain = domain;
	}

	public String getHtmlAdapterPort() {
		return htmlAdapterPort;
	}

	public void setHtmlAdapterPort(String htmlAdapterPort) {
		JMXServer.htmlAdapterPort = htmlAdapterPort;
	}

	public static MBeanServer getMBeanServer() {
		return mbs;
	}

}