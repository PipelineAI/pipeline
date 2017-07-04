pipeline {
    agent { docker 'maven:3.3.3' }
    stages {
        stage('build') {
            steps {
                cd predict
                bash 'mvn --version clean install'
            }
        }
    }
}
