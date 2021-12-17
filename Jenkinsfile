pipeline {
  agent {
    kubernetes {
      idleMinutes 5
      yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
    - name: contrast
      image: ghcr.io/garage-contrast/contrast-client-go:sha-b931941
      command: ["tail", "-f", "/dev/null"]
      volumeMounts:
      - mountPath: '/shared'
        name: sharedvolume
    - name: openshift
      image: registry.redhat.io/openshift4/ose-jenkins-agent-base:latest
      command: ["tail", "-f", "/dev/null"]
  volumes:
    - name: sharedvolume
      emptyDir: {}
'''
    }
  }
  environment {
    APP_NAME = 'vulnerable-spring-boot-app'
    PROJECT = 'my-test-project'
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
    // stage('Contrast Config') {
    //   steps {
    //     container('contrast') {
    //       sh "contrast-metadata -h"   
    //     }
    //   }
    // }
    // stage('Customize Deployment') {
    //   steps {
    //     container('contrast') {
    //       sh "contrast-metadata -h"   
    //     }
    //   }
    // }
    // stage('Deploy Application') {
    //   steps {
    //     container('contrast') {
    //       sh "contrast-metadata -h"   
    //     }
    //   }
    // }
    stage('build') {
      steps {
        sh '''
cat > buildconfig-template.yaml << 'EOF'
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: ${APP_NAME}-intermed
spec:
  source:
    type: Git
    git:
      uri: $GIT_URL
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: Dockerfile
  output:
    to:
      kind: ImageStreamTag
      name: ${APP_NAME}:intermediate
---
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: $APP_NAME
spec:
  lookupPolicy:
    local: true
---
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: ${APP_NAME}-contrast
spec:
  source:
    dockerfile: |
      FROM please/replace/this:latest

      RUN mkdir -p /opt/contrast \
      && mvn dependency:copy -Dartifact=com.contrastsecurity:contrast-agent:LATEST -DoutputDirectory=/opt/contrast \
      && mv /opt/contrast/contrast-agent*.jar /opt/contrast/contrast-agent.jar
      
      ENV JAVA_TOOL_OPTIONS="-javaagent:/opt/contrast/contrast-agent.jar"
  strategy:
    type: Docker
    dockerStrategy:
      from:
        kind: ImageStreamTag
        name: ${APP_NAME}:intermediate
  output:
    to:
      kind: ImageStreamTag
      name: ${APP_NAME}:latest
  triggers:
    - type: ImageChange
      imageChange: {}
EOF
ls -lh
pwd
'''
        container('openshift') {
          sh "APP_NAME=${env.APP_NAME} GIT_URL=${env.GIT_URL} envsubst '\$APP_NAME,\$GIT_URL' < \"buildconfig-template.yaml\" > \"buildconfig.yaml\""
        }
        container('openshift') {
          script {
            openshift.withCluster() {
                openshift.withProject() {
                  def buildconfig = openshift.apply(readFile( './buildconfig.yaml' ))
                }
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
