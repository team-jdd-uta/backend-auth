pipeline {
  agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins-kaniko-agent
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:debug
      command: ["/busybox/cat"]
      tty: true
      env:
        - name: AWS_SDK_LOAD_CONFIG
          value: "true"
    - name: gradle
      image: gradle:8.14.4-jdk21-alpine
      command: ["cat"]
      tty: true
"""
    }
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  parameters {
    string(name: 'IMAGE_TAG', defaultValue: '', description: '비워두면 git short sha를 이미지 태그로 사용합니다.')
    booleanParam(name: 'RUN_TESTS', defaultValue: true, description: '이미지 push 전에 테스트를 실행합니다.')
    booleanParam(name: 'PUSH_IMAGE', defaultValue: true, description: 'main 브랜치에서 ECR 이미지 push 여부')
  }

  environment {
    AWS_ACCOUNT_ID = '881490135253'
    AWS_REGION = 'ap-northeast-2'
    IMAGE_REF = ''
    IMAGE_NAME = 'team9-auth-service'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Prepare') {
      steps {
        script {
          env.RESOLVED_IMAGE_TAG = params.IMAGE_TAG?.trim()
            ? params.IMAGE_TAG.trim()
            : sh(returnStdout: true, script: 'git rev-parse --short=12 HEAD').trim()
          env.ECR_REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
          env.IMAGE_REF = "${env.ECR_REGISTRY}/${env.IMAGE_NAME}:${env.RESOLVED_IMAGE_TAG}"
        }
      }
    }
    stage('Test') {
      when { expression { return params.RUN_TESTS } }
      steps {
        container('gradle') {
          sh './gradlew test'
        }
      }
    }
    stage('PR Image Build Check') {
      when { changeRequest target: 'main' }
      steps {
        container('kaniko') {
          sh '''
            /kaniko/executor \
              --context "$WORKSPACE" \
              --dockerfile "$WORKSPACE/Dockerfile" \
              --custom-platform linux/amd64 \
              --no-push \
              --no-push-cache
          '''
        }
      }
    }
    stage('Main Image Push') {
      when {
        allOf {
          branch 'main'
          expression { return params.PUSH_IMAGE }
        }
      }
      steps {
        container('kaniko') {
          sh '''
            /kaniko/executor \
              --context "$WORKSPACE" \
              --dockerfile "$WORKSPACE/Dockerfile" \
              --custom-platform linux/amd64 \
              --destination "$IMAGE_REF" \
              --destination "$ECR_REGISTRY/$IMAGE_NAME:latest" \
              --cache=false
          '''
        }
      }
    }
  }
}
