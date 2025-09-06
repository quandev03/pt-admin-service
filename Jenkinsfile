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
        sh '''
          echo "=== Build với docker-compose (v1) ==="
          docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
            -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMG} \
            -f docker-compose.yml --env-file .env build
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
            -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
            ${COMPOSE_IMG} \
            -f docker-compose.yml --env-file .env up -d --force-recreate
        '''
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
    success { echo '✅ Deploy OK với docker-compose' }
    failure {
      echo '❌ Deploy fail — in logs'
      sh '''
        docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          -v ${WORKSPACE}:${WORKSPACE} -w ${WORKSPACE} \
          -e COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME} \
          ${COMPOSE_IMG} \
          -f docker-compose.yml --env-file .env logs --no-color || true
      '''
    }
    always {
      // dọn dẹp file .env sinh ra (tuỳ chọn)
      sh 'shred -u .env || true'
    }
  }
}