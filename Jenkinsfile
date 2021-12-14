pipeline {
  agent {
    kubernetes {
      idleMinutes 5
      yaml '''
apiVersion: v1
kind: Pod
spec:
  securityContext:
    runAsUser: 1000990000
  containers:
    - name: contrast
      image: ghcr.io/garage-contrast/contrast-client-go:sha-b931941
      command: ["tail", "-f", "/dev/null"]
      volumeMounts:
      - mountPath: '/shared'
        name: sharedvolume
    - name: buildah
      image: quay.io/buildah/stable:v1.23.0
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
    stage('Do something') {
      steps {
        sh "echo 'hello world'"   
      }
    }
    stage('Build Contrast Container') {
      steps {
        container('buildah') {
          sh "buildah --storage-driver=vfs bud --format=oci --layers=true -f ./Dockerfile -t 'test-intermediate' ."
          bash '''
cat > Dockerfile.contrast << 'EOF'
FROM $(params.IMAGE)-intermediate

RUN mkdir -p /opt/contrast \
&& mvn dependency:copy -Dartifact=com.contrastsecurity:contrast-agent:LATEST -DoutputDirectory=/opt/contrast \
&& mv /opt/contrast/contrast-agent*.jar /opt/contrast/contrast-agent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/opt/contrast/contrast-agent.jar"
EOF

cat Dockerfile.contrast
'''
          sh "buildah --storage-driver=vfs bud --format=oci --layers=true -f ./Dockerfile.contrast -t 'test-final' ."
        }
      }
    }
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
  }
}
