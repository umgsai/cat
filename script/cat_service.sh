#!/usr/bin/env bash

# CAT 服务启动脚本。
#
# 使用方式：
#   bash cat_service.sh start     启动 CAT
#   bash cat_service.sh stop      停止 CAT
#   bash cat_service.sh restart   重启 CAT
#   bash cat_service.sh status    查看 CAT 运行状态
#
# 当前应用的标准启动方式是：
#   java -jar ${HOME}/cat-boot.jar
#
# 这个脚本在该启动方式外层补充了几件生产环境常用能力：
#   1. 自动定位 Java 可执行文件，并检查当前用户目录下的 cat-boot.jar。
#   2. 维护 pid 文件，支持 start/stop/restart/status。
#   3. 配置 cat.home、cat.log.path、server.port 等 CAT 运行参数。
#   4. 配置 JVM 内存、GC 日志和 OOM dump。

set -u

# 服务名用于：
#   1. pid 文件名称；
#   2. stdout 日志名称；
#   3. 进程查找标记 -Dcat.service.name。
APP_NAME=${APP_NAME:-cat}

# CAT 启动 jar 固定放在当前用户目录下。
# 这里不做多路径查找：如果 ${HOME}/cat-boot.jar 不存在，启动时直接报错。
APP_PATH=${HOME}/cat-boot.jar

# CAT Web 端口。应用启动后默认访问地址为：
#   http://localhost:8080/cat/
SERVER_PORT=${SERVER_PORT:-8080}

# CAT_HOME 默认使用当前用户目录下的 .cat，和 CatBootApplication 中的默认逻辑保持一致。
# 该目录会存放 server.xml、bucket、logs 等 CAT 运行数据。
CAT_HOME=${CAT_HOME:-${HOME}/.cat}

# CAT 应用日志目录。cat-boot 的 logback.xml 默认也会使用 cat.log.path。
CAT_LOG_PATH=${CAT_LOG_PATH:-${CAT_HOME}/logs}

# 内嵌 Tomcat 的工作目录。每次启动时 EmbeddedCatServer 会清理该目录并重新展开/加载 Web 应用。
BOOT_BASE_DIR=${BOOT_BASE_DIR:-${CAT_HOME}/work/cat-boot-${SERVER_PORT}}

# pid 文件和 nohup stdout 日志。
# 应用自身日志主要在 ${CAT_LOG_PATH} 下，LOG_PATH 只记录 JVM 标准输出/标准错误。
PID_FILE=${PID_FILE:-${CAT_HOME}/${APP_NAME}.pid}
LOG_PATH=${LOG_PATH:-${CAT_LOG_PATH}/${APP_NAME}.out}

# JVM GC 日志和 OOM heap dump 输出位置。
GC_LOG_PATH=${GC_LOG_PATH:-${CAT_LOG_PATH}/gc.log}
OOM_DUMP_PATH=${OOM_DUMP_PATH:-${CAT_LOG_PATH}/oom_dump.hprof}

# Java 可执行文件路径。
# 未指定时优先使用 ${JAVA_HOME}/bin/java，再 fallback 到 PATH 中的 java。
JAVA_BIN=${JAVA_BIN:-}

# 生产环境使用 JDK 21。启动时会校验 Java 主版本，避免 JAVA_HOME 指到错误版本。
JAVA_REQUIRED_MAJOR=${JAVA_REQUIRED_MAJOR:-21}

# JVM 内存参数默认值。可以按机器规格通过环境变量覆盖。
# JDK 21 下建议 Xms 和 Xmx 保持一致，减少运行期堆扩容带来的抖动。
JAVA_XMS=${JAVA_XMS:-4096m}
JAVA_XMX=${JAVA_XMX:-4096m}
JAVA_XSS=${JAVA_XSS:-1024k}

# Metaspace 只设置初始值，不默认设置 MaxMetaspaceSize。
# JDK 21 的 Metaspace 回收能力已经比较成熟，固定上限容易在 JSP/Tomcat 类加载较多时误触发 OOM。
JAVA_METASPACE_SIZE=${JAVA_METASPACE_SIZE:-256m}

# G1 的停顿目标。200ms 是 JDK 21 下比较常用的服务端默认目标；如果更关注吞吐，可适当调大。
JAVA_MAX_GC_PAUSE_MILLIS=${JAVA_MAX_GC_PAUSE_MILLIS:-200}

# stop 时等待优雅退出的最长秒数，超时后会 kill -9。
STOP_TIMEOUT=${STOP_TIMEOUT:-30}

# 默认 JVM 参数。
# 如果设置 JAVA_OPTS，会整体替换下面这组默认值；如果只是追加参数，请使用 CAT_EXTRA_JAVA_OPTS。
DEFAULT_JAVA_OPTS=${JAVA_OPTS:-"-server -Xms${JAVA_XMS} -Xmx${JAVA_XMX} -Xss${JAVA_XSS} -XX:MetaspaceSize=${JAVA_METASPACE_SIZE} -XX:+UseG1GC -XX:MaxGCPauseMillis=${JAVA_MAX_GC_PAUSE_MILLIS} -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+AlwaysPreTouch -XX:+ExplicitGCInvokesConcurrent -Xlog:gc*,safepoint:file=${GC_LOG_PATH}:time,uptime,level,tags:filecount=10,filesize=100M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${OOM_DUMP_PATH} -XX:+ExitOnOutOfMemoryError -XX:ErrorFile=${CAT_LOG_PATH}/hs_err_pid%p.log -XX:-OmitStackTraceInFastThrow"}
CAT_EXTRA_JAVA_OPTS=${CAT_EXTRA_JAVA_OPTS:-}

# 输出使用说明。
usage() {
  echo "Usage: bash cat_service.sh [start|stop|restart|status]"
  exit 1
}

# 定位 Java 可执行文件。
# 允许用 JAVA_BIN 指定完整路径，例如：
#   JAVA_BIN=/opt/jdk-21/bin/java bash cat_service.sh start
resolve_java_bin() {
  if [ -n "${JAVA_BIN}" ]; then
    return
  fi

  if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
    JAVA_BIN="${JAVA_HOME}/bin/java"
    return
  fi

  JAVA_BIN=$(command -v java 2>/dev/null || true)
}

# 校验 Java 主版本。
# 当前应用按 JDK 21 生产环境配置 JVM 参数，如果实际运行版本不一致，直接停止启动。
validate_java_version() {
  java_version_text=$("${JAVA_BIN}" -version 2>&1 | head -n 1)
  java_major=$(printf '%s\n' "${java_version_text}" | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p')

  if [ -z "${java_major}" ]; then
    echo "unable to detect java version from: ${java_version_text}"
    exit 1
  fi

  if [ "${java_major}" != "${JAVA_REQUIRED_MAJOR}" ]; then
    echo "JDK ${JAVA_REQUIRED_MAJOR} is required, but current java is: ${java_version_text}"
    exit 1
  fi
}

# 查找当前 CAT 进程 PID。
# 优先使用 pid 文件；如果 pid 文件丢失，再根据 -Dcat.service.name 查找进程。
# 这种方式比单纯 pgrep cat-boot jar 更稳，避免误匹配其他 Java 进程。
find_pid() {
  if [ -f "${PID_FILE}" ]; then
    saved_pid=$(tr -d '[:space:]' < "${PID_FILE}")

    if [ -n "${saved_pid}" ] && kill -0 "${saved_pid}" 2>/dev/null; then
      echo "${saved_pid}"
      return
    fi
  fi

  pgrep -f -- "-Dcat.service.name=${APP_NAME}" 2>/dev/null | head -n 1
}

# 判断应用是否正在运行。
# 运行中返回 0，并把 pid 写入全局变量 pid；未运行返回 1。
is_exist() {
  pid=$(find_pid)

  if [ -z "${pid}" ]; then
    return 1
  fi

  return 0
}

# 启动前校验 Java 和 ${HOME}/cat-boot.jar 是否存在。
# 缺少依赖时直接失败，避免 nohup 后静默退出，排查困难。
validate_startup_files() {
  resolve_java_bin

  if [ -z "${JAVA_BIN}" ] || [ ! -x "${JAVA_BIN}" ]; then
    echo "java executable not found or not executable. Set JAVA_HOME or JAVA_BIN."
    exit 1
  fi
  validate_java_version

  if [ ! -f "${APP_PATH}" ]; then
    echo "cat boot jar not found: ${APP_PATH}"
    exit 1
  fi
}

# 启动 CAT。
# 关键启动参数：
#   -Dcat.home        指向 CAT_HOME，影响 server.xml、bucket、logs 等路径。
#   -Dcat.log.path    指向应用日志目录。
#   -Dcat.boot.baseDir 指向内嵌 Tomcat 工作目录。
#   -Dserver.port     指向 Web 端口。
start() {
  is_exist
  if [ $? -eq 0 ]; then
    echo "${APP_NAME} is already running. pid=${pid}"
    return
  fi

  validate_startup_files
  mkdir -p "${CAT_HOME}" "${CAT_LOG_PATH}" "$(dirname "${PID_FILE}")"

  # 尽量提高文件句柄上限。权限不足时忽略，不阻塞启动。
  ulimit -n 65535 >/dev/null 2>&1 || true

  # DEFAULT_JAVA_OPTS 和 CAT_EXTRA_JAVA_OPTS 保持不加引号展开，是为了允许传入多个 JVM 参数。
  nohup "${JAVA_BIN}" \
    -Dcat.service.name="${APP_NAME}" \
    -Dcat.home="${CAT_HOME}" \
    -DCAT_HOME="${CAT_HOME}" \
    -Dcat.log.path="${CAT_LOG_PATH}" \
    -Dcat.boot.baseDir="${BOOT_BASE_DIR}" \
    -Dserver.port="${SERVER_PORT}" \
    -Dfile.encoding=UTF-8 \
    ${DEFAULT_JAVA_OPTS} \
    ${CAT_EXTRA_JAVA_OPTS} \
    -jar "${APP_PATH}" > "${LOG_PATH}" 2>&1 &

  new_pid=$!
  echo "${new_pid}" > "${PID_FILE}"

  # nohup 命令返回不代表 Java 应用真正启动成功。
  # 等待 3 秒后检查进程是否仍然存在，若已退出则提示查看 stdout 日志。
  sleep 3
  if kill -0 "${new_pid}" 2>/dev/null; then
    echo "${APP_NAME} start success. pid=${new_pid}, url=http://localhost:${SERVER_PORT}/cat/"
  else
    rm -f "${PID_FILE}"
    echo "${APP_NAME} start failed, check log: ${LOG_PATH}"
    exit 1
  fi
}

# 停止 CAT。
# 先发送 SIGTERM，给 Spring/Tomcat/CAT checkpoint 留出优雅退出机会。
# 超过 STOP_TIMEOUT 后仍未退出，再使用 SIGKILL。
stop() {
  is_exist
  if [ $? -ne 0 ]; then
    echo "${APP_NAME} is not running."
    rm -f "${PID_FILE}"
    return
  fi

  kill "${pid}"

  i=0
  while [ "${i}" -lt "${STOP_TIMEOUT}" ]; do
    sleep 1
    if ! kill -0 "${pid}" 2>/dev/null; then
      rm -f "${PID_FILE}"
      echo "${APP_NAME} stop success."
      return
    fi
    i=$((i + 1))
  done

  echo "${APP_NAME} graceful stop timeout, force killing. pid=${pid}"
  kill -9 "${pid}" 2>/dev/null || true
  rm -f "${PID_FILE}"
  echo "${APP_NAME} stop success."
}

# 查看 CAT 运行状态。
status() {
  is_exist
  if [ $? -eq 0 ]; then
    echo "${APP_NAME} is running. pid=${pid}, url=http://localhost:${SERVER_PORT}/cat/"
  else
    echo "${APP_NAME} is not running."
  fi
}

# 重启 CAT。
# restart = stop + sleep 3 + start。
restart() {
  stop
  sleep 3
  start
}

# 根据命令行参数分发到对应动作。
case "${1:-}" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    restart
    ;;
  status)
    status
    ;;
  *)
    usage
    ;;
esac
