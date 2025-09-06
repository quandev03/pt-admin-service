pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "pt-admin"
    }

    stage('Docker Compose Build') {
      steps {
        sh '''
          set -e
          COMPOSE="docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v $PWD:$PWD -w $PWD \
            docker/compose:2.29.7"

          echo "=== Build bằng docker-compose (container) ==="
          $COMPOSE version
          $COMPOSE build
        '''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''
          set -e
          COMPOSE="docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v $PWD:$PWD -w $PWD \
            docker/compose:2.29.7"

          echo "=== Up -d ==="
          $COMPOSE up -d
        '''
      }
    }

    post {
      failure {
        sh '''
          COMPOSE="docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v $PWD:$PWD -w $PWD \
            docker/compose:2.29.7"
          $COMPOSE logs app || true
        '''
      }
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
            }
        }

        stage('Docker Compose Build') {
            steps {
                sh '''
                  echo "=== Build bằng docker-compose ==="
                  docker compose build
                '''
            }
        }

        stage('Docker Compose Up') {
            steps {
                sh '''
                  echo "=== Run container với docker-compose ==="
                  docker compose up -d
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                  echo "=== Kiểm tra app ==="
                  sleep 10
                  curl -f http://localhost:8080/actuator/health || exit 1
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Deploy thành công bằng Docker Compose"
        }
        failure {
            echo "❌ Deploy thất bại, xem log Jenkins"
            sh "docker compose logs app || true"
        }
    }
}