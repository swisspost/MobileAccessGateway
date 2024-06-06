#!groovy
@Library('pipeline-library@master') _

def BUILD_INFO = Artifactory.newBuildInfo()
def ROOT_PROJECT_NAME = 'app/ehealth'

def PROJECT_NAME = 'windseeker-mag'
def GIT_PROJECT = 'WDS'
def GIT_USER = 'cicd-tdh'

def SLAVE = 'tdh-slaves'

def DOCKER_IMAGE_TYPE = BRANCH_NAME == 'main' ? 'stable' : 'experimental'
def DOCKER_IMAGE_NAME_MAG = "app/ehealth/windseeker-mag/${DOCKER_IMAGE_TYPE}/${PROJECT_NAME}"

def AZURE_SANDBOX_ENVIRONMENT_REPOSITORY = 'gitops-e-health-windseeker-poc-sandbox'

pipeline {
    agent {
        label SLAVE
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
        timestamps()
    }

    environment {
        GIT_CREDENTIALS = credentials("${GIT_USER}")
    }

    tools {
        jdk 'jdk-17'
        maven 'maven-3'
    }

    stages {

        stage('Init') {
            steps {
                script {
                    def branchName = BRANCH_NAME != "main" ? "${BRANCH_NAME.replaceAll('/','-')}" : ""

                    VERSION = branchName == "" ?
                        "${getMvnVersion()}.${currentBuild.number}" :
                        "${getMvnVersion()}-${branchName}.${currentBuild.number}"

                    DOCKER_IMAGE_TAGS = BRANCH_NAME == 'main' ? [VERSION, 'latest'] : [VERSION]

                    currentBuild.displayName = VERSION
                }
            }
        }

        stage('Build') {
            steps {
                mvnBuild(buildInfo: BUILD_INFO, mavenGoals: 'clean package', mavenParams: '-DskipTests')
            }
        }

        stage('Test') {
            steps {
                mvnBuild(buildInfo: BUILD_INFO, mavenGoals: 'test verify')
            }
        }

       stage('Docker login') {
            steps {
                dockerLogin(credentialsId: 'f68e22a9-4d3d-4cbb-9c39-998164396bb7')
            }
        }

        stage('Docker build & publish') {
            when {
                expression {
                    env.BRANCH_NAME.startsWith("PR-") == false
                }
            }
            steps {




                dockerBuild(
                    projectName: DOCKER_IMAGE_NAME_MAG,
                    tag: DOCKER_IMAGE_TAGS,
                    buildOptions: ['pull', 'no-cache', 'rm=true', 'build-arg http_proxy=http://outappl.pnet.ch:3128', 'build-arg https_proxy=http://outappl.pnet.ch:3128', 'build-arg no_proxy=.pnet.ch,.post.ch,.pnetcloud.ch', "build-arg JAR_FILE=target/mobile-access-gateway-${getMvnVersion()}-SNAPSHOT-spring-boot.jar"],
                    path: '.'
                )
            }
        }

        stage('Docker push internally') {
            when {
                expression {
                    env.BRANCH_NAME.startsWith("PR-") == false
                }
            }
            steps {
                 dockerPush(
                    projectName: DOCKER_IMAGE_NAME_MAG,
                    tag: DOCKER_IMAGE_TAGS,
                    doLoginLogout: false
                 )
            }
        }

        stage('Update Kustomize') {
            steps {
                gitOpsUpdate(
                    kustomizationFile: 'kustomize/kustomization.yaml',
                    bitbucketCredential: GIT_USER,
                    projectVersion: VERSION
                )
            }
        }

        stage('Deploy to Dev'){
            when {
                branch 'main'
            }
            stages{
                stage('Create GitOpsPR') {
                    steps {
                        gitOpsPR(
                            bitbucketProject: GIT_PROJECT,
                            bitbucketRepository: AZURE_SANDBOX_ENVIRONMENT_REPOSITORY,
                            bitbucketCredential: GIT_USER,
                            projectVersion: VERSION,
                            automerge: true
                        )
                    }
                }

            }
        }
    }
    post {
        cleanup {
            script {
                cleanWs()
            }
        }
    }
}