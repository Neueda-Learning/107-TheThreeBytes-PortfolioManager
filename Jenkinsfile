pipeline {
    agent any

    environment {
        // Image name used when building/referencing the backend Docker image
        IMAGE_NAME   = "portfolio-backend"
        COMPOSE_FILE = "docker-compose.yml"
    }

    stages {

        // ── 1. Checkout ────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ── 2. Build JAR with Maven ────────────────────────────────────────
        stage('Maven Build') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -DskipTests -B'
                }
            }
        }

        // ── 3. Build Docker image ──────────────────────────────────────────
        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:latest ./backend"
            }
        }

        // ── 4. Tear down any existing deployment ──────────────────────────
        stage('Stop Existing Deployment') {
            steps {
                script {
                    // --remove-orphans cleans up containers from old service names
                    sh "docker compose -f ${COMPOSE_FILE} down --remove-orphans || true"
                }
            }
        }

        // ── 5. Deploy with Docker Compose ─────────────────────────────────
        stage('Deploy') {
            steps {
                sh "docker compose -f ${COMPOSE_FILE} up -d --build"
            }
        }

        // ── 6. Prune dangling images ───────────────────────────────────────
        stage('Cleanup') {
            steps {
                sh "docker image prune -f"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful – backend running on http://localhost:8080"
        }
        failure {
            echo "❌ Pipeline failed. Check the logs above for details."
            // Roll back: bring compose down so a broken stack isn't left running
            sh "docker compose -f ${COMPOSE_FILE} down --remove-orphans || true"
        }
    }
}

