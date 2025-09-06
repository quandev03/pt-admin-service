pipeline {
  agent any
  options { timestamps() }
  environment { COMPOSE_PROJECT_NAME = 'pt-admin' }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'develop', url: 'https://github.com/quandev03/pt-admin-service.git'
      }
    }

    stage('Compose Version') {
      steps {
        sh '''#!/bin/sh
set -e
if docker compose version >/dev/null 2>&1; then
  echo ">>> Using host docker compose"
  docker compose version
  echo "HOST_COMPOSE=1" > .compose_env
else
  echo ">>> Using containerized docker/compose:latest"
  WORKDIR="$(pwd -P)"
  COMPOSE="docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} docker/compose:latest"
  echo "$COMPOSE version:"
  $COMPOSE version
  echo "HOST_COMPOSE=0" > .compose_env
fi
'''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''#!/bin/sh
set -e
. ./.compose_env || true
if [ "$HOST_COMPOSE" = "1" ]; then
  docker compose build
else
  WORKDIR="$(pwd -P)"
  COMPOSE="docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} docker/compose:latest"
  $COMPOSE build
fi
'''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''#!/bin/sh
set -e
. ./.compose_env || true
if [ "$HOST_COMPOSE" = "1" ]; then
  docker compose up -d
else
  WORKDIR="$(pwd -P)"
  COMPOSE="docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} docker/compose:latest"
  $COMPOSE up -d
fi
'''
      }
    }

    stage('Health Check') {
      steps {
        sh '''#!/bin/sh
set -e
echo "=== Kiểm tra app ==="
sleep 10
curl -fsS http://localhost:8080/actuator/health > /dev/null
'''
      }
    }
  }

  post {
    success { echo '✅ Deploy thành công bằng Docker Compose' }
    failure {
      echo '❌ Deploy thất bại, in logs docker-compose'
      sh '''#!/bin/sh
. ./.compose_env || true
if [ "$HOST_COMPOSE" = "1" ]; then
  docker compose logs app || true
else
  WORKDIR="$(pwd -P)"
  COMPOSE="docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} docker/compose:latest"
  $COMPOSE logs app || true
fi
'''
    }
  }
}