pipeline {
    agent any

    environment {
        COMPOSE_CMD = "/usr/local/bin/docker-compose"
        COMPOSE_FILE = "docker-compose.yml"
        ENV_FILE     = ".env"
        SETTINGS_XML = "settings-vnsky.xml"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop',

                    url: 'https://github.com/quandev03/pt-admin-service.git'
            }
        }

        stage('Verify files') {
            steps {
                sh '''
                  echo ">>> Workspace: $(pwd)"
                  ls -la
                  test -f ${COMPOSE_FILE} || (echo "❌ Missing docker-compose.yml" && exit 1)
                  test -f ${ENV_FILE} || (echo "❌ Missing .env" && exit 1)
                  test -f ${SETTINGS_XML} || (echo "❌ Missing settings.xml" && exit 1)
                '''
            }
        }

        stage('Docker Compose Up') {
            steps {
                sh '''
                  echo "=== Compose build & up ==="
                  ${COMPOSE_CMD} -f ${COMPOSE_FILE} --env-file ${ENV_FILE} up -d --build
                '''
            }
        }

        stage('Check status') {
            steps {
                sh '''
                  echo "=== Containers status ==="
                  docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
                '''
            }
        }
    }

    post {
        always {
            sh '''
              echo "=== docker-compose logs ==="
              ${COMPOSE_CMD} -f ${COMPOSE_FILE} --env-file ${ENV_FILE} logs --tail=50
            '''
        }
    }
}