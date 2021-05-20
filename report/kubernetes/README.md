로컬환경 세팅

    현재 Mac 기준 세팅으로 각자 환경에 따라 맞는 로컬환경 설치 필요

    1. awscli 설치
       로컬 환경에서 AWS를 제어하기 위해 설치
       https://aws.amazon.com/ko/cli/
    
    파이썬 설치
    ➜  ~ pip3 install awscli
    
    
    2. EKS Client (eksctl) 설치
       https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/eksctl.html
    
    ➜  ~ brew tap weaveworks/tap
    ➜  ~ brew install weaveworks/tap/eksctl

클러스터 생성

    - AWS 관리콘솔 IAM에서 자격증명 생성
    * AWS 관리콘솔 접속
    * IAM 서비스의 ‘사용자’ 메뉴 접속
    * 사용자의 Access Key와 Secret Access Key 복사

    - Cloud IDE - AWS Client Config
      $ aws configure
      $ Input Access Key
      $ Input Secret Access Key
      $ Input your Region Code
      $ Input 'json' in Output format

    - EKS (Elastic Kubernetes Service) 생성
    ➜  ~ eksctl create cluster --name user01-eks --version 1.17 --nodegroup-name standard-workers --node-type t3.medium --nodes 4 --nodes-min 1 --nodes-max 4

    [참조]실습 스크립트 - 컨테이너 오케스트레이션 Lab. Guide
    http://msaschool.io/operation/operation/operation-seven/

    클러스터 설정 로컬 저장
    ➜  ~ aws eks --region ap-northeast-2 update-kubeconfig --name user01-eks

k8s에 kafka 설치 및 실행

    [참조]kafka 설치
    http://msaschool.io/operation/implementation/implementation-seven/

    로컬환경 세팅(helm 설치)
    ➜  ~ brew install helm

    kafka 설치
    ➜  ~ kubectl --namespace kube-system create sa tiller
    serviceaccount/tiller created
    ➜  ~ kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
    clusterrolebinding.rbac.authorization.k8s.io/tiller created
    ➜  ~ helm repo add incubator https://charts.helm.sh/incubator
    "incubator" has been added to your repositories
    ➜  ~ helm repo update
    Hang tight while we grab the latest from your chart repositories...
    ...Successfully got an update from the "incubator" chart repository
    ...Successfully got an update from the "bitnami" chart repository
    Update Complete. ⎈Happy Helming!⎈
    ➜  ~ kubectl create ns kafka
    namespace/kafka created
    ➜  ~ helm install my-kafka --namespace kafka incubator/kafka
    WARNING: This chart is deprecated
    NAME: my-kafka
    .. 이하 생략

