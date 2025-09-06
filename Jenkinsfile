pipeline {
  agent any

  environment {
    // đặt tên stack cho dễ lọc container/network
    COMPOSE_PROJECT_NAME = 'pt-admin'
    // dùng compose v1 ổn định từ official image
    COMPOSE_IMG = 'docker/compose:1.29.2'
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
      }
    }

    stage('Compose Version') {
      steps {
        sh '''
          echo ">>> Using containerized ${COMPOSE_IMG}"
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            ${COMPOSE_IMG} version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''
          echo "=== Build với docker-compose (v1) ==="
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            --env COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMG} \
            -f ${WORKSPACE}/docker-compose.yml \
            --env-file ${WORKSPACE}/.env \
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
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            --env COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMG} \
            -f ${WORKSPACE}/docker-compose.yml \
            --env-file ${WORKSPACE}/.env \
            up -d --force-recreate
        '''
      }
    }

    stage('Health Check') {
      steps {
        sh '''
          echo "=== Health check ==="
          # cho app có thời gian khởi động
          sleep 10
          curl -sf http://localhost:8080/actuator/health | grep -qi '"status":"UP"'
        '''
      }
    }
  }

  post {
    success {
      echo '✅ Deploy thành công bằng docker-compose (containerized)'
    }
    failure {
      echo '❌ Deploy thất bại — in logs'
      sh '''
        docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
          --env COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
          ${COMPOSE_IMG} \
          -f ${WORKSPACE}/docker-compose.yml \
          --env-file ${WORKSPACE}/.env \
          logs --no-color || true
      '''
    }
  }
}