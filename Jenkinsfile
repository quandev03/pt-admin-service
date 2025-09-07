environment {
  COMPOSE_BIN = '/usr/local/bin/docker-compose'
  COMPOSE_VER = '1.29.2'   // ổn định cho môi trường server
}

stages {
  stage('Compose Install') {
    steps {
      sh '''
        set -e
        if ! ${COMPOSE_BIN} version >/dev/null 2>&1; then
          echo ">> Install docker-compose ${COMPOSE_VER}"
          (curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VER}/docker-compose-$(uname -s)-$(uname -m)" -o ${COMPOSE_BIN} \
            || wget -qO ${COMPOSE_BIN} "https://github.com/docker/compose/releases/download/${COMPOSE_VER}/docker-compose-$(uname -s)-$(uname -m)")
          chmod +x ${COMPOSE_BIN}
        fi
        ${COMPOSE_BIN} version
      '''
    }
  }

  stage('Docker Compose Build') {
    steps {
      sh '''
        set -e
        echo "== LS =="
        ls -la
        test -f docker-compose.yml
        test -f .env

        echo "=== Build với docker-compose ==="
        ${COMPOSE_BIN} -f docker-compose.yml --env-file .env build
      '''
    }
  }

  stage('Docker Compose Up') {
    steps {
      sh '''
        set -e
        echo "=== Up -d ==="
        ${COMPOSE_BIN} -f docker-compose.yml --env-file .env up -d

        echo "=== PS ==="
        ${COMPOSE_BIN} -f docker-compose.yml ps
      '''
    }
  }
}

post {
  failure {
    echo "❌ Deploy fail — in logs"
    sh '''
      ${COMPOSE_BIN} -f docker-compose.yml --env-file .env logs --no-color || true
    '''
  }
}