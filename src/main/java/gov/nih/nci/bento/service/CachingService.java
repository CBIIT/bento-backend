package gov.nih.nci.bento.service;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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

    private void startCacheSweeper() {
        runSweep = true;
        int timeout = config.getCacheTimeToLiveSec();
        taskExecutor.execute(() -> {
            logger.debug("Cache sweeping started");
            try {
                while (runSweep) {
                    if (!timeoutQueue.isEmpty()) {
                        synchronized (syncLock) {
                            int hash = timeoutQueue.peek();
                            long diff = System.currentTimeMillis() - cache.get(hash).getTime();
                            if (diff / 1000 > timeout) {
                                remove(hash);
                                continue;
                            }
                        }
                    }
                    Thread.sleep(10 * 1000);
                }
                logger.debug("Cache sweeping stopped");
            }catch (InterruptedException e) {
                logger.debug("Cache sweeping interrupted");
            }
        });
    }

    private void stopCacheSweeper(){
        runSweep = false;
    }

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

    public void cache(int hashValue, JsonNode response){
        synchronized (syncLock){
            if(!cache.containsKey(hashValue)){
                add(hashValue, response);
            }
        }
    }

    /**
     * Checks the cache for a response to the input query and returns the response if it is found.
     *
     * @param hashValue The hash representation of the query
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

    private double getSizeMB(String s){
        double bytes = s.length() * 2 + 38;
        bytes += bytes % 8;
        return bytes / 1000000;
    }

    private class CacheObject{

        private int hashValue;
        private String data;
        private long time;
        private double size;

        CacheObject(int hashValue, JsonNode data){
            this.hashValue = hashValue;
            this.data = data.toString();
            this.time = System.currentTimeMillis();
            calcSizeEstimate();
        }

        private void calcSizeEstimate(){
            double dataSize = data.length() * 2 + 38;
            dataSize += dataSize % 8;
            size += dataSize;
            size += 4; //hashValue (int)
            size += 8; //size (double)
            size += 16; //time (long)
            size = size/1000000;
        }

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
