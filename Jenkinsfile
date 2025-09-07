pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  environment {
    DOCKER_COMPOSE = '/usr/local/bin/docker-compose' // đã có trên máy Jenkins của bạn
    COMPOSE_FILE   = 'docker-compose.yml'
    ENV_FILE       = '.env'
    APP_SERVICE    = 'app' // đổi nếu service tên khác
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Verify workspace & .env') {
      steps {
        sh '''
          set -e
          echo ">>> PWD: $(pwd)"
          ls -la

          echo ">>> Kiểm tra file bắt buộc"
          test -f "$COMPOSE_FILE"
          test -f "$ENV_FILE"

          # Chuẩn hóa CRLF -> LF cho .env
          sed -i 's/\r$//' "$ENV_FILE" || true
          echo ">>> Kích thước .env:"; wc -c "$ENV_FILE" || true

          # Xử lý Maven settings: ưu tiên ci/settings.xml, sau đó settings.xml; nếu không có tạo rỗng
          if [ -f ci/settings.xml ]; then
            echo "[i] Found ci/settings.xml -> copy sang settings.xml"
            cp -f ci/settings.xml settings.xml
          elif [ -f settings.xml ]; then
            echo "[i] Found settings.xml tại root repo"
          else
            echo "[i] Không có settings.xml -> tạo rỗng (OK nếu chỉ dùng Maven Central)"
            mkdir -p .
            printf '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"/>\n' > settings.xml
          fi

          echo ">>> Liệt kê nhanh:"
          ls -la | sed -n '1,100p'
        '''
      }
    }

    stage('Install docker-compose if missing') {
      steps {
        sh '''
          set -e
          if ! "$DOCKER_COMPOSE" version >/dev/null 2>&1; then
            echo "[!] Không tìm thấy docker-compose tại $DOCKER_COMPOSE"
            exit 1
          fi
          "$DOCKER_COMPOSE" version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''
          set -e
          echo "=== docker-compose build ==="
          "$DOCKER_COMPOSE" -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build
        '''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''
          set -e
          echo "=== docker-compose up -d ==="
          "$DOCKER_COMPOSE" -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

          echo "=== Đợi container lên (nếu có service app) ==="
          if "$DOCKER_COMPOSE" -f "$COMPOSE_FILE" config --services | grep -q "^$APP_SERVICE$"; then
            for i in $(seq 1 30); do
              CID=$("$DOCKER_COMPOSE" -f "$COMPOSE_FILE" ps -q "$APP_SERVICE" || true)
              if [ -n "$CID" ]; then
                STATUS=$(docker inspect -f '{{.State.Status}} {{.State.Health.Status}}' "$CID" 2>/dev/null || true)
                echo "[i] $APP_SERVICE status: $STATUS"
                echo "$STATUS" | grep -E 'running|healthy' >/dev/null && break
              fi
              sleep 2
            done
          else
            echo "[i] Không thấy service '$APP_SERVICE' trong compose -> bỏ qua chờ."
          fi
        '''
      }
    }

    stage('Validate compose file (optional)') {
      steps {
        sh '''
          set -e
          echo ">>> Kiểm tra (optional) service app trong compose:"
          if grep -n '^[[:space:]]*app:' "$COMPOSE_FILE" >/dev/null; then
            echo "[i] Tìm thấy service 'app'."
          else
            echo "[WARN] Không thấy 'app' trong $COMPOSE_FILE (không sao nếu bạn đặt tên khác)."
          fi
        '''
      }
    }
  }

  post {
    success {
      echo '✅ Deploy OK'
      sh '''
        "$DOCKER_COMPOSE" -f "$COMPOSE_FILE" ps || true
      '''
    }
    failure {
      echo '❌ Deploy fail — in logs'
      sh '''
        set +e
        "$DOCKER_COMPOSE" -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs --no-color || true
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
      '''
    }
    always {
      echo '🏁 Pipeline kết thúc.'
    }
  }
}