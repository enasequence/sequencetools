package uk.ac.ebi.embl.flatfile;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class FlatFileUtilsTest {
   private static final Logger LOG = Logger.getLogger(FlatFileUtilsTest.class);
   public static String[] sDates = {
                                    "06-JAN-1971",
                                    "02-JAN-1972",
                                    "06-JAN-1973",
                                    "02-JAN-1974",
                                    "06-JAN-1975",
                                    "02-FEB-1976",
                                    "06-FEB-1977",
                                    "02-FEB-1978",
                                    "06-FEB-1981",
                                    "02-FEB-1982",
                                    "06-MAR-1983",
                                    "12-MAR-1984",
                                    "16-MAR-1985",
                                    "12-MAR-1986",
                                    "16-APR-1991",
                                    "12-APR-1992",
                                    "16-APR-1993",
                                    "12-APR-1994",
                                    "16-MAY-1995",
                                    "12-MAY-1996",
                                    "16-JUN-2011",
                                    "22-JUN-2012",
                                    "26-JUL-2013",
                                    "22-JUL-2014",
                                    "26-AUG-2015",
                                    "22-AUG-2015",
                                    "26-SEP-2016",
                                    "22-SEP-2016",
                                    "26-OCT-2003",
                                    "22-NOV-2004",
                                    "26-DEC-2005",
                                    "22-DEC-2006",
   };

   private static final int numThreads = Runtime.getRuntime().availableProcessors() * 4;
   private static final int numTestsPerThread = 1000;
   
   private Random rand = new Random(System.currentTimeMillis());
   
   @Before
   public void before() {
      rand = new Random(System.currentTimeMillis());
   }
   
   @Test
   public void testGetDay() throws Exception {
      Collection<Runnable> runnables = new ArrayList<>();
      for (int i = 0; i < numThreads; i++) {
         runnables.add(new Runnable() {
            
            @Override
            public void run() {
               for (int j = 0; j < numTestsPerThread; j++) {
                  int idx = rand.nextInt(sDates.length);
                  FlatFileUtils.getDay(sDates[idx]);
               }
            }
         });
      }
      int maxTimeoutSeconds = (int)(numThreads * numTestsPerThread * 0.001); // always close to 1 sec
      assertConcurrentTest("testGetDay", runnables, maxTimeoutSeconds);
   }
   
   @Test
   public void testGetYear() throws Exception {
      Collection<Runnable> runnables = new ArrayList<>();
      for (int i = 0; i < numThreads; i++) {
         runnables.add(new Runnable() {
            
            @Override
            public void run() {
               for (int j = 0; j < numTestsPerThread; j++) {
                  int idx = rand.nextInt(sDates.length);
                  FlatFileUtils.getYear(sDates[idx]);
               }
            }
         });
      }
      int maxTimeoutSeconds = (int)(numThreads * numTestsPerThread * 0.001); // always close to 1 sec
      assertConcurrentTest("testGetYear", runnables, maxTimeoutSeconds);
   }
   
   public void assertConcurrentTest(String testName, Collection<Runnable> runnables, int maxTimeoutSeconds) throws Exception {
      Collection<Exception> exceptions = Collections.synchronizedCollection(new ArrayList<Exception>());
      ExecutorService s = Executors.newWorkStealingPool(numThreads);

      final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
      final CountDownLatch afterInitBlocker = new CountDownLatch(1);
      final CountDownLatch allDone = new CountDownLatch(numThreads);

      
      
      try {
         for (Runnable r : runnables) {
            s.submit(new Runnable() {
               
               @Override
               public void run() {
                  allExecutorThreadsReady.countDown();
                  try {
                     afterInitBlocker.await();
                     r.run();
                  } catch (Exception e) {
                     exceptions.add(e);
                  } finally {
                     allDone.countDown();
                  }
               }
            });
         }
         
         // wait until all threads are ready
         assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent",
                    allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
         // start all test runners
         afterInitBlocker.countDown();
         assertTrue(String.format("[%s] timeout! More than [%d] seconds", testName, maxTimeoutSeconds), allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
      } finally {
         s.shutdown();
      }
      
      if (!exceptions.isEmpty()) {
         // this log can be huge, maybe a trace level
         for (Exception e : exceptions) {
            LOG.error("AssertConcurrentTest - Exception", e);
            e.printStackTrace();
         }
      }
      assertTrue(String.format("[%s] generated exceptions [%s]", testName, exceptions.size()), exceptions.isEmpty());
   }
}
