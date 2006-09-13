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
 * Tester class for testing the ThreadPool
 */
public class PoolTester {
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PoolTester.class);

   private ThreadPool pool = null;

   /**
    * @return Returns the pool.
    */
   public ThreadPool getPool() {
      return pool;
   }

   /**
    * @param pool - The pool to set.
    */
   public void setPool(ThreadPool tpool) {
      this.pool = tpool;
   }

   public PoolTester() {
      if (pool == null) {
         pool = new ThreadPool(new ThreadObjectFactory());
      }
   }

   /**
    * shutdown 
    */
   public void shutdown() {
      try {
         pool.close();
      } catch (Exception e) {
         log.error("", e);
      }
   }

   /**
    * runWithNotify - method simulates the use of wait() and notify().
    */
   public void runWithNotify() {
      try {
         log.debug("**** Running run().. ");
         WorkerThread rt1 = (WorkerThread) pool.borrowObject();

         Object synObj = new Object();
         rt1.execute("com.findonnet.services.pooling.test.SampleWork",
               "executeTask", null, null, synObj);
         synchronized (synObj) {
            synObj.wait(4000);
         }
         Object result  = rt1.getResult();
         log.debug("**Result = " + result);

         pool.returnObject(rt1);
      } catch (Exception e) {
         log.error("", e);
      }
   }
   
   /**
    * runAsyncTask - method simulates the executing a task without waiting for
    * the results. Please note that  we need to set the pool so that the thread goes
    * back to the pool, upon completion of the task.
    */
   public void runAsyncTask() {
      try {
         log.debug("**** Running runAsyncTask().. ");
         WorkerThread rt1 = (WorkerThread) pool.borrowObject();
         // set the pool now so that the WorkerThread knows how to return itself
         //to the pool.
         rt1.setPool(pool); 

         rt1.execute("com.findonnet.services.pooling.test.SampleWork",
               "executeTask", null, null, null);
      } catch (Exception e) {
         log.error("", e);
      }
   }


   //.
   /**
    * runWithoutNotify - This methods does not use the wait() and notify()
    * semantics, but uses a loop to check if the thread had finished executing
    * the task by invoking the <code>isDone()</code> method.
    */
   public void runWithoutNotify() {
      try {
         log.debug("**** Running runWithTwoWorkers().. ");

         WorkerThread[] rtArr = pool.borrowObjects(2);
         WorkerThread rt1 = rtArr[0];
         WorkerThread rt2 = rtArr[1];

         rt1.execute("com.findonnet.services.pooling.test.SampleWork",
               "executeTask", null, null, null);
         rt2.execute("com.findonnet.services.pooling.test.SampleWork",
               "executeTask", null, null, null);

         while (!rt1.isDone() || !rt2.isDone()) {
            Thread.sleep(100);
         }

         pool.returnObject(rt1);
         pool.returnObject(rt2);
         log.debug("*** Finished Thread " + this);
      } catch (Exception e) {
         log.error("", e);
      }
   }

   /**
    * runMultiple - Simulates multiple threads executing the SampleWork task.
    * @param cnt 
    */
   public void runMultiple(int cnt) {
      try {
         log.debug("**** Running runMultiple().. " + cnt);
         PoolWorker[] poolArr = new PoolWorker[cnt];
         for (int i = 0; i < cnt; i++) {
            poolArr[i] = new PoolWorker(this.getPool(), i);
            new Thread(poolArr[i]).start();
         }
         log.debug("*** Finished runMultiple ");
      } catch (Exception e) {
         log.error("", e);
      }
   }

   public static void main(String[] args) {
      log.debug("\n\n********* Starting Test ************************");
      PoolTester pt = new PoolTester();

//    pt.runWithNotify(); // test using wait() and notify()
//    pt.runWithoutNotify(); // test using without wait() and notify()
      pt.runMultiple(6); // multiple thread run simulation
//    pt.runAsyncTask(); // run asynchronously without waiting for results.

      try {
         Thread.sleep(8000); // sleep for 8 secs
      } catch (InterruptedException e) {
         log.error("", e);
      }

      log.debug(" \n    <<<< Shutting down pool >>> ");
      pt.shutdown();
      log.debug("\n     <<<< Finished Shutting down pool >>> ");
      log.debug("\n\n********* Finished Test *************************");
   }

}