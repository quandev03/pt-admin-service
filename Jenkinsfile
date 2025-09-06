pipeline {
  agent any
  options {
    timestamps()
  }

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
    IMAGE = "${params.REGISTRY}/${params.PROJECT}/${params.APP}"
    TAG   = "${env.BUILD_NUMBER}"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Docker Login') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'harbor-creds',
          usernameVariable: 'HUSER',
          passwordVariable: 'HPASS'
        )]) {
          // Dùng bash + không để dòng trống trước shebang
          sh '''#!/usr/bin/env bash
set -euo pipefail
echo ">> Docker login as: $HUSER"
docker logout "${REGISTRY}" || true
echo "$HPASS" | docker login "${REGISTRY}" -u "$HUSER" --password-stdin
'''
        }
      }
    }

    stage('Buildx Setup') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
export DOCKER_BUILDKIT=1
docker buildx create --name jxbuilder --use || docker buildx use jxbuilder
docker buildx inspect --bootstrap
docker buildx ls
'''
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([
          // Secret file cho Maven settings.xml nếu Dockerfile có --mount=type=secret
          file(credentialsId: 'maven-settings-xml', variable: 'MVN_SETTINGS')
        ]) {
          sh '''#!/usr/bin/env bash
set -euo pipefail
echo ">> Building ${IMAGE}:${TAG} (context=${CONTEXT}, dockerfile=${DOCKERFILE})"

# Build & Push với BuildKit + secret
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
set -euo pipefail
echo ">> Deploy placeholder (tuỳ môi trường của bạn)"
# Ví dụ nếu bạn muốn apply:
# kubectl -n your-namespace set image deploy/your-deploy your-container="${IMAGE}:${TAG}"
# kubectl -n your-namespace rollout status deploy/your-deploy --timeout=120s
'''
      }
    }

    stage('Health Check') {
      when { expression { return params.DO_DEPLOY } }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
echo ">> Health check: ${HEALTH_URL}"
curl -fsSL "${HEALTH_URL}" | tee /dev/stderr | grep -i "UP" >/dev/null
'''
      }
    }
  }

  post {
    success {
      echo "✅ Build thành công: ${IMAGE}:${TAG}"
    }
    failure {
      echo "❌ Build/Pipeline thất bại. Kiểm tra log các stage trước."
    }
  }
}