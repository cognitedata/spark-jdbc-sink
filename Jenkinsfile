def label = "spark-jdbc-sink-${UUID.randomUUID().toString().substring(0, 5)}"

podTemplate(label: label,
            containers: [containerTemplate(name: 'sbt',
                                           image: 'eu.gcr.io/cognitedata/openjdk-sbt:jdk8-2020-03-20-3631d83',
                                           resourceRequestCpu: '100m',
                                           resourceLimitCpu: '2000m',
                                           resourceRequestMemory: '3000Mi',
                                           resourceLimitMemory: '3000Mi',
                                           ttyEnabled: true,
                                           command: '/bin/cat -')],
            volumes: [secretVolume(secretName: 'sbt-credentials', mountPath: '/sbt-credentials'),
                      secretVolume(secretName: 'jenkins-docker-builder', mountPath: '/jenkins-docker-builder')]) {

    node(label) {
        container('jnlp') {
            stage('Checkout') {
                checkout(scm)
            }
        }
        container('sbt') {
            stage('Install SBT config') {
                sh('mkdir -p /root/.sbt/1.0 && cp /sbt-credentials/credentials.sbt /root/.sbt/1.0/credentials.sbt')
                sh('cp /sbt-credentials/repositories /root/.sbt/')
            }
            stage('Build') {
                sh('sbt package')
            }
            //if (env.BRANCH_NAME == 'master') {
                stage('Deploy') {
                    sh('sbt publish')
                }
            //}
        }
    }
}
