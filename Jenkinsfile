pipeline {
  agent any
  options { timestamps() }

  environment {
    COMPOSE_BIN         = '/usr/local/bin/docker-compose'   // đã cài ở bước chuẩn bị
    COMPOSE_FILE        = 'docker-compose.yml'
    ENV_FILE            = '.env'
    COMPOSE_PROJECT_NAME= 'pt-admin'                        // tên project compose
  }

  stages {
    stage('Checkout') {
      steps {
        // Lấy code từ nhánh develop
        git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
      }
    }

    stage('Verify workspace & .env') {
      steps {
        sh '''
          set -e
          echo ">>> PWD: $(pwd)"
          ls -la

          # Kiểm tra file bắt buộc
          test -f "$COMPOSE_FILE" || { echo "❌ Thiếu $COMPOSE_FILE"; exit 2; }
          test -f "$ENV_FILE"     || { echo "❌ Thiếu $ENV_FILE"; exit 3; }

          # Chuẩn hóa xuống dòng .env (nếu có CRLF)
          sed -i \'s/\r$//\' "$ENV_FILE"

          echo ">>> Kích thước .env:"
          wc -c "$ENV_FILE" || true
        '''
      }
    }

    stage('Install docker-compose if missing') {
      steps {
        sh '''
          set -e
          if ! $COMPOSE_BIN version >/dev/null 2>&1; then
            echo ">> Installing docker-compose 1.29.2 ..."
            curl -fsSL "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o "$COMPOSE_BIN"
            chmod +x "$COMPOSE_BIN"
          fi
          $COMPOSE_BIN version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''
          set -e
          echo "=== docker-compose build ==="
          $COMPOSE_BIN -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build
        '''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''
          set -e
          echo "=== docker-compose up -d ==="
          $COMPOSE_BIN -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

          echo "=== docker-compose ps ==="
          $COMPOSE_BIN -f "$COMPOSE_FILE" ps
        '''
      }
    }

    stage('Health Check (trong container app)') {
      steps {
        // Gọi từ bên trong container service "app"
        sh '''
          set -e
          echo "=== Health check qua compose exec ==="
          # Thử curl, nếu không có thì fallback wget (busybox)
          $COMPOSE_BIN -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec -T app sh -c '\
            if command -v curl >/dev/null 2>&1; then \
              curl -fsS http://localhost:8080/actuator/health; \
            elif command -v wget >/dev/null 2>&1; then \
              wget -qO- http://localhost:8080/actuator/health; \
            else \
              echo "⚠️  Không có curl/wget trong container. Bỏ qua health check chi tiết."; \
              exit 0; \
            fi'
        '''
      }
    }
  }

  post {
    success {
      echo '✅ Deploy bằng docker-compose thành công.'
    }
    failure {
      echo '❌ Deploy fail — in logs'
      sh '''
        set +e
        # In log nếu có
        $COMPOSE_BIN -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs --no-color || true
        # Liệt kê containers
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" || true
      '''
    }
  }
}