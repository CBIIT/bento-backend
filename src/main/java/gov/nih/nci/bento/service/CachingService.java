package gov.nih.nci.bento.service;

import com.mashape.unirest.http.JsonNode;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Service to cache GraphQL queries and their results.
 */
@Service
public class CachingService {

    private static final Logger logger = LogManager.getLogger(CachingService.class);
    private static final Object syncLock = new Object();
    private static final HashMap<Integer, CacheObject> cache = new HashMap<>();
    private static final Queue<Integer> timeoutQueue = new LinkedList<>();
    private static double cacheSize = 0;
    private static boolean runSweep;

    @Autowired
    private ConfigurationDAO config;
    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * Starts a thread that will loop every 30 seconds to check for expired cache entries and delete them. This will
     * until either the stop flag (runSweep) is set to false or the program is shut down.
     */
    private void startCacheSweeper() {
        runSweep = true;
        double timeout = config.getCacheTimeToLiveMin();
        taskExecutor.execute(() -> {
            logger.debug("Cache sweeping started");
            try {
                while (runSweep) {
                    if (!timeoutQueue.isEmpty()) {
                        synchronized (syncLock) {
                            int hash = timeoutQueue.peek();
                            long diff = System.currentTimeMillis() - cache.get(hash).getTime();
                            if (diff / 6000 > timeout) {
                                remove(hash);
                                continue;
                            }
                        }
                    }
                    Thread.sleep(5 / 60 * 6000);    //Sleep for 10 minutes
                }
                logger.debug("Cache sweeping stopped");
            }catch (InterruptedException e) {
                logger.debug("Cache sweeping interrupted");
            }
        });
    }

    /**
     * Sets a flag that will stop the cache sweeper thread.
     */
    private void stopCacheSweeper(){
        runSweep = false;
    }

    /**
     * Removes an entry from the cache, the cache sweeper is stopped if the cache is empty after the removal.
     *
     * @param hash The query hash value of the entry to be removed.
     */
    private void remove(int hash){
        double objectSize = cache.get(hash).getSize();
        cacheSize -= objectSize;
        if (cacheSize <= 0){
            cacheSize = 0;
            stopCacheSweeper();
        }
        cache.remove(hash);
        timeoutQueue.remove();
        logger.debug(hash+" has been cleared from cache, "+objectSize+" MB cleared");
    }

    /**
     * Add a query and response to the cache if it is not too big, if there is not room the oldest cached queries will
     * be removed until there is enough room. The cache sweeper is started if the cache was previously empty.
     *
     * @param hashValue The hash value of the query
     * @param response The JSON response to the query
     */
    private void add(int hashValue, JsonNode response){
        CacheObject cacheObject = new CacheObject(hashValue, response);
        double maxSize = config.getCacheMaxSizeMB();
        double size = cacheObject.getSize();
        if (maxSize > size){
            if(cacheSize <= 0){
                startCacheSweeper();
            }
            while(maxSize < (size + cacheSize)){
                remove(timeoutQueue.peek());
            }
            cacheSize += size;
            cache.put(hashValue, cacheObject);
            timeoutQueue.add(hashValue);
            logger.debug(hashValue+" has been added to the cache, "+size+"MB stored");
        }
        else {
            logger.debug(hashValue + " is too large to cache");
        }
    }

    /**
     * Thread safe operation to add a query and result to the cache if it has not already been stored.
     *
     * @param hashValue The hash value of the query
     * @param response The JSON response to the query
     */
    public void cache(int hashValue, JsonNode response){
        taskExecutor.execute(() -> {
            synchronized (syncLock){
                if(!cache.containsKey(hashValue)){
                    add(hashValue, response);
                }
            }
        });
    }

    /**
     * Returns a cached query response if it exists in the cache, otherwise returns null
     *
     * @param hashValue The hash value of the query
     * @return The query response as a JSONNode object, returns null if no response is cached
     */
    public JsonNode checkInCache(int hashValue){
        synchronized (syncLock){
            if(cache.containsKey(hashValue)){
                return cache.get(hashValue).getData();
            }
            else{
                return null;
            }
        }
    }

    /**
     * An object wrapper for a query response that is stored in the cache
     */
    private class CacheObject{
        private final int hashValue;  // The hash value of the query
        private final String data;    // The JSON response stored as a string
        private final long time;      // The time that this object was created
        private final double size;    // An estimate of the size of the object

        /**
         * Class constructor
         *
         * @param hashValue The hash value of the query
         * @param data The response to the query
         */
        CacheObject(int hashValue, JsonNode data){
            this.hashValue = hashValue;
            this.data = data.toString();
            this.time = System.currentTimeMillis();
            this.size = calcSizeEstimate();
        }

        /**
         * Estimates the minimum size of the CacheObject in MBs by adding up the size of the object properties
         */
        private double calcSizeEstimate(){
            double size = 0;
            double dataSize = data.length() * 2 + 38;
            dataSize += dataSize % 8;
            size += dataSize;
            size += 4;  //hashValue (int)
            size += 8;  //size (double)
            size += 16; //time (long)
            return size/1000000;
        }

        //Getters
        public int getHashValue() {
            return hashValue;
        }

        public JsonNode getData() {
            return new JsonNode(data);
        }

        public long getTime() {
            return time;
        }

        public double getSize() {
            return size;
        }
    }
}
