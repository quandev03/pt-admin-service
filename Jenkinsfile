pipeline {
  agent any

  environment {
    COMPOSE_IMAGE = 'docker/compose:1.29.2'
    COMPOSE_PROJECT_NAME = 'pt-admin'
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
        sh '''
          echo "== LS WORKSPACE =="
          ls -la
          echo "== Check files =="
          test -f "${WORKSPACE}/docker-compose.yml" && echo "OK: docker-compose.yml" || (echo "MISS: docker-compose.yml" && exit 1)
          test -f "${WORKSPACE}/.env" && echo "OK: .env" || echo "WARN: .env not found (Compose vẫn chạy nếu biến đã có default)"
        '''
      }
    }

    stage('Compose Version') {
      steps {
        sh '''
          echo ">>> Using ${COMPOSE_IMAGE}"
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v "${WORKSPACE}:${WORKSPACE}" \
            "${COMPOSE_IMAGE}" version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''
          echo "=== Build với docker-compose (v1) ==="
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v "${WORKSPACE}:${WORKSPACE}" \
            -e COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
            "${COMPOSE_IMAGE}" \
            -f "${WORKSPACE}/docker-compose.yml" \
            --project-directory "${WORKSPACE}" \
            build
        '''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''
          echo "=== Up -d ==="
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v "${WORKSPACE}:${WORKSPACE}" \
            -e COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
            "${COMPOSE_IMAGE}" \
            -f "${WORKSPACE}/docker-compose.yml" \
            --project-directory "${WORKSPACE}" \
            up -d
        '''
      }
    }

    stage('Health Check') {
      steps {
        sh '''
          echo "=== Kiểm tra app ==="
          sleep 10
          # chỉnh lại endpoint/port nếu khác
          curl -fsS http://localhost:8080/actuator/health | tee /dev/stderr | grep -qi '"status":"UP"'
        '''
      }
    }
  }

  post {
    failure {
      echo "❌ Deploy fail — in logs"
      sh '''
        # In logs không cần .env; dùng tuyệt đối & --project-directory
        docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          -v "${WORKSPACE}:${WORKSPACE}" \
          -e COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
          "${COMPOSE_IMAGE}" \
          -f "${WORKSPACE}/docker-compose.yml" \
          --project-directory "${WORKSPACE}" \
          ps
        docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          -v "${WORKSPACE}:${WORKSPACE}" \
          -e COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
          "${COMPOSE_IMAGE}" \
          -f "${WORKSPACE}/docker-compose.yml" \
          --project-directory "${WORKSPACE}" \
          logs --no-color || true
      '''
    }
    success {
      echo "✅ Deploy thành công bằng Docker Compose"
    }
  }
}