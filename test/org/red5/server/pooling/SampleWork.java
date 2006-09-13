/*
 * Copyright 2004-2005 the original author.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.red5.server.pooling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Murali Kosaraju
 * Class which contains the method <code>executeTask()</code> 
 * which is ivoked by the <code>PoolWorker</code> thread. 
 */
public class SampleWork {
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(SampleWork.class);

   public String executeTask(String strArg, int intArg) {
      log.debug(" Begin executeTask(String, int)  ");
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         log.debug("InterruptedException - ", e);
      }
      log.debug(" End executeTask(String, int) ");
      return strArg + " : " + intArg;
   }

   public String executeTask() {
      log.debug(" Begin executeTask() ");
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         log.debug("InterruptedException - ", e);
      }
      log.debug(" End executeTask ");
      return Thread.currentThread().getName();
   }

}