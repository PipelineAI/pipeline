package tests_test

import (
	"sigs.k8s.io/kustomize/k8sdeps/kunstruct"
	"sigs.k8s.io/kustomize/k8sdeps/transformer"
	"sigs.k8s.io/kustomize/pkg/fs"
	"sigs.k8s.io/kustomize/pkg/loader"
	"sigs.k8s.io/kustomize/pkg/resmap"
	"sigs.k8s.io/kustomize/pkg/resource"
	"sigs.k8s.io/kustomize/pkg/target"
	"testing"
)

func writeMxnetOperatorOverlaysApplication(th *KustTestHarness) {
	th.writeF("/manifests/mxnet-job/mxnet-operator/overlays/application/application.yaml", `
apiVersion: app.k8s.io/v1beta1
kind: Application
metadata:
  name: mxnet-operator
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: mxnet-operator
      app.kubernetes.io/instance: mxnet-operator
      app.kubernetes.io/version: v0.6.0
      app.kubernetes.io/component: mxnet
      app.kubernetes.io/part-of: kubeflow
      app.kubernetes.io/managed-by: kfctl
  componentKinds:
  - group: apps
    kind: Deployment
  - group: core
    kind: ServiceAccount
  - group: kubeflow.org
    kind: MXJob
  descriptor:
    type: "mxnet-operator"
    version: "v1beta1"
    description: "mxnet-operator allows users to create and manage the \"MXJob\" custom resource."
    maintainers:
    - name: Lei Su
      email: suleisl2000@hotmail.com
    owners:
    - name: Lei Su
      email: suleisl2000@hotmail.com
    keywords:
    - "MXjob"
    - "mxnet-operator"
    - "mxnet-training"
    links:
    - description: About
      url: "https://github.com/kubeflow/mxnet-operator"
  addOwnerRef: true
`)
	th.writeK("/manifests/mxnet-job/mxnet-operator/overlays/application", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
bases:
- ../../base
resources:
- application.yaml
commonLabels:
  app.kubernetes.io/name: mxnet-operator
  app.kubernetes.io/instance: mxnet-operator
  app.kubernetes.io/version: v0.6.0
  app.kubernetes.io/component: mxnet
  app.kubernetes.io/part-of: kubeflow
  app.kubernetes.io/managed-by: kfctl
`)
	th.writeF("/manifests/mxnet-job/mxnet-operator/base/cluster-role-binding.yaml", `
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  labels:
    app: mxnet-operator
  name: mxnet-operator
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: mxnet-operator
subjects:
- kind: ServiceAccount
  name: mxnet-operator`)
	th.writeF("/manifests/mxnet-job/mxnet-operator/base/cluster-role.yaml", `
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRole
metadata:
  labels:
    app: mxnet-operator
  name: mxnet-operator
rules:
- apiGroups:
  - kubeflow.org
  resources:
  - mxjobs
  verbs:
  - '*'
- apiGroups:
  - apiextensions.k8s.io
  resources:
  - customresourcedefinitions
  verbs:
  - '*'
- apiGroups:
  - storage.k8s.io
  resources:
  - storageclasses
  verbs:
  - '*'
- apiGroups:
  - batch
  resources:
  - jobs
  verbs:
  - '*'
- apiGroups:
  - ""
  resources:
  - configmaps
  - pods
  - services
  - endpoints
  - persistentvolumeclaims
  - events
  verbs:
  - '*'
- apiGroups:
  - apps
  - extensions
  resources:
  - deployments
  verbs:
  - '*'`)
	th.writeF("/manifests/mxnet-job/mxnet-operator/base/crd.yaml", `
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  name: mxjobs.kubeflow.org
spec:
  group: kubeflow.org
  names:
    kind: MXJob
    plural: mxjobs
    singular: mxjob
  version: v1beta1
  scope: Namespaced
`)
	th.writeF("/manifests/mxnet-job/mxnet-operator/base/deployment.yaml", `
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: mxnet-operator
spec:
  replicas: 1
  template:
    metadata:
      labels:
        name: mxnet-operator
    spec:
      containers:
      - command:
        - /opt/kubeflow/mxnet-operator.v1beta1
        - --alsologtostderr
        - -v=1
        env:
        - name: MY_POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: MY_POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        image: mxjob/mxnet-operator:v1beta1
        imagePullPolicy: Always
        name: mxnet-operator
      serviceAccountName: mxnet-operator
`)
	th.writeF("/manifests/mxnet-job/mxnet-operator/base/service-account.yaml", `
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    app: mxnet-operator
  name: mxnet-operator`)
	th.writeK("/manifests/mxnet-job/mxnet-operator/base", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: kubeflow
resources:
- cluster-role-binding.yaml
- cluster-role.yaml
- crd.yaml
- deployment.yaml
- service-account.yaml
commonLabels:
  kustomize.component: mxnet-operator
images:
  - name: mxjob/mxnet-operator
    newName: mxjob/mxnet-operator
    newTag: v1beta1
`)
}

func TestMxnetOperatorOverlaysApplication(t *testing.T) {
	th := NewKustTestHarness(t, "/manifests/mxnet-job/mxnet-operator/overlays/application")
	writeMxnetOperatorOverlaysApplication(th)
	m, err := th.makeKustTarget().MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	expected, err := m.EncodeAsYaml()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	targetPath := "../mxnet-job/mxnet-operator/overlays/application"
	fsys := fs.MakeRealFS()
	_loader, loaderErr := loader.NewLoader(targetPath, fsys)
	if loaderErr != nil {
		t.Fatalf("could not load kustomize loader: %v", loaderErr)
	}
	rf := resmap.NewFactory(resource.NewFactory(kunstruct.NewKunstructuredFactoryImpl()))
	kt, err := target.NewKustTarget(_loader, rf, transformer.NewFactoryImpl())
	if err != nil {
		th.t.Fatalf("Unexpected construction error %v", err)
	}
	actual, err := kt.MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	th.assertActualEqualsExpected(actual, string(expected))
}
