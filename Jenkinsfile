pipeline {
    agent any

    environment {
        GIT_URL = 'https://github.com/Neueda-Learning/107-TheThreeBytes-PortfolioManager.git'
        BRANCH = 'ci-cd'
        COMPOSE_FILE = "docker-compose.yml"
    }

    stages {
        stage('Checkout Source') {
            steps {
                git branch: "${BRANCH}",
                    url: "${GIT_URL}"
            }
        }

        stage('Stop Existing Containers') {
            steps {
                sh "docker-compose -f ${COMPOSE_FILE} down || true"
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker-compose -f ${COMPOSE_FILE} build --no-cache"
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker-compose down || true
                    docker rm -f portfolio_mysql portfolio_backend || true
                    docker-compose up -d
                '''
            }
        }

        stage('Verify') {
            steps {
                sh 'docker ps'
            }
        }
    }
}
