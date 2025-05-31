package exam_easv_belman.GUI;

import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.util.SessionManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class SessionManagerTest {

    /**
     * Sets up the test environment by resetting the singleton instance. Using the @BeforeEach annotation, means that this will
     * be run before every single test further down the list.
     *
     * This method uses reflection to access and reset the instance. Reflection simply allows the test class to access the
     * instance even though it is private.
     */
    @BeforeEach
    void setUp() {
        try {
            Field instance = SessionManager.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple Singleton instance check.
     * Checks if when multiple SessionManager objects use the .getInstance() method, if they receive the same instance.
     */
    @Test
    void testSingletonPattern() {
        // Test singleton behavior
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();
        assertSame(instance2, instance1, "Should return the same instance");
    }

    /**
     * A test to see if the instance works at the same time across multiple different threads.
     * By using CountDownLatch we activate all the threads at once when it reaches (threadCount) amount of threads.
     * Then they all attempt to use SessionManager.getInstance().
     * The test checks if the first instance is the same as every threads instance.
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<SessionManager> instances = new ArrayList<>();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                //Every time a thread is created the latches goes down.
                latch.countDown();
                try {
                    //Sets the current thread to wait until latch = 0;
                    latch.await();
                    //gives instructions
                    instances.add(SessionManager.getInstance());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            //adds the threads to the list.
            threads.add(t);
            t.start();
        }

        //waits for every thread to finish their task.
        for (Thread t : threads) {
            t.join();
        }


        //checks if 1st thread result is the same as every other thread.
        SessionManager firstInstance = instances.get(0);
        for (SessionManager instance : instances) {
            assertSame(firstInstance, instance, "Thread safety test failed.");
        }
    }

    /**
     * Tests the SessionManager by simulating multiple threads attempting to set and retrieve the order number concurrently.
     * This test ensures that SessionManager's methods like setCurrentOrderNumber()
     * and getCurrentOrderNumber() handle simultaneous access from multiple threads correctly and maintain consistent information.
     *
     * This test includes:
     * - Using a CountDownLatch to coordinate the start of multiple threads.
     * - Each thread sets a unique order number and retrieves it to simulate concurrent access.
     * - Observes and records all the order numbers seen during execution.
     * - Validates conditions like non-null order numbers, pattern matching, and expected behavior.
     * - Ensures the final order number is valid and one of the test inputs.
     *
     * Assertion checks:
     * - Verifies no order number is null.
     * - Checks that all observed order numbers match the expected format.
     * - Confirms that the final state aligns with one of the assigned order numbers.
     *
     * @throws InterruptedException if the thread execution is interrupted
     */
    @Test
    void testConcurrentModification() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        //Using synchronizedSet and AtomicReference due to them being thread-safe.
        Set<String> observedOrderNumbers = Collections.synchronizedSet(new HashSet<>());
        AtomicReference<String> lastOrderNumber = new AtomicReference<>();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final String orderNum = "ORDER-" + i;
            Thread t = new Thread(() -> {
                latch.countDown();
                try {
                    latch.await();
                    SessionManager.getInstance().setCurrentOrderNumber(orderNum);
                    observedOrderNumbers.add(orderNum);  // Add the value we set, not what we get later

                    // Add a small delay for delayed hardware issues.
                    Thread.sleep(10);

                    String currentOrder = SessionManager.getInstance().getCurrentOrderNumber();
                    lastOrderNumber.set(currentOrder);

                    // Basic verification
                    assertNotNull(currentOrder, "Order number should not be null");
                    assertTrue(currentOrder.startsWith("ORDER-"),
                            "Order number should start with 'ORDER-'");

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(t);
            t.start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // check if there are the correct amount of responses from the threads.
        assertEquals(threadCount, observedOrderNumbers.size(),
                "Should have observed exactly " + threadCount + " order numbers");

        // check if the numbers follow the pattern created at the beginning.
        for (String orderNum : observedOrderNumbers) {
            assertTrue(orderNum.matches("ORDER-[0-9]"),
                    "Each order number should match our test pattern. Found: " + orderNum);
        }

        // check if the numbers used match the for loop above.
        for (int i = 0; i < threadCount; i++) {
            assertTrue(observedOrderNumbers.contains("ORDER-" + i),
                    "Missing expected order number: ORDER-" + i);
        }

        //check if the currect order number matches the pattern.
        String finalOrderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        assertTrue(finalOrderNumber.matches("ORDER-[0-9]"),
                "Final order number should match our test pattern");

        // check if the last order number that was set, is the same as the current order number.
        assertEquals(lastOrderNumber.get(), finalOrderNumber,
                "Final order number should match the last observed order number");
    }

        /**
         * This test verifies the following functions:
         *
         * - Setting and getting the current user.
         * - Setting and getting the current order number.
         * - Setting and getting whether the session is associated with a product.
         * - Setting and getting the current product number.
         * - Verifying the session state after changes.
         * - Logging out and ensuring all session attributes are reset to null or default state.
         *
         * Assertions are used to confirm that the session attributes are set and received
         * correctly, and to check if logout properly logs off the user.
         */
    @Test
    void testCompleteSessionLifecycle() {
        
        // Test complete lifecycle of a session
        User testUser = new User();
        testUser.setUsername("testUser");
        
        // Setup session
        SessionManager.getInstance().setCurrentUser(testUser);
        SessionManager.getInstance().setCurrentOrderNumber("ORDER-789");
        SessionManager.getInstance().setIsProduct(true);
        SessionManager.getInstance().setCurrentProductNumber("PROD-123");
        
        // Verify session state
        assertEquals(testUser, SessionManager.getInstance().getCurrentUser());
        assertEquals("ORDER-789", SessionManager.getInstance().getCurrentOrderNumber());
        assertTrue(SessionManager.getInstance().getIsProduct());
        assertEquals("PROD-123", SessionManager.getInstance().getCurrentProductNumber());
        
        // Test logout
        SessionManager.getInstance().logout();
        
        // Verify clean session state
        assertNull(SessionManager.getInstance().getCurrentUser());
        assertNull(SessionManager.getInstance().getCurrentOrderNumber());
    }
}