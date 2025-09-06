pipeline {
  agent any
  options { timestamps() }
  environment {
    COMPOSE_PROJECT_NAME = 'pt-admin'
  }

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
          WORKDIR="$(pwd -P)"
          COMPOSE="docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} \
            docker/compose:2.29.7"
          echo "=== Compose version ==="
          $COMPOSE version
        '''
      }
    }

    stage('Docker Compose Build') {
      steps {
        sh '''#!/bin/sh
          set -e
          WORKDIR="$(pwd -P)"
          COMPOSE="docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} \
            docker/compose:2.29.7"
          echo "=== Build bằng docker-compose (container) ==="
          $COMPOSE build
        '''
      }
    }

    stage('Docker Compose Up') {
      steps {
        sh '''#!/bin/sh
          set -e
          WORKDIR="$(pwd -P)"
          COMPOSE="docker run --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} \
            docker/compose:2.29.7"
          echo "=== Up -d ==="
          $COMPOSE up -d
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
    success {
      echo '✅ Deploy thành công bằng Docker Compose (container)'
    }
    failure {
      echo '❌ Deploy thất bại, in logs docker-compose'
      sh '''#!/bin/sh
        WORKDIR="$(pwd -P)"
        COMPOSE="docker run --rm \
          -v /var/run/docker.sock:/var/run/docker.sock \
          -v ${WORKDIR}:${WORKDIR} -w ${WORKDIR} \
          docker/compose:2.29.7"
        $COMPOSE logs --no-color app || true
      '''
    }
  }
}