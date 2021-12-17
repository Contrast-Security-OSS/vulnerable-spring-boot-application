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
    - name: openshift
      image: registry.redhat.io/openshift4/ose-jenkins-agent-base:latest
      command: ["tail", "-f", "/dev/null"]
      env:
        - name: LANG
          value: ""
        - name: LC_ALL
          value: ""
    - name: yq
      image: docker.io/mikefarah/yq:latest
      command: ["tail", "-f", "/dev/null"]
'''
    }
  }
  environment {
    APP_NAME = 'provider-search'
    PROJECT = 'my-test-project'
    CONTRAST_URL = 'asdf'
    CONTRAST_APIKEY = 'asdf'
    CONTRAST_SERVICEKEY = 'asdf'
    CONTRAST_USERNAME = 'asdf'
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
    stage('configure build') {
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
      name: ${APP_NAME}:${GIT_COMMIT}
  triggers:
    - type: ImageChange
      imageChange: {}
EOF
'''
        container('openshift') {
          sh "APP_NAME=${env.APP_NAME} GIT_URL=${env.GIT_URL} GIT_COMMIT=${env.GIT_COMMIT} envsubst '\$APP_NAME,\$GIT_COMMIT,\$GIT_URL' < \"buildconfig-template.yaml\" > \"buildconfig.yaml\""
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
    stage('build') {
      steps {
        container('openshift') {
          script {
            openshift.withCluster() {
              openshift.withProject() {
                def bc = openshift.selector('bc', "${env.APP_NAME}-intermed")
                def buildSelector = bc.startBuild()
                buildSelector.logs('-f')
              }
            }
          }
        }
      }
    }
    stage('Customize Deployment') {
      steps {
        container('openshift') {
          script {
            openshift.withCluster() {
              openshift.withProject() {
                openshift.apply(openshift.raw("create configmap contrast-cm --from-literal=CONTRAST__API__URL=${env.CONTRAST_URL} --from-literal=CONTRAST__AGENT__JAVA__STANDALONE_APP_NAME=${env.APP_NAME} --from-literal=CONTRAST__APPLICATION__NAME=${env.APP_NAME} --dry-run -o yaml").actions[0].out)
                openshift.apply(openshift.raw("create secret generic contrast-secret --from-literal=CONTRAST__API__API_KEY=${env.CONTRAST_APIKEY} --from-literal=CONTRAST__API__SERVICE_KEY=${env.CONTRAST_SERVICEKEY} --from-literal=CONTRAST__API__USER_NAME=${env.CONTRAST_USERNAME} --dry-run -o yaml").actions[0].out)
              }
            }
          }
        }
        container('yq') {
          sh '''
cd manifests
RESOURCES=$(ls)
echo 'resources:' | tee -a kustomization-template.yaml
for res in $RESOURCES ; 
do
  (yq ea -N -e '. | has("apiVersion")' $res  >> /dev/null && echo "- $res" | tee -a kustomization.yaml ) || echo "$res is invalid"
done

cat >> kustomization-template.yaml << 'EOF'
images:
- name: .*${APP_NAME}.*
  tag: $GIT_COMMIT
  newName: image-registry.openshift-image-registry.svc:5000/${NAMESPACE}/${APP_NAME}
patchesStrategicMerge:
- |-
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: ${APP_NAME}
  spec:
    template:
      spec:
        containers:
          - name: ${APP_NAME}
            envFrom:
            - configMapRef:
                name: contrast-cm
            - secretRef:
                name: contrast-secret
EOF
'''
        }
        container('openshift') {
          sh "cd manifests && APP_NAME=${env.APP_NAME} NAMESPACE=${env.PROJECT} GIT_COMMIT=${env.GIT_COMMIT} envsubst '\$APP_NAME,\$NAMESPACE,\$GIT_COMMIT' < \"kustomization-template.yaml\" > \"kustomization.yaml\""
        }
      }
    }
    stage('deploy') {
      steps {
        container('openshift') {
          script {
            openshift.withCluster() {
              openshift.withProject() {
                sh "ls -lah manifests/"
                sh "cat manifests/kustomization.yaml"
                sh "oc kustomize manifests"
                sh "oc kustomize manifests | oc apply -f -"
              }
            }
          }
        }
      }
    }
  }
}
