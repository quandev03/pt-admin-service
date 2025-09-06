pipeline {
  agent any

  options { timestamps(); ansiColor('xterm') }

  environment {
    K8S_NAMESPACE = 'dev'
    RELEASE_NAME  = 'hvn-admin'
    HEALTH_PATH   = '/actuator/health'
    APP_NAME      = 'hvn-admin-service'
    DOCKER_REGISTRY = 'harbor.vissoft.vn'
    DOCKER_REPO     = 'vnsky'
    IMAGE           = "${DOCKER_REGISTRY}/${DOCKER_REPO}/${APP_NAME}"
    IMAGE_TAG       = "${env.BUILD_NUMBER}"
  }

  stages {
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
  }
}