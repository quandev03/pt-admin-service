pipeline {
  agent any
  options { timestamps() }

  parameters {
    string(name: 'REGISTRY',    defaultValue: 'harbor.vissoft.vn', description: 'Docker registry')
    string(name: 'PROJECT',     defaultValue: 'vnsky',             description: 'Harbor project')
    string(name: 'APP',         defaultValue: 'hvn-admin-service', description: 'Image/app name')
    string(name: 'DOCKERFILE',  defaultValue: 'Dockerfile',        description: 'Path to Dockerfile')
    string(name: 'CONTEXT',     defaultValue: '.',                 description: 'Docker build context')
    booleanParam(name: 'DO_DEPLOY', defaultValue: false,           description: 'Chạy bước deploy?')
    string(name: 'HEALTH_URL',  defaultValue: 'http://my-service:8080/actuator/health', description: 'URL health check')
  }

  environment {
    // Map params -> env (để shell dùng được)
    REGISTRY   = "${params.REGISTRY}"
    PROJECT    = "${params.PROJECT}"
    APP        = "${params.APP}"
    DOCKERFILE = "${params.DOCKERFILE}"
    CONTEXT    = "${params.CONTEXT}"

    IMAGE = "${params.REGISTRY}/${params.PROJECT}/${params.APP}"
    TAG   = "${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Docker Login') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'harbor-creds', usernameVariable: 'HUSER', passwordVariable: 'HPASS')]) {
          sh '''#!/usr/bin/env bash
set -eo pipefail   # bỏ -u để tránh fail khi env chưa map
echo ">> Docker login as: $HUSER"
docker logout "$REGISTRY" || true
echo "$HPASS" | docker login "$REGISTRY" -u "$HUSER" --password-stdin
'''
        }
      }
    }

    stage('Buildx Setup') {
      steps {
        sh '''#!/usr/bin/env bash
set -eo pipefail
export DOCKER_BUILDKIT=1
docker buildx create --name jxbuilder --use || docker buildx use jxbuilder
docker buildx inspect --bootstrap
docker buildx ls
'''
      }
    }

    stage('Docker Build & Push') {
      steps {
        // Nếu không dùng secret maven settings thì bỏ whole withCredentials() này, giữ nguyên phần sh bên trong
        withCredentials([file(credentialsId: 'maven-settings-xml', variable: 'MVN_SETTINGS')]) {
          sh '''#!/usr/bin/env bash
set -eo pipefail
echo ">> Building ${IMAGE}:${TAG}  (context=${CONTEXT}, dockerfile=${DOCKERFILE})"

docker buildx build \
  --file "${DOCKERFILE}" \
  --secret id=mvnsettings,src="${MVN_SETTINGS}" \
  -t "${IMAGE}:${TAG}" \
  -t "${IMAGE}:latest" \
  --push \
  "${CONTEXT}"
'''
        }
      }
    }

    stage('Deploy') {
      when { expression { return params.DO_DEPLOY } }
      steps {
        sh '''#!/usr/bin/env bash
set -eo pipefail
echo ">> Deploy placeholder..."
# ví dụ:
# kubectl -n your-ns set image deploy/your-deploy your-container="${IMAGE}:${TAG}"
# kubectl -n your-ns rollout status deploy/your-deploy --timeout=120s
'''
      }
    }

    stage('Health Check') {
      when { expression { return params.DO_DEPLOY } }
      steps {
        sh '''#!/usr/bin/env bash
set -eo pipefail
echo ">> Health check: ${HEALTH_URL}"
curl -fsSL "${HEALTH_URL}" | tee /dev/stderr | grep -i "UP" >/dev/null
'''
      }
    }
  }

  post {
    success { echo "✅ Build OK: ${IMAGE}:${TAG}" }
    failure { echo "❌ Pipeline failed — xem log các stage trên." }
  }
}