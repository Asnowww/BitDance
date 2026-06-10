#!/usr/bin/env bash
set -euo pipefail

# 本地联调数据库入口：后端连 127.0.0.1:15432，再经 SSH 服务器本机转发到 PostgreSQL 127.0.0.1:5432。
LOCAL_HOST="${BITDANCE_DB_TUNNEL_LOCAL_HOST:-127.0.0.1}"
LOCAL_PORT="${BITDANCE_DB_TUNNEL_LOCAL_PORT:-15432}"
SSH_HOST="${BITDANCE_SSH_HOST:-98.142.241.155}"
SSH_PORT="${BITDANCE_SSH_PORT:-22}"
SSH_USER="${BITDANCE_SSH_USER:-root}"
REMOTE_DB_HOST="${BITDANCE_REMOTE_DB_HOST:-127.0.0.1}"
REMOTE_DB_PORT="${BITDANCE_REMOTE_DB_PORT:-5432}"
# 本地联调会长时间挂着后端；给 SSH 隧道加 keepalive，避免空闲一段时间后静默断开。
SSH_KEEPALIVE_OPTS=(-o TCPKeepAlive=yes -o ServerAliveInterval=30 -o ServerAliveCountMax=3 -o ConnectTimeout=10)

if lsof -nP -iTCP:"${LOCAL_PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "SSH database tunnel already listening on ${LOCAL_HOST}:${LOCAL_PORT}"
  exit 0
fi

# 可选用 BITDANCE_SSH_PASSWORD 非交互启动；未设置时交给 ssh 自己提示密码，避免把 SSH 密码写入仓库文件。
if [[ -n "${BITDANCE_SSH_PASSWORD:-}" ]]; then
  expect <<EOF
set timeout 12
spawn ssh -fN -o ExitOnForwardFailure=yes -o NumberOfPasswordPrompts=1 -o PreferredAuthentications=password -o PubkeyAuthentication=no -o StrictHostKeyChecking=accept-new -o TCPKeepAlive=yes -o ServerAliveInterval=30 -o ServerAliveCountMax=3 -o ConnectTimeout=10 -p "${SSH_PORT}" -L "${LOCAL_HOST}:${LOCAL_PORT}:${REMOTE_DB_HOST}:${REMOTE_DB_PORT}" "${SSH_USER}@${SSH_HOST}"
expect {
  -re "(?i)password:" { send "\$env(BITDANCE_SSH_PASSWORD)\r"; exp_continue }
  eof
}
catch wait result
exit [lindex \$result 3]
EOF
else
  ssh -fN \
    -o ExitOnForwardFailure=yes \
    -o StrictHostKeyChecking=accept-new \
    "${SSH_KEEPALIVE_OPTS[@]}" \
    -p "${SSH_PORT}" \
    -L "${LOCAL_HOST}:${LOCAL_PORT}:${REMOTE_DB_HOST}:${REMOTE_DB_PORT}" \
    "${SSH_USER}@${SSH_HOST}"
fi

lsof -nP -iTCP:"${LOCAL_PORT}" -sTCP:LISTEN
