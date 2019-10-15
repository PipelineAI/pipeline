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

func writeKatibControllerOverlaysApplication(th *KustTestHarness) {
	th.writeF("/manifests/katib-v1alpha2/katib-controller/overlays/application/application.yaml", `
apiVersion: app.k8s.io/v1beta1
kind: Application
metadata:
  name: katib-controller
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: katib-controller
      app.kubernetes.io/instance: katib-controller 
      app.kubernetes.io/managed-by: kfctl
      app.kubernetes.io/component: katib
      app.kubernetes.io/part-of: kubeflow
      app.kubernetes.io/version: v0.6
  componentKinds:
  - group: core
    kind: Service
  - group: apps
    kind: Deployment
  - group: core
    kind: Secret
  - group: core
    kind: ServiceAccount
  - group: kubeflow.org
    kind: Experiment
  - group: kubeflow.org
    kind: Trial
  descriptor:
    type: "katib-controller"
    version: "v1alpha2"
    description: "Katib controller is used to create custom resource \"Experiment\" and \"Trial\"."
    maintainers:
    - name: Zhongxuan Wu
      email: wuzhongxuan@caicloud.io
    - name: Ce Gao
      email: gaoce@caicloud.io
    - name: Johnu George
      email: johnugeo@cisco.com
    - name: Hougang Liu
      email: liuhougang6@126.com
    - name: Richard Liu
      email: ricliu@google.com
    - name: YujiOshima
      email: yuji.oshima0x3fd@gmail.com
    owners:
    - name: Ce Gao
      email: gaoce@caicloud.io
    - name: Johnu George
      email: johnugeo@cisco.com
    - name: Hougang Liu
      email: liuhougang6@126.com
    - name: Richard Liu
      email: ricliu@google.com
    - name: YujiOshima
      email: yuji.oshima0x3fd@gmail.com
    keywords:
    - katib
    - katib-controller
    - hyperparameter tuning
    links:
    - description: About
      url: "https://github.com/kubeflow/katib"
  addOwnerRef: true
`)
	th.writeK("/manifests/katib-v1alpha2/katib-controller/overlays/application", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
bases:
- ../../base
resources:
- application.yaml
commonLabels:
  app.kubernetes.io/name: katib-controller 
  app.kubernetes.io/instance: katib-controller 
  app.kubernetes.io/managed-by: kfctl
  app.kubernetes.io/component: katib
  app.kubernetes.io/part-of: kubeflow
  app.kubernetes.io/version: v0.6
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/experiment-crd.yaml", `
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  name: experiments.kubeflow.org
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[-1:].type
    name: Status
    type: string
  - JSONPath: .metadata.creationTimestamp
    name: Age
    type: date
  group: kubeflow.org
  version: v1alpha2
  scope: Namespaced
  subresources:
    status: {}
  names:
    kind: Experiment
    singular: experiment
    plural: experiments
    categories:
    - all
    - kubeflow
    - katib
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/katib-controller-deployment.yaml", `
apiVersion: apps/v1
kind: Deployment
metadata:
  name: katib-controller
  labels:
    app: katib-controller
spec:
  replicas: 1
  selector:
    matchLabels:
      app: katib-controller
  template:
    metadata:
      labels:
        app: katib-controller
    spec:
      serviceAccountName: katib-controller
      containers:
      - name: katib-controller
        image: gcr.io/kubeflow-images-public/katib/v1alpha2/katib-controller:v0.1.2-alpha-289-g14dad8b
        imagePullPolicy: IfNotPresent
        command: ["./katib-controller"]
        ports:
        - containerPort: 443
          name: webhook
          protocol: TCP
        env:
        - name: KATIB_CORE_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        volumeMounts:
        - mountPath: /tmp/cert
          name: cert
          readOnly: true
      volumes:
      - name: cert
        secret:
          defaultMode: 420
          secretName: katib-controller
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/katib-controller-rbac.yaml", `
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: katib-controller
rules:
- apiGroups:
  - ""
  resources:
  - configmaps
  - serviceaccounts
  - services
  - secrets
  verbs:
  - "*"
- apiGroups:
  - ""
  resources:
  - pods
  - pods/log
  - pods/status
  verbs:
  - "*"
- apiGroups:
  - batch
  resources:
  - jobs
  - cronjobs
  verbs:
  - "*"
- apiGroups:
  - apiextensions.k8s.io
  resources:
  - customresourcedefinitions
  verbs:
  - create
  - get
- apiGroups:
  - admissionregistration.k8s.io
  resources:
  - validatingwebhookconfigurations
  - mutatingwebhookconfigurations
  verbs:
  - '*'
- apiGroups:
  - kubeflow.org
  resources:
  - experiments
  - experiments/status
  - trials
  - trials/status
  verbs:
  - "*"
- apiGroups:
  - kubeflow.org
  resources:
  - tfjobs
  - pytorchjobs
  verbs:
  - "*"
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: katib-controller
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: katib-controller
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: katib-controller
subjects:
- kind: ServiceAccount
  name: katib-controller
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/katib-controller-secret.yaml", `
apiVersion: v1
kind: Secret
metadata:
  name: katib-controller
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/katib-controller-service.yaml", `
apiVersion: v1
kind: Service
metadata:
  name: katib-controller
spec:
  ports:
  - port: 443
    protocol: TCP
    targetPort: 443
  selector:
    app: katib-controller
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/trial-crd.yaml", `
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  name: trials.kubeflow.org
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[-1:].type
    name: Status
    type: string
  - JSONPath: .metadata.creationTimestamp
    name: Age
    type: date
  group: kubeflow.org
  version: v1alpha2
  scope: Namespaced
  subresources:
    status: {}
  names:
    kind: Trial
    singular: trial
    plural: trials
    categories:
    - all
    - kubeflow
    - katib
`)
	th.writeF("/manifests/katib-v1alpha2/katib-controller/base/trial-template.yaml", `
apiVersion: v1
kind: ConfigMap
metadata:
  name: trial-template
data:
  defaultTrialTemplate.yaml : |-
    apiVersion: batch/v1
    kind: Job
    metadata:
      name: {{.Trial}}
      namespace: {{.NameSpace}}
    spec:
      template:
        spec:
          containers:
          - name: {{.Trial}}
            image: alpine
          restartPolicy: Never
`)
	th.writeK("/manifests/katib-v1alpha2/katib-controller/base", `
namespace: kubeflow
resources:
- experiment-crd.yaml
- katib-controller-deployment.yaml
- katib-controller-rbac.yaml
- katib-controller-secret.yaml
- katib-controller-service.yaml
- trial-crd.yaml
- trial-template.yaml
generatorOptions:
  disableNameSuffixHash: true
images:
  - name: gcr.io/kubeflow-images-public/katib/v1alpha2/katib-controller
    newTag: v0.6.0-rc.0
`)
}

func TestKatibControllerOverlaysApplication(t *testing.T) {
	th := NewKustTestHarness(t, "/manifests/katib-v1alpha2/katib-controller/overlays/application")
	writeKatibControllerOverlaysApplication(th)
	m, err := th.makeKustTarget().MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	expected, err := m.EncodeAsYaml()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	targetPath := "../katib-v1alpha2/katib-controller/overlays/application"
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
