package org.red5.demos.oflaDemo;

import java.util.HashMap;
import java.util.Map;

public class DemoServiceImpl implements IDemoService {

	/**
	 * Getter for property 'listOfAvailableFLVs'.
	 * 
	 * @return Value for property 'listOfAvailableFLVs'.
	 */
	public Map<String, Map<String, Object>> getListOfAvailableFLVs() {
		System.out.println("getListOfAvailableFLVs empty");
		return new HashMap<String, Map<String, Object>>();
	}

	public Map<String, Map<String, Object>> getListOfAvailableFLVs(String string) {
		System.out.println("getListOfAvailableFLVs, Got a string: " + string);
		return getListOfAvailableFLVs();
	}

}
