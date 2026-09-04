pipeline {
  agent any
  options { timestamps(); disableConcurrentBuilds(); timeout(time: 30, unit: 'MINUTES'); skipDefaultCheckout(true) }
  environment { REGISTRY = 'registry.example.com/platform' }
  stages {
    stage('Checkout') { steps {
      checkout scm
      script { env.IMAGE_TAG = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim() }
    } }
    stage('Test') { steps { sh 'mvn -B verify' } }
    stage('Build images') { steps {
      sh 'docker build -f gateway-service/Dockerfile -t $REGISTRY/gateway-service:$IMAGE_TAG .'
      sh 'docker build -f order-service/Dockerfile -t $REGISTRY/order-service:$IMAGE_TAG .'
    } }
    stage('Security scan') { steps {
      sh 'trivy image --exit-code 1 --severity CRITICAL $REGISTRY/gateway-service:$IMAGE_TAG'
      sh 'trivy image --exit-code 1 --severity CRITICAL $REGISTRY/order-service:$IMAGE_TAG'
    } }
    stage('Push') { when { branch 'main' }; steps { withCredentials([usernamePassword(credentialsId: 'container-registry', usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
      sh 'printf %s "$REG_PASS" | docker login registry.example.com -u "$REG_USER" --password-stdin'
      sh 'docker push $REGISTRY/gateway-service:$IMAGE_TAG && docker push $REGISTRY/order-service:$IMAGE_TAG'
    } } }
    stage('Validate Helm') { steps { sh 'helm lint deploy/helm/platform && helm template platform deploy/helm/platform >/dev/null' } }
    stage('Update GitOps') { when { branch 'main' }; steps {
      echo 'Update values image tags in a separate GitOps repository, commit, and let Argo CD reconcile. Add repository credentials before enabling.'
    } }
  }
  post { always { junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'; deleteDir() } }
}
