pipeline {
  agent {
    kubernetes {
      idleMinutes 5
      yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: contrast
      image: ghcr.io/garage-contrast/contrast-client-go:sha-b931941
      command: ["tail", "-f", "/dev/null"]
      volumeMounts:
      - mountPath: '/shared'
        name: sharedvolume
    - name: buildah
      image: ghcr.io/garage-contrast/jenkinsagent:sha-31195e1
      command: ["tail", "-f", "/dev/null"]
      volumeMounts:
      - name: sharedvolume
        mountPath: '/shared'
      - name: varlibcontainers
        mountPath: '/var/lib/containers'
    
  volumes:
    - name: sharedvolume
      emptyDir: {}
    - name: varlibcontainers
      emptyDir: {}
'''
    }
  }
  stages {
    stage('preamble') {
        steps {
            script {
                openshift.withCluster() {
                    openshift.withProject() {
                        echo "Using project: ${openshift.project()}"
                    }
                }
            }
        }
    }
    stage('Do something') {
      steps {
        sh "echo 'hello world'" 
        sh "whoami"
        sh "ls /etc/subuid && cat /etc/subuid"  
      }
    }
//     stage('Build Contrast Container') {
//       steps {
//         container('buildah') {
//           sh "pwd"
//           sh "ls -lah"
//           sh "buildah --storage-driver=vfs bud --format=oci --layers=true -f ./Dockerfile -t 'test-intermediate' ."
//           sh '''
// cat > Dockerfile.contrast << 'EOF'
// FROM $(params.IMAGE)-intermediate

// RUN mkdir -p /opt/contrast \
// && mvn dependency:copy -Dartifact=com.contrastsecurity:contrast-agent:LATEST -DoutputDirectory=/opt/contrast \
// && mv /opt/contrast/contrast-agent*.jar /opt/contrast/contrast-agent.jar

// ENV JAVA_TOOL_OPTIONS="-javaagent:/opt/contrast/contrast-agent.jar"
// EOF

// cat Dockerfile.contrast
// '''
//           sh "buildah --storage-driver=vfs bud --format=oci --layers=true -f ./Dockerfile.contrast -t 'test-final' ."
//         }
//       }
//     }
    stage('Contrast Config') {
      steps {
        container('contrast') {
          sh "contrast-metadata -h"   
        }
      }
    }
    stage('Customize Deployment') {
      steps {
        container('contrast') {
          sh "contrast-metadata -h"   
        }
      }
    }
    stage('Deploy Application') {
      steps {
        container('contrast') {
          sh "contrast-metadata -h"   
        }
      }
    }
    stage('preamble') {
        steps {
            script {
                openshift.withCluster() {
                    openshift.withProject() {
                        echo "Using project: ${openshift.project()}"
                    }
                }
            }
        }
    }
    stage('build') {
      steps {
        script {
            openshift.withCluster() {
                openshift.withProject() {
                  def buildconfig = openshift.apply('''
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: intermed-build
  namespace: my-test-project
  labels:
    name: intermed-build
spec:
  source:
    type: Git
    git:
      uri: 'https://github.com/Garage-Contrast/vulnerable-spring-boot-application.git'
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: Dockerfile
  output:
    to:
      kind: ImageStreamTag
      name: 'java-app:intermediate'
''')
                  // def builds = openshift.selector("bc", templateName).related('builds')
                  // timeout(5) { 
                  //   builds.untilEach(1) {
                  //     return (it.object().status.phase == "Complete")
                  //   }
                  // }
                }
            }
        }
      }
    }
    // stage('deploy') {
    //   steps {
    //     script {
    //         openshift.withCluster() {
    //             openshift.withProject() {
    //               def rm = openshift.selector("dc", templateName).rollout()
    //               timeout(5) { 
    //                 openshift.selector("dc", templateName).related('pods').untilEach(1) {
    //                   return (it.object().status.phase == "Running")
    //                 }
    //               }
    //             }
    //         }
    //     }
    //   }
    // }
    // stage('tag') {
    //   steps {
    //     script {
    //         openshift.withCluster() {
    //             openshift.withProject() {
    //               openshift.tag("${templateName}:latest", "${templateName}-staging:latest") 
    //             }
    //         }
    //     }
    //   }
    // }
  }
}
