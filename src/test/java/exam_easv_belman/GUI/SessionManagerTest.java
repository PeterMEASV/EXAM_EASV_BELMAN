package exam_easv_belman.GUI;

import exam_easv_belman.BE.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
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
            java.lang.reflect.Field instance = SessionManager.class.getDeclaredField("instance");
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
     * A test to see if the instace works at the same time across multiple different threads.
     * By using CountDownLatch we activate all the threads at once when it reaches (threadCount) amount of threads.
     * Then they all attempt to use SessionManager.getInstance().
     * The the test checks if the first instance is the same as every threads instance.
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

            //runs the threads when they are ready.
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
        SessionManager manager = SessionManager.getInstance();
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        //Using synchronizedSet and AtomicReference due to them being thread-safe.
        //using a simple HashSet or A simple String could create errors.
        Set<String> observedOrderNumbers = Collections.synchronizedSet(new HashSet<>());
        AtomicReference<String> lastOrderNumber = new AtomicReference<>();
        
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final String orderNum = "ORDER-" + i;
            Thread t = new Thread(() -> {
                latch.countDown();
                try {
                    latch.await();
                    manager.setCurrentOrderNumber(orderNum);

                    // Add a small delay for delayed hardware issues.
                    Thread.sleep(10);

                    String currentOrder = manager.getCurrentOrderNumber();
                    observedOrderNumbers.add(currentOrder);
                    lastOrderNumber.set(currentOrder);
                    
                    // Basic verification
                    assertNotNull(currentOrder, "Order number should not be null");
                    assertTrue(currentOrder.startsWith("ORDER-"), 
                        "Order number should start with 'ORDER-'");
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
            t.start();
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
        
        // Verify the final state
        System.out.println("Final order number: " + lastOrderNumber.get());
        System.out.println("Observed order numbers: " + observedOrderNumbers);
        
        // Verify that we saw order numbers being changed
        assertTrue(observedOrderNumbers.size() > 0, 
            "Should have observed at least one order number");
        
        // Verify the current order number is one of our test numbers
        String finalOrderNumber = manager.getCurrentOrderNumber();
        assertTrue(finalOrderNumber.matches("ORDER-[0-9]"),
            "Final order number should match our test pattern");
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
        SessionManager manager = SessionManager.getInstance();
        
        // Test complete lifecycle of a session
        User testUser = new User();
        testUser.setUsername("testUser");
        
        // Setup session
        manager.setCurrentUser(testUser);
        manager.setCurrentOrderNumber("ORDER-789");
        manager.setIsProduct(true);
        manager.setCurrentProductNumber("PROD-123");
        
        // Verify session state
        assertEquals(testUser, manager.getCurrentUser());
        assertEquals("ORDER-789", manager.getCurrentOrderNumber());
        assertTrue(manager.getIsProduct());
        assertEquals("PROD-123", manager.getCurrentProductNumber());
        
        // Test logout
        manager.logout();
        
        // Verify clean session state
        assertNull(manager.getCurrentUser());
        assertNull(manager.getCurrentOrderNumber());
    }
}