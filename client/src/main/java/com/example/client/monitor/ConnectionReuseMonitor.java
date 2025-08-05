package com.example.client.monitor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConnectionReuseMonitor {

    // 跟踪每个连接对象的使用次数
    private final Map<String, ConnectionUsageInfo> connectionUsageMap = new ConcurrentHashMap<>();

    // 跟踪每个连接哈希码的使用历史
    private final Map<Integer, ConnectionReuseInfo> reuseStatsMap = new ConcurrentHashMap<>();

    public void trackConnectionAcquired(Connection connection) {
        int connectionHashCode = System.identityHashCode(connection);
        String connectionId = connection.getClass().getSimpleName() + "@" + Integer.toHexString(connectionHashCode);

        // 记录连接使用
        ConnectionReuseInfo reuseInfo = reuseStatsMap.computeIfAbsent(connectionHashCode,
                k -> new ConnectionReuseInfo(connectionId, connectionHashCode));
        reuseInfo.incrementUsageCount();
        reuseInfo.setLastUsedTime(System.currentTimeMillis());

        // 记录使用历史
        ConnectionUsageInfo usageInfo = new ConnectionUsageInfo(
                connectionId,
                System.currentTimeMillis(),
                reuseInfo.getUsageCount()
        );
        connectionUsageMap.put(Thread.currentThread().getName() + "-" + System.currentTimeMillis(), usageInfo);

        if (reuseInfo.getUsageCount() > 1) {
            log.info("【连接复用检测】检测到连接复用! 连接: {}, 总使用次数: {}",
                    connectionId, reuseInfo.getUsageCount());
        } else {
            log.info("【连接复用检测】新连接获取: {}", connectionId);
        }
    }

    public void printReuseStatistics() {
        log.info("=== 连接复用统计报告 ===");
        long reusedConnections = reuseStatsMap.values().stream()
                .filter(info -> info.getUsageCount() > 1)
                .count();

        long totalUsage = reuseStatsMap.values().stream()
                .mapToLong(ConnectionReuseInfo::getUsageCount)
                .sum();

        log.info("总连接数: {}", reuseStatsMap.size());
        log.info("被复用的连接数: {}", reusedConnections);
        log.info("总使用次数: {}", totalUsage);
        log.info("平均复用次数: {}", reuseStatsMap.isEmpty() ? 0 : (double) totalUsage / reuseStatsMap.size());

        // 显示详细复用信息
        reuseStatsMap.values().stream()
                .filter(info -> info.getUsageCount() > 1)
                .sorted((a, b) -> Long.compare(b.getUsageCount(), a.getUsageCount()))
                .forEach(info -> {
                    log.info("  连接 {} 被复用 {} 次, 最后使用时间: {}",
                            info.getConnectionId(),
                            info.getUsageCount(),
                            new Date(info.getLastUsedTime()));
                });
    }

    // 内部类定义
    @Getter
    private static class ConnectionReuseInfo {
        private final String connectionId;
        private final int identityHashCode;
        private long firstUsedTime;
        @Setter
        private long lastUsedTime;
        private long usageCount;

        public ConnectionReuseInfo(String connectionId, int identityHashCode) {
            this.connectionId = connectionId;
            this.identityHashCode = identityHashCode;
            this.firstUsedTime = System.currentTimeMillis();
            this.lastUsedTime = firstUsedTime;
            this.usageCount = 0;
        }

        public void incrementUsageCount() {
            if (this.usageCount == 0) {
                this.firstUsedTime = System.currentTimeMillis();
            }
            this.usageCount++;
        }


    }

    @Getter
    private static class ConnectionUsageInfo {
        private final String connectionId;
        private final long usageTime;
        private final long usageCountAtTime;

        public ConnectionUsageInfo(String connectionId, long usageTime, long usageCountAtTime) {
            this.connectionId = connectionId;
            this.usageTime = usageTime;
            this.usageCountAtTime = usageCountAtTime;
        }


    }
}
