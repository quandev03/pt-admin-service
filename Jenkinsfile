pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
  }

  environment {
    // Tên project docker-compose để dễ quản lý
    COMPOSE_PROJECT_NAME = 'pt-admin'
    // Dùng file compose mặc định tại root repo
    COMPOSE_FILE = 'docker-compose.yml'
    // Đường dẫn docker-compose binary (đã có 1.29.2 trên host)
    DC = '/usr/local/bin/docker-compose'
    // ID Managed File cho Maven settings
    MAVEN_SETTINGS_ID = 'maven-settings-vnsky'
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
          test -f "${COMPOSE_FILE}"
          test -f ".env"

          # Chuẩn hoá .env (loại CRLF nếu có)
          sed -i 's/\r$//' .env || true

          echo ">>> Kích thước .env:"
          wc -c .env || true
        '''
      }
    }

    stage('Materialize Maven settings.xml') {
      steps {
        script {
          sh 'mkdir -p ci'

          // Lấy settings.xml từ Managed Files
          configFileProvider([configFile(fileId: env.MAVEN_SETTINGS_ID, targetLocation: 'ci/settings.xml')]) {
            sh '''
              set -e
              echo "=== settings.xml đã ghi vào ci/settings.xml ==="
              ls -la ci
              # show vài dòng đầu để debug (không in toàn bộ)
              head -n 5 ci/settings.xml || true
            '''
          }
        }
      }
    }

    stage('Install docker-compose if missing') {
      steps {
        sh '''
          set -e
          ${DC} version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''
          set -e
          echo "=== docker-compose build ==="
          # In danh sách để chắc chắn settings.xml có trong context
          echo "== LS ROOT ==" && ls -la
          echo "== LS ci ==" && ls -la ci

          # Build với env-file để thay biến trong compose (nếu compose dùng ${VAR})
          ${DC} -f "${COMPOSE_FILE}" --env-file .env build
        '''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''
          set -e
          echo "=== docker-compose up -d ==="
          ${DC} -f "${COMPOSE_FILE}" --env-file .env up -d
          echo "=== docker-compose ps ==="
          ${DC} -f "${COMPOSE_FILE}" --env-file .env ps
        '''
      }
    }

    stage('Health Check (app)') {
      when {
        expression {
          // Chạy nếu file compose có service "app"
          // Không fail pipeline ở bước when nếu grep không thấy
          return sh(script: "sh '''
                               set -e
                               # Kiểm tra service app có trong compose file không
                               grep -n '^\s*app:' "$COMPOSE_FILE" >/dev/null || {
                                 echo "[WARN] Không thấy service 'app' trong $COMPOSE_FILE"
                               }
                             '''", returnStdout: true).trim() == '0'
        }
      }
      steps {
        // Tuỳ theo app của bạn, điều chỉnh liveness path/port
        sh '''
          set -e
          echo "=== Kiểm tra container app có up không ==="
          ${DC} -f "${COMPOSE_FILE}" --env-file .env ps

          # Thử curl local nếu app map cổng, ví dụ 8081
          # Điều chỉnh URL/port/path cho đúng thực tế dự án
          if command -v curl >/dev/null 2>&1; then
            echo "=== Thử HTTP health ==="
            curl -m 5 -fsS http://127.0.0.1:8081/actuator/health || true
          fi
        '''
      }
    }
  }

  post {
    success {
      echo "✅ Deploy thành công!"
      sh '''
        echo "=== Containers đang chạy ==="
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
      '''
    }

    failure {
      echo "❌ Deploy fail — in logs"
      // Thử in logs của compose nếu fail sớm
      sh '''
        set +e
        ${DC} -f "${COMPOSE_FILE}" --env-file .env logs --no-color || true
        echo "=== Containers hiện tại ==="
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
      '''
    }

    always {
      // Không xóa .env để lần sau còn dùng; settings.xml cũng giữ lại để build lần tới.
      echo "=== Pipeline kết thúc ==="
    }
  }
}