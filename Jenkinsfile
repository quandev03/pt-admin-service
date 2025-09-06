pipeline {
  agent any
  options { timestamps() }

  environment {
    COMPOSE_PROJECT_NAME = 'pt-admin'
    COMPOSE_IMG = 'docker/compose:1.29.2'
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
      }
    }

    stage('Prepare .env') {
      steps {
        script {
          env.ENV_FROM_CRED = 'false'
          if (fileExists('.env')) {
            echo "[i] Found .env in repo -> dùng file này."
          } else {
            withCredentials([string(credentialsId: 'pt-admin-env', variable: 'ENV_CONTENT')]) {
              writeFile file: '.env', text: ENV_CONTENT
              env.ENV_FROM_CRED = 'true'
              echo "[i] Viết .env từ Jenkins credentials."
            }
          }
          sh '''
            echo "== LS WORKSPACE =="
            ls -la
            echo "== HEAD .env (ẩn nội dung) =="
            wc -c .env || true
          '''
        }
      }
    }


    stage('Compose Version') {
      steps {
        sh '''
          echo ">>> Using ${COMPOSE_IMG}"
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            ${COMPOSE_IMG} version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh """
          echo '=== Build với docker-compose (v1) ==='
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMAGE} \
            -f docker-compose.yml \
            build
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
            ${COMPOSE_IMAGE} \
            -f docker-compose.yml \
            up -d
        """
      }
    }


    stage('Health Check') {
      steps {
        sh '''
          echo "=== Health check ==="
          sleep 10
          curl -sf http://localhost:8080/actuator/health | grep -qi '"status":"UP"'
        '''
      }
    }
  }


post {
  failure {
    echo '❌ Deploy fail — in logs'
    sh """
      docker run --rm \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
        -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
        ${COMPOSE_IMAGE} \
        -f docker-compose.yml \
        logs --no-color || true
    """
  }
}
}