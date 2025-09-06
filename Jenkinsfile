pipeline {
  agent any

  // chỉ giữ timestamps
  options { timestamps() }

  environment {
    K8S_NAMESPACE   = 'prod'
    RELEASE_NAME    = 'hvn-admin'
    HEALTH_PATH     = '/actuator/health'
    APP_NAME        = 'hvn-admin-service'
    DOCKER_REGISTRY = 'harbor.vissoft.vn'
    DOCKER_REPO     = 'vnsky'
    IMAGE           = "${DOCKER_REGISTRY}/${DOCKER_REPO}/${APP_NAME}"
    IMAGE_TAG       = "${env.BUILD_NUMBER}"
  }

  stages {
    stage('Docker Build & Push (BuildKit + secret)') {
      steps {
        withCredentials([
          usernamePassword(credentialsId: 'harbor-creds', usernameVariable: 'HUSER', passwordVariable: 'HPASS'),
          file(credentialsId: 'maven-settings-xml', variable: 'MVN_SETTINGS')
        ]) {
          sh '''
            set -e

            # login registry (để pull base private & push)
            echo "$HPASS" | docker login harbor.vissoft.vn -u "$HUSER" --password-stdin

            # Bật BuildKit & chuẩn bị builder
            export DOCKER_BUILDKIT=1
            docker buildx create --name jxbuilder --use || docker buildx use jxbuilder
            docker buildx inspect --bootstrap

            # Build & PUSH (truyền settings.xml qua secret, KHÔNG ghi vào image)
            docker buildx build \
              --secret id=mvnsettings,src=${MVN_SETTINGS} \
              -t harbor.vissoft.vn/vnsky/hvn-admin-service:${BUILD_NUMBER} \
              -t harbor.vissoft.vn/vnsky/hvn-admin-service:latest \
              --push \
              .
          '''
        }
      }
    }

    stage('Deploy (kubectl apply + .env → ConfigMap/Secret)') {
      steps {
        sh '''
          set -e
          test -f .env || { echo ".env not found"; exit 1; }

          kubectl get ns ${K8S_NAMESPACE} >/dev/null 2>&1 || kubectl create ns ${K8S_NAMESPACE}

          grep -v '^SECRET_' .env | kubectl -n ${K8S_NAMESPACE} create configmap ${RELEASE_NAME}-env \
            --from-env-file=/dev/stdin --dry-run=client -o yaml | kubectl apply -f -

          if grep -q '^SECRET_' .env; then
            grep '^SECRET_' .env | sed 's/^SECRET_//' | \
              kubectl -n ${K8S_NAMESPACE} create secret generic ${RELEASE_NAME}-secret \
              --from-env-file=/dev/stdin --dry-run=client -o yaml | kubectl apply -f -
          fi

          sed "s#ghcr.io/quandev03/${APP_NAME}:latest#${IMAGE}:${IMAGE_TAG}#g" k8s/deployment.yaml | kubectl apply -f -
          kubectl apply -f k8s/service.yaml
          kubectl -n ${K8S_NAMESPACE} rollout status deploy/${RELEASE_NAME} --timeout=180s
        '''
      }
    }

    stage('Health Check') {
      steps {
        sh '''
          set -e
          SVC=${RELEASE_NAME}
          PORT=$(kubectl -n ${K8S_NAMESPACE} get svc $SVC -o jsonpath='{.spec.ports[0].port}')
          CLUSTER_IP=$(kubectl -n ${K8S_NAMESPACE} get svc $SVC -o jsonpath='{.spec.clusterIP}')
          echo "Health check http://${CLUSTER_IP}:${PORT}${HEALTH_PATH}"
          for i in $(seq 1 20); do
            code=$(curl -m 3 -s -o /dev/null -w "%{http_code}" "http://${CLUSTER_IP}:${PORT}${HEALTH_PATH}" || true)
            [ "$code" = "200" ] && { echo "Healthy"; exit 0; }
            echo "Attempt $i/20 => $code"; sleep 3
          done
          echo "Health check failed"; exit 1
        '''
      }
    }
  }
}