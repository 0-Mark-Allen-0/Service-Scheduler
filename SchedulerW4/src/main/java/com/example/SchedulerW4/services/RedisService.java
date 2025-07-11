// src/main/java/com/example/SchedulerW4/services/RedisService.java
package com.example.SchedulerW4.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit; // For optional expiry

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // Key format for slot queues
    private String getSlotQueueKey(Long slotId) {
        return "slot_queue:" + slotId;
    }

    /**
     * Adds a userId to the end of a slot's queue.
     * @param slotId The ID of the slot.
     * @param userId The ID of the user to add to the queue.
     */
    public void addToQueue(Long slotId, Long userId) {
        String key = getSlotQueueKey(slotId);
        redisTemplate.opsForList().rightPush(key, String.valueOf(userId));
        // Optional: Set an expiry for the queue itself, in case a slot never becomes free
        // redisTemplate.expire(key, 24, TimeUnit.HOURS); // e.g., queue expires after 24 hours
    }

    /**
     * Retrieves and removes the first userId from a slot's queue.
     * @param slotId The ID of the slot.
     * @return The userId at the front of the queue, or null if the queue is empty.
     */
    public Long popFromQueue(Long slotId) {
        String key = getSlotQueueKey(slotId);
        String userIdStr = redisTemplate.opsForList().leftPop(key);
        return userIdStr != null ? Long.valueOf(userIdStr) : null;
    }

    /**
     * Checks if a specific userId is already in a slot's queue.
     * This iterates through the entire list, so for very large queues, consider alternative Redis structures (e.g., a Set).
     * @param slotId The ID of the slot.
     * @param userId The ID of the user to check.
     * @return true if the user is in the queue, false otherwise.
     */
    public boolean isUserInQueue(Long slotId, Long userId) {
        String key = getSlotQueueKey(slotId);
        List<String> usersInQueue = redisTemplate.opsForList().range(key, 0, -1); // Get all elements
        if (usersInQueue != null) {
            return usersInQueue.contains(String.valueOf(userId));
        }
        return false;
    }

    /**
     * Get the current size of the queue for a given slot.
     * @param slotId The ID of the slot.
     * @return The number of users in the queue.
     */
    public Long getQueueSize(Long slotId) {
        String key = getSlotQueueKey(slotId);
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0L;
    }

    /**
     * Removes a specific user from the queue (e.g., if they cancel their queue spot or reschedule).
     * @param slotId The ID of the slot.
     * @param userId The ID of the user to remove.
     * @return The number of elements removed.
     */
    public Long removeUserFromQueue(Long slotId, Long userId) {
        String key = getSlotQueueKey(slotId);
        // valueOf(0) removes all occurrences of the value
        return redisTemplate.opsForList().remove(key, 0, String.valueOf(userId));
    }
}