pipeline {
  agent any
  options { timestamps() }

  environment {
    COMPOSE_IMAGE       = 'docker/compose:1.29.2'   // <-- thêm biến này
    COMPOSE_PROJECT_NAME = 'pt-admin'
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
      }
    }

    stage('Prepare .env') {
      steps {
        sh '''
          echo "== LS WORKSPACE =="
          ls -la
          # Nếu .env có CRLF thì chuẩn hóa sang LF (không bắt buộc)
          [ -f .env ] && sed -i 's/\r$//' .env || true
        '''
      }
    }

    stage('Compose Version') {
      steps {
        sh """
          echo '>>> Using ${COMPOSE_IMAGE}'
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            ${COMPOSE_IMAGE} version
        """
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh """
          echo '=== Build với docker-compose ==='
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMAGE} -f docker-compose.yml build
        """
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh """
          echo '=== Up -d ==='
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMAGE} -f docker-compose.yml up -d
        """
      }
    }

    stage('Health Check') {
      steps {
        sh 'sleep 10; curl -f http://localhost:8080/actuator/health'
      }
    }
  }

  post {
    success { echo '✅ Deploy OK' }
    failure {
      echo '❌ Deploy fail — in logs'
      sh """
        docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
          -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
          ${COMPOSE_IMAGE} -f docker-compose.yml logs --no-color || true
      """
    }
  }
}