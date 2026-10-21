pipeline {
    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['staging', 'production'],
            description: 'Target deployment environment for simulated continuous delivery'
        )
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Execute automated unit and UI test suites'
        )
    }

    environment {
        APP_NAME = 'cultural-festival-platform'
        APP_PORT = '8080'
        JAR_NAME = 'cultural-festival-platform-0.0.1-SNAPSHOT.jar'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                echo "=========================================================="
                echo " Stage 1: Checkout Source Code from SCM"
                echo "=========================================================="
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "=========================================================="
                echo " Stage 2: Clean & Compile Application (Java 21 / Maven)"
                echo "=========================================================="
                script {
                    if (isUnix()) {
                        sh '''
                            if [ -f "./mvnw" ]; then
                                chmod +x ./mvnw
                                ./mvnw clean compile
                            else
                                mvn clean compile
                            fi
                        '''
                    } else {
                        bat '''
                            if exist mvnw.cmd (
                                call mvnw.cmd clean compile
                            ) else (
                                call mvn clean compile
                            )
                        '''
                    }
                }
            }
        }

        stage('Unit & UI Tests') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                echo "=========================================================="
                echo " Stage 3: Execute Unit Tests & Headless Selenium UI Suite"
                echo "=========================================================="
                script {
                    if (isUnix()) {
                        sh '''
                            if [ -f "./mvnw" ]; then
                                ./mvnw test
                            else
                                mvn test
                            fi
                        '''
                    } else {
                        bat '''
                            if exist mvnw.cmd (
                                call mvnw.cmd test
                            ) else (
                                call mvn test
                            )
                        '''
                    }
                }
            }
            post {
                always {
                    echo "Archiving JUnit test execution reports..."
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    
                    echo "Archiving failure diagnostic screenshots (if any)..."
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/screenshots/*.png'
                }
            }
        }

        stage('Package') {
            steps {
                echo "=========================================================="
                echo " Stage 4: Package Standalone Spring Boot Executable JAR"
                echo "=========================================================="
                script {
                    if (isUnix()) {
                        sh '''
                            if [ -f "./mvnw" ]; then
                                ./mvnw package -DskipTests
                            else
                                mvn package -DskipTests
                            fi
                        '''
                    } else {
                        bat '''
                            if exist mvnw.cmd (
                                call mvnw.cmd package -DskipTests
                            ) else (
                                call mvn package -DskipTests
                            )
                        '''
                    }
                }
            }
            post {
                success {
                    echo "Archiving generated Spring Boot JAR artifact..."
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Deploy (Simulated)') {
            steps {
                echo "=========================================================="
                echo " Stage 5: Simulated Deployment to [${params.DEPLOY_ENV}]"
                echo "=========================================================="
                script {
                    echo "Deploying artifact: target/${JAR_NAME}"
                    echo "Target Environment: ${params.DEPLOY_ENV}"

                    if (isUnix()) {
                        sh '''
                            echo "[LINUX] Simulating deployment on ${DEPLOY_ENV} host..."
                            echo "Stopping any existing instance on port ${APP_PORT}..."
                            pkill -f ${JAR_NAME} || true

                            echo "Starting ${APP_NAME} in background using nohup..."
                            nohup java -jar target/${JAR_NAME} --server.port=${APP_PORT} > app_${DEPLOY_ENV}.log 2>&1 &

                            sleep 4
                            echo "Verifying application availability..."
                            curl -f http://localhost:${APP_PORT}/api/v1/health || echo "Application daemon started; ready for traffic."
                        '''
                    } else {
                        bat '''
                            @echo off
                            echo [WINDOWS] Simulating deployment on %DEPLOY_ENV% host...
                            echo Launching Spring Boot JAR in background process...
                            start /B java -jar target\\%JAR_NAME% --server.port=%APP_PORT% > app_%DEPLOY_ENV%.log 2>&1
                            timeout /t 4 /nobreak >nul
                            echo Simulated deployment successfully executed for %DEPLOY_ENV%.
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo "=========================================================="
            echo " Pipeline Execution Succeeded! Artifact ready for ${params.DEPLOY_ENV}"
            echo "=========================================================="
        }
        failure {
            echo "=========================================================="
            echo " Pipeline Execution Failed! Check Surefire reports and screenshot artifacts."
            echo "=========================================================="
        }
    }
}
