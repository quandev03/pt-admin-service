stage('Deploy (kubectl apply + .env → ConfigMap/Secret)') {
  steps {
    sh '''
      set -e
      # .env phải tồn tại
      test -f .env || { echo ".env not found"; exit 1; }

      # Namespace
      kubectl get ns ${K8S_NAMESPACE} >/dev/null 2>&1 || kubectl create ns ${K8S_NAMESPACE}

      # ConfigMap từ biến KHÔNG bắt đầu SECRET_
      grep -v '^SECRET_' .env | kubectl -n ${K8S_NAMESPACE} create configmap ${RELEASE_NAME}-env \
        --from-env-file=/dev/stdin --dry-run=client -o yaml | kubectl apply -f -

      # Secret từ biến BẮT ĐẦU SECRET_ (bỏ tiền tố SECRET_)
      if grep -q '^SECRET_' .env; then
        grep '^SECRET_' .env | sed 's/^SECRET_//' | \
          kubectl -n ${K8S_NAMESPACE} create secret generic ${RELEASE_NAME}-secret \
          --from-env-file=/dev/stdin --dry-run=client -o yaml | kubectl apply -f -
      else
        kubectl -n ${K8S_NAMESPACE} get secret ${RELEASE_NAME}-secret >/dev/null 2>&1 || true
      fi

      # Patch image tag & apply
      sed "s#ghcr.io/quandev03/${APP_NAME}:latest#${IMAGE}:${IMAGE_TAG}#g" k8s/deployment.yaml | kubectl apply -f -
      kubectl apply -f k8s/service.yaml

      # Rollout
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