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

func writeKnativeServingCrdsBase(th *KustTestHarness) {
	th.writeF("/manifests/knative/knative-serving-crds/base/crd.yaml", `
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: certificates.networking.internal.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[?(@.type=="Ready")].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=="Ready")].reason
    name: Reason
    type: string
  group: networking.internal.knative.dev
  names:
    categories:
    - knative-internal
    - networking
    kind: Certificate
    plural: certificates
    shortNames:
    - kcert
    singular: certificate
  scope: Namespaced
  subresources:
    status: {}
  version: v1alpha1

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: clusteringresses.networking.internal.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: networking.internal.knative.dev
  names:
    categories:
    - knative-internal
    - networking
    kind: ClusterIngress
    plural: clusteringresses
    singular: clusteringress
  scope: Cluster
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
  name: images.caching.internal.knative.dev
spec:
  group: caching.internal.knative.dev
  names:
    categories:
    - knative-internal
    - caching
    kind: Image
    plural: images
    shortNames:
    - img
    singular: image
  scope: Namespaced
  subresources:
    status: {}
  version: v1alpha1

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: ingresses.networking.internal.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: networking.internal.knative.dev
  names:
    categories:
    - knative-internal
    - networking
    kind: Ingress
    plural: ingresses
    shortNames:
    - ing
    singular: ingress
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: metrics.autoscaling.internal.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: autoscaling.internal.knative.dev
  names:
    categories:
    - knative-internal
    - autoscaling
    kind: Metric
    plural: metrics
    singular: metric
  scope: Namespaced
  subresources:
    status: {}
  version: v1alpha1

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: podautoscalers.autoscaling.internal.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: autoscaling.internal.knative.dev
  names:
    categories:
    - knative-internal
    - autoscaling
    kind: PodAutoscaler
    plural: podautoscalers
    shortNames:
    - kpa
    - pa
    singular: podautoscaler
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: serverlessservices.networking.internal.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .spec.mode
    name: Mode
    type: string
  - JSONPath: .status.serviceName
    name: ServiceName
    type: string
  - JSONPath: .status.privateServiceName
    name: PrivateServiceName
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: networking.internal.knative.dev
  names:
    categories:
    - knative-internal
    - networking
    kind: ServerlessService
    plural: serverlessservices
    shortNames:
    - sks
    singular: serverlessservice
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: configurations.serving.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.latestCreatedRevisionName
    name: LatestCreated
    type: string
  - JSONPath: .status.latestReadyRevisionName
    name: LatestReady
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: serving.knative.dev
  names:
    categories:
    - all
    - knative
    - serving
    kind: Configuration
    plural: configurations
    shortNames:
    - config
    - cfg
    singular: configuration
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: revisions.serving.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .metadata.labels['serving\.knative\.dev/configuration']
    name: Config Name
    type: string
  - JSONPath: .status.serviceName
    name: K8s Service Name
    type: string
  - JSONPath: .metadata.labels['serving\.knative\.dev/configurationGeneration']
    name: Generation
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: serving.knative.dev
  names:
    categories:
    - all
    - knative
    - serving
    kind: Revision
    plural: revisions
    shortNames:
    - rev
    singular: revision
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: routes.serving.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.url
    name: URL
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: serving.knative.dev
  names:
    categories:
    - all
    - knative
    - serving
    kind: Route
    plural: routes
    shortNames:
    - rt
    singular: route
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  labels:
    knative.dev/crd-install: "true"
    serving.knative.dev/release: "v0.8.0"
  name: services.serving.knative.dev
spec:
  additionalPrinterColumns:
  - JSONPath: .status.url
    name: URL
    type: string
  - JSONPath: .status.latestCreatedRevisionName
    name: LatestCreated
    type: string
  - JSONPath: .status.latestReadyRevisionName
    name: LatestReady
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].status
    name: Ready
    type: string
  - JSONPath: .status.conditions[?(@.type=='Ready')].reason
    name: Reason
    type: string
  group: serving.knative.dev
  names:
    categories:
    - all
    - knative
    - serving
    kind: Service
    plural: services
    shortNames:
    - kservice
    - ksvc
    singular: service
  scope: Namespaced
  subresources:
    status: {}
  versions:
  - name: v1alpha1
    served: true
    storage: true

---
`)
	th.writeK("/manifests/knative/knative-serving-crds/base", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- crd.yaml
`)
}

func TestKnativeServingCrdsBase(t *testing.T) {
	th := NewKustTestHarness(t, "/manifests/knative/knative-serving-crds/base")
	writeKnativeServingCrdsBase(th)
	m, err := th.makeKustTarget().MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	expected, err := m.EncodeAsYaml()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	targetPath := "../knative/knative-serving-crds/base"
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
