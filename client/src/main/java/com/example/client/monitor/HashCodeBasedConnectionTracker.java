package com.example.client.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class HashCodeBasedConnectionTracker {

    // 跟踪代理连接的使用情况
    private final Map<String, Integer> proxyConnectionUsageCount = new ConcurrentHashMap<>();

    // 跟踪真实连接的使用情况
    private final Map<Integer, RealConnectionInfo> realConnectionUsageCount = new ConcurrentHashMap<>();

    public void logConnectionWithHashCode(Connection connection, String operation) {
        // 获取代理连接标识
        String proxyConnectionHash = getProxyConnectionHash(connection);
        log.debug("准备跟踪代理连接: {}, 操作: {}", proxyConnectionHash, operation);

        // 记录代理连接使用次数
        int proxyUsageCount = proxyConnectionUsageCount.getOrDefault(proxyConnectionHash, 0) + 1;
        proxyConnectionUsageCount.put(proxyConnectionHash, proxyUsageCount);

        // 获取真实连接标识
        int realConnectionId = getRealConnectionIdentity(connection);
        String realConnectionHash = "RealConnection@" + Integer.toHexString(realConnectionId);

        // 记录真实连接使用次数
        RealConnectionInfo realInfo = realConnectionUsageCount.computeIfAbsent(realConnectionId,
                id -> new RealConnectionInfo(realConnectionHash, proxyConnectionHash));
        realInfo.incrementUsageCount();

        if (realInfo.getUsageCount() > 1) {
            log.info("【连接复用检测】操作: {}, 代理连接: {}, 真实连接: {}, 第 {} 次复用",
                    operation, proxyConnectionHash, realConnectionHash, realInfo.getUsageCount());
        } else {
            log.info("【连接首次使用】操作: {}, 代理连接: {}, 真实连接: {}, 首次代理: {}",
                    operation, proxyConnectionHash, realConnectionHash, realInfo.getFirstProxyConnection());
        }
    }

    private String getProxyConnectionHash(Connection connection) {
        return connection.getClass().getSimpleName() +
                "@" +
                Integer.toHexString(System.identityHashCode(connection));
    }

    private int getRealConnectionIdentity(Connection proxyConnection) {
        try {
            // 尝试 unwrap 获取真实连接
            if (proxyConnection.isWrapperFor(Connection.class)) {
                Connection realConnection = proxyConnection.unwrap(Connection.class);
                return System.identityHashCode(realConnection);
            }
        } catch (SQLException e) {
            log.debug("无法 unwrap 连接: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("获取真实连接时发生异常: {}", e.getMessage());
        }

        // 如果无法获取真实连接，使用代理连接的标识
        return System.identityHashCode(proxyConnection);
    }

    public void printHashStatistics() {
        log.info("=== 代理连接使用统计 ===");
        log.info("总共创建了 {} 个不同的代理连接", proxyConnectionUsageCount.size());
        proxyConnectionUsageCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        log.info("代理连接 {} 被使用 {} 次", entry.getKey(), entry.getValue())
                );

        log.info("=== 真实连接复用统计 ===");
        log.info("总共使用了 {} 个不同的真实连接", realConnectionUsageCount.size());

        long reusedConnections = realConnectionUsageCount.values().stream()
                .filter(info -> info.getUsageCount() > 1)
                .count();

        log.info("被复用的真实连接数: {}", reusedConnections);

        if (reusedConnections > 0) {
            realConnectionUsageCount.values().stream()
                    .filter(info -> info.getUsageCount() > 1)
                    .sorted((a, b) -> Integer.compare(b.getUsageCount(), a.getUsageCount()))
                    .forEach(info ->
                            log.info("真实连接 {} 被复用 {} 次, 首次通过代理: {}",
                                    info.getRealConnectionHash(), info.getUsageCount(), info.getFirstProxyConnection())
                    );
        } else {
            log.info("暂无真实连接被复用");
        }
    }

    private static class RealConnectionInfo {
        private final String realConnectionHash;
        private final String firstProxyConnection;
        private final long firstUsedTime;
        private long lastUsedTime;
        private int usageCount;

        public RealConnectionInfo(String realConnectionHash, String firstProxyConnection) {
            this.realConnectionHash = realConnectionHash;
            this.firstProxyConnection = firstProxyConnection;
            this.firstUsedTime = System.currentTimeMillis();
            this.lastUsedTime = firstUsedTime;
            this.usageCount = 0;
        }

        public void incrementUsageCount() {
            this.usageCount++;
            this.lastUsedTime = System.currentTimeMillis();
        }

        // Getters
        public String getRealConnectionHash() { return realConnectionHash; }
        public String getFirstProxyConnection() { return firstProxyConnection; }
        public long getFirstUsedTime() { return firstUsedTime; }
        public long getLastUsedTime() { return lastUsedTime; }
        public int getUsageCount() { return usageCount; }
    }
}
