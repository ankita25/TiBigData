/*
 * Copyright 2021 TiDB Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tidb.bigdata.tidb;

import io.tidb.bigdata.jdbc.TiDBDriver;
import java.util.Map;
import java.util.Objects;

public final class ClientConfig {

  public static final String TIDB_DRIVER_NAME = "io.tidb.bigdata.jdbc.TiDBDriver";

  public static final String MYSQL_DRIVER_NAME = "com.mysql.jdbc.Driver";

  public static final String DATABASE_URL = "tidb.database.url";

  public static final String USERNAME = "tidb.username";

  public static final String PASSWORD = "tidb.password";

  public static final String CLUSTER_TLS_ENABLE = "tidb.cluster-tls-enable";
  public static final String CLUSTER_TLS_CA = "tidb.cluster-tls-ca";
  public static final String CLUSTER_TLS_KEY = "tidb.cluster-tls-key";
  public static final String CLUSTER_TLS_CERT = "tidb.cluster-tls-cert";

  public static final String MAX_POOL_SIZE = "tidb.maximum.pool.size";
  public static final String MAX_POOL_SIZE_DEFAULT = "1";

  public static final String MIN_IDLE_SIZE = "tidb.minimum.idle.size";
  public static final String MIN_IDLE_SIZE_DEFAULT = "1";

  public static final String TIDB_WRITE_MODE = "tidb.write_mode";
  public static final String TIDB_WRITE_MODE_DEFAULT = "append";

  public static final String TIDB_REPLICA_READ = "tidb.replica-read";
  public static final String TIDB_REPLICA_READ_DEFAULT = "leader";

  public static final String TIDB_REPLICA_READ_LABEL = "tidb.replica-read.label";
  public static final String TIDB_REPLICA_READ_LABEL_DEFAULT = "";

  public static final String TIDB_REPLICA_READ_ADDRESS_WHITELIST =
      "tidb.replica-read.address.whitelist";
  public static final String TIDB_REPLICA_READ_ADDRESS_BLACKLIST =
      "tidb.replica-read.address.blacklist";
  public static final String TIDB_REPLICA_READ_ADDRESS_DEFAULT = "";

  public static final String TIDB_FILTER_PUSH_DOWN = "tidb.filter-push-down";
  public static final String TIDB_FILTER_PUSH_DOWN_DEFAULT = "false";

  public static final String SNAPSHOT_TIMESTAMP = "tidb.snapshot_timestamp";
  public static final String SNAPSHOT_VERSION = "tidb.snapshot_version";

  public static final String TIDB_DNS_SEARCH = "tidb.dns.search";
  public static final String TIDB_DNS_SEARCH_DEFAULT = null;

  public static final String TIKV_GRPC_TIMEOUT = "tikv.grpc.timeout_in_ms";
  public static final String TIKV_GRPC_TIMEOUT_DEFAULT = Long.toString(60 * 1000L);

  public static final String TIKV_GRPC_SCAN_TIMEOUT = "tikv.grpc.scan_timeout_in_ms";
  public static final String TIKV_GRPC_SCAN_TIMEOUT_DEFAULT = Long.toString(60 * 1000L);

  public static final String TIDB_BUILD_IN_DATABASE_VISIBLE = "tidb.build-in.database.visible";
  public static final String TIDB_BUILD_IN_DATABASE_VISIBLE_DEFAULT = "false";

  private String pdAddresses;

  private String databaseUrl;

  private String username;

  private String password;

  private boolean clusterTlsEnabled;

  private String clusterTlsCA;

  private String clusterTlsKey;

  private String clusterTlsCert;

  private int maximumPoolSize;

  private int minimumIdleSize;

  private String writeMode;

  private ReplicaReadPolicy replicaReadPolicy;

  private boolean isFilterPushDown;

  private String dnsSearch;

  private long timeout;

  private long scanTimeout;

  private boolean buildInDatabaseVisible;

  public ClientConfig() {
    this(null,
        null,
        null,
        Integer.parseInt(MAX_POOL_SIZE_DEFAULT),
        Integer.parseInt(MIN_IDLE_SIZE_DEFAULT),
        TIDB_WRITE_MODE_DEFAULT,
        ReplicaReadPolicy.DEFAULT,
        Boolean.parseBoolean(TIDB_FILTER_PUSH_DOWN_DEFAULT),
        TIDB_DNS_SEARCH_DEFAULT,
        Long.parseLong(TIKV_GRPC_TIMEOUT_DEFAULT),
        Long.parseLong(TIKV_GRPC_SCAN_TIMEOUT_DEFAULT),
        Boolean.parseBoolean(TIDB_BUILD_IN_DATABASE_VISIBLE_DEFAULT));
  }

  public ClientConfig(String databaseUrl, String username, String password) {
    this(databaseUrl,
        username,
        password,
        Integer.parseInt(MAX_POOL_SIZE_DEFAULT),
        Integer.parseInt(MIN_IDLE_SIZE_DEFAULT),
        TIDB_WRITE_MODE_DEFAULT,
        ReplicaReadPolicy.DEFAULT,
        Boolean.parseBoolean(TIDB_FILTER_PUSH_DOWN_DEFAULT),
        TIDB_DNS_SEARCH_DEFAULT,
        Long.parseLong(TIKV_GRPC_TIMEOUT_DEFAULT),
        Long.parseLong(TIKV_GRPC_SCAN_TIMEOUT_DEFAULT),
        Boolean.parseBoolean(TIDB_BUILD_IN_DATABASE_VISIBLE_DEFAULT));
  }

  /* For historical compatibility, this constructor omits cluster TLS
   * options and implicitly disables TLS. */
  public ClientConfig(String databaseUrl,
      String username,
      String password,
      int maximumPoolSize,
      int minimumIdleSize,
      String writeMode,
      ReplicaReadPolicy replicaRead,
      boolean isFilterPushDown,
      String dnsSearch,
      long timeout,
      long scanTimeout,
      boolean buildInDatabaseVisible) {
    this.databaseUrl = databaseUrl;
    this.username = username;
    this.password = password;
    this.clusterTlsEnabled = false;
    this.clusterTlsCA = null;
    this.clusterTlsKey = null;
    this.clusterTlsCert = null;
    this.maximumPoolSize = maximumPoolSize;
    this.minimumIdleSize = minimumIdleSize;
    this.writeMode = writeMode;
    this.replicaReadPolicy = replicaRead;
    this.isFilterPushDown = isFilterPushDown;
    this.dnsSearch = dnsSearch;
    this.timeout = timeout;
    this.scanTimeout = scanTimeout;
    this.buildInDatabaseVisible = buildInDatabaseVisible;
  }

  /* This constructor adds support for cluster TLS options without
   * breaking backward compatibility for existing programs. */
  public ClientConfig(String databaseUrl,
      String username,
      String password,
      boolean clusterTlsEnable,
      String clusterTlsCA,
      String clusterTlsKey,
      String clusterTlsCert,
      int maximumPoolSize,
      int minimumIdleSize,
      String writeMode,
      ReplicaReadPolicy replicaRead,
      boolean isFilterPushDown,
      String dnsSearch,
      long timeout,
      long scanTimeout,
      boolean buildInDatabaseVisible) {
    this.databaseUrl = databaseUrl;
    this.username = username;
    this.password = password;
    this.clusterTlsEnabled = clusterTlsEnable;
    this.clusterTlsCA = clusterTlsCA;
    this.clusterTlsKey = clusterTlsKey;
    this.clusterTlsCert = clusterTlsCert;
    this.maximumPoolSize = maximumPoolSize;
    this.minimumIdleSize = minimumIdleSize;
    this.writeMode = writeMode;
    this.replicaReadPolicy = replicaRead;
    this.isFilterPushDown = isFilterPushDown;
    this.dnsSearch = dnsSearch;
    this.timeout = timeout;
    this.scanTimeout = scanTimeout;
    this.buildInDatabaseVisible = buildInDatabaseVisible;
  }

  public ClientConfig(Map<String, String> properties) {
    this(properties.get(DATABASE_URL),
        properties.get(USERNAME),
        properties.get(PASSWORD),
        Boolean.parseBoolean(properties.get(CLUSTER_TLS_ENABLE)),
        properties.get(CLUSTER_TLS_CA),
        properties.get(CLUSTER_TLS_KEY),
        properties.get(CLUSTER_TLS_CERT),
        Integer.parseInt(properties.getOrDefault(MAX_POOL_SIZE, MAX_POOL_SIZE_DEFAULT)),
        Integer.parseInt(properties.getOrDefault(MIN_IDLE_SIZE, MIN_IDLE_SIZE_DEFAULT)),
        properties.getOrDefault(TIDB_WRITE_MODE, TIDB_WRITE_MODE_DEFAULT),
        ReplicaReadPolicy.create(properties),
        Boolean.parseBoolean(
            properties.getOrDefault(TIDB_FILTER_PUSH_DOWN, TIDB_FILTER_PUSH_DOWN_DEFAULT)),
        properties.getOrDefault(TIDB_DNS_SEARCH, TIDB_DNS_SEARCH_DEFAULT),
        Long.parseLong(properties.getOrDefault(TIKV_GRPC_TIMEOUT, TIKV_GRPC_TIMEOUT_DEFAULT)),
        Long.parseLong(
            properties.getOrDefault(TIKV_GRPC_SCAN_TIMEOUT, TIKV_GRPC_SCAN_TIMEOUT_DEFAULT)),
        Boolean.parseBoolean(properties.getOrDefault(TIDB_BUILD_IN_DATABASE_VISIBLE,
            TIDB_BUILD_IN_DATABASE_VISIBLE_DEFAULT))
    );
  }

  public ClientConfig(ClientConfig config) {
    this(config.getDatabaseUrl(),
        config.getUsername(),
        config.getPassword(),
        config.getClusterTlsEnabled(),
        config.getClusterTlsCA(),
        config.getClusterTlsKey(),
        config.getClusterTlsCert(),
        config.getMaximumPoolSize(),
        config.getMinimumIdleSize(),
        config.getWriteMode(),
        config.getReplicaReadPolicy(),
        config.isFilterPushDown(),
        config.getDnsSearch(),
        config.getTimeout(),
        config.getScanTimeout(),
        config.isBuildInDatabaseVisible());
  }

  public boolean isFilterPushDown() {
    return isFilterPushDown;
  }

  public void setFilterPushDown(boolean filterPushDown) {
    isFilterPushDown = filterPushDown;
  }

  public final ReplicaReadPolicy getReplicaReadPolicy() {
    return replicaReadPolicy;
  }

  public String getPdAddresses() {
    return pdAddresses;
  }

  public void setPdAddresses(String addresses) {
    this.pdAddresses = addresses;
  }

  public String getDatabaseUrl() {
    return databaseUrl;
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean getClusterTlsEnabled() {
    return clusterTlsEnabled;
  }

  public void setClusterTlsEnabled(boolean enabled) {
    this.clusterTlsEnabled = enabled;
  }

  public String getClusterTlsCA() {
    return clusterTlsCA;
  }

  public void setClusterTlsCA(String ca) {
    this.clusterTlsCA = ca;
  }

  public String getClusterTlsKey() {
    return clusterTlsKey;
  }

  public void setClusterTlsKey(String key) {
    this.clusterTlsKey = key;
  }

  public String getClusterTlsCert() {
    return clusterTlsCert;
  }

  public void setClusterTlsCert(String cert) {
    this.clusterTlsCert = cert;
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public int getMinimumIdleSize() {
    return minimumIdleSize;
  }

  public void setMinimumIdleSize(int minimumIdleSize) {
    this.minimumIdleSize = minimumIdleSize;
  }

  public String getWriteMode() {
    return writeMode;
  }

  public void setWriteMode(String writeMode) {
    this.writeMode = writeMode;
  }

  public String getDriverName() {
    return TiDBDriver.driverForUrl(databaseUrl);
  }

  public String getDnsSearch() {
    return dnsSearch;
  }

  public void setDnsSearch(String dnsSearch) {
    this.dnsSearch = dnsSearch;
  }

  public void setReplicaReadPolicy(ReplicaReadPolicy replicaReadPolicy) {
    this.replicaReadPolicy = replicaReadPolicy;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public long getScanTimeout() {
    return scanTimeout;
  }

  public void setScanTimeout(long scanTimeout) {
    this.scanTimeout = scanTimeout;
  }

  public boolean isBuildInDatabaseVisible() {
    return buildInDatabaseVisible;
  }

  public void setBuildInDatabaseVisible(boolean buildInDatabaseVisible) {
    this.buildInDatabaseVisible = buildInDatabaseVisible;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientConfig that = (ClientConfig) o;
    return maximumPoolSize == that.maximumPoolSize
        && minimumIdleSize == that.minimumIdleSize
        && isFilterPushDown == that.isFilterPushDown
        && timeout == that.timeout
        && scanTimeout == that.scanTimeout
        && buildInDatabaseVisible == that.buildInDatabaseVisible
        && Objects.equals(pdAddresses, that.pdAddresses)
        && Objects.equals(databaseUrl, that.databaseUrl)
        && Objects.equals(username, that.username)
        && Objects.equals(password, that.password)
        && Objects.equals(clusterTlsEnabled, that.clusterTlsEnabled)
        && Objects.equals(clusterTlsCA, that.clusterTlsCA)
        && Objects.equals(clusterTlsKey, that.clusterTlsKey)
        && Objects.equals(clusterTlsCert, that.clusterTlsCert)
        && Objects.equals(writeMode, that.writeMode)
        && Objects.equals(replicaReadPolicy, that.replicaReadPolicy)
        && Objects.equals(dnsSearch, that.dnsSearch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pdAddresses, databaseUrl, username, password, clusterTlsEnabled,
        clusterTlsCA, clusterTlsKey, clusterTlsCert, maximumPoolSize,
        minimumIdleSize, writeMode, replicaReadPolicy, isFilterPushDown, dnsSearch, timeout,
        scanTimeout, buildInDatabaseVisible);
  }

  @Override
  public String toString() {
    return "ClientConfig{"
        + "pdAddresses='" + pdAddresses + '\''
        + ", databaseUrl='" + databaseUrl + '\''
        + ", username='" + username + '\''
        + ", password='" + password + '\''
        + ", clusterTlsEnabled='" + clusterTlsEnabled + '\''
        + ", clusterTlsCA='" + clusterTlsCA + '\''
        + ", clusterTlsKey='" + clusterTlsKey + '\''
        + ", clusterTlsCert='" + clusterTlsCert + '\''
        + ", maximumPoolSize=" + maximumPoolSize
        + ", minimumIdleSize=" + minimumIdleSize
        + ", writeMode='" + writeMode + '\''
        + ", replicaReadPolicy=" + replicaReadPolicy
        + ", isFilterPushDown=" + isFilterPushDown
        + ", dnsSearch='" + dnsSearch + '\''
        + ", timeout=" + timeout
        + ", scanTimeout=" + scanTimeout
        + ", buildInDatabaseVisible=" + buildInDatabaseVisible
        + '}';
  }
}
