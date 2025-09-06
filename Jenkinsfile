pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

      stage('Docker Build & Push') {
        steps {
          withCredentials([usernamePassword(
            credentialsId: 'harbor-creds',
            usernameVariable: 'HUSER',
            passwordVariable: 'HPASS'
          )]) {
            sh '''
              #!/usr/bin/env bash
              set -euo pipefail

              echo ">> Using Harbor user: ${HUSER}"   # username KHÔNG bị mask nên thấy được
              docker logout harbor.vissoft.vn || true
              echo "$HPASS" | docker login harbor.vissoft.vn -u "$HUSER" --password-stdin

              # ví dụ build/push
              DOCKER_BUILDKIT=1 docker build -t harbor.vissoft.vn/vnsky/hvn-admin-service:${BUILD_NUMBER} .
              docker push harbor.vissoft.vn/vnsky/hvn-admin-service:${BUILD_NUMBER}
            '''
          }
        }
      }

        stage('Deploy') {
            steps {
                sh '''
                    echo "=== Deploy app ==="
                    # ví dụ apply config map, secret, manifest
                    # kubectl apply -f k8s/
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    echo "=== Health Check ==="
                    curl -f http://my-service:8081/actuator/health || exit 1
                '''
            }
        }
    }
}