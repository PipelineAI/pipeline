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

func writeIstioIngressOverlaysCognito(th *KustTestHarness) {
	th.writeF("/manifests/aws/istio-ingress/overlays/cognito/ingress.yaml", `
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: istio-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/auth-type: cognito
    alb.ingress.kubernetes.io/auth-idp-cognito: '{"UserPoolArn":"$(CognitoUserPoolArn)","UserPoolClientId":"$(CognitoAppClientId)", "UserPoolDomain":"$(CognitoUserPoolDomain)"}'
    alb.ingress.kubernetes.io/certificate-arn: $(certArn)
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'`)
	th.writeF("/manifests/aws/istio-ingress/overlays/cognito/params.yaml", `
varReference:
- path: metadata/annotations
  kind: Ingress`)
	th.writeF("/manifests/aws/istio-ingress/overlays/cognito/params.env", `
CognitoUserPoolArn=
CognitoAppClientId=
CognitoUserPoolDomain=
certArn=`)
	th.writeK("/manifests/aws/istio-ingress/overlays/cognito", `
bases:
- ../../base
patchesStrategicMerge:
- ingress.yaml
configMapGenerator:
- name: istio-ingress-cognito-parameters
  env: params.env
vars:
- name: CognitoUserPoolArn
  objref:
    kind: ConfigMap
    name: istio-ingress-cognito-parameters
    apiVersion: v1
  fieldref:
    fieldpath: data.CognitoUserPoolArn
- name: CognitoAppClientId
  objref:
    kind: ConfigMap
    name: istio-ingress-cognito-parameters
    apiVersion: v1
  fieldref:
    fieldpath: data.CognitoAppClientId
- name: CognitoUserPoolDomain
  objref:
    kind: ConfigMap
    name: istio-ingress-cognito-parameters
    apiVersion: v1
  fieldref:
    fieldpath: data.CognitoUserPoolDomain
- name: certArn
  objref:
    kind: ConfigMap
    name: istio-ingress-cognito-parameters
    apiVersion: v1
  fieldref:
    fieldpath: data.certArn
configurations:
- params.yaml`)
	th.writeF("/manifests/aws/istio-ingress/base/ingress.yaml", `
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
  name: istio-ingress
spec:
  rules:
    - http:
        paths:
          - backend:
              serviceName: istio-ingressgateway
              servicePort: 80
            path: /*`)
	th.writeF("/manifests/aws/istio-ingress/base/istio-gateway.yaml", `
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: kubeflow-gateway
  namespace: kubeflow
spec:
  selector:
    istio: ingressgateway
  servers:
  - hosts:
    - '*'
    port:
      name: http
      number: 80
      protocol: HTTP`)
	th.writeK("/manifests/aws/istio-ingress/base", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
- ingress.yaml
- istio-gateway.yaml
#- istio-virtual-service.yaml
commonLabels:
  kustomize.component: istio-ingress
`)
}

func TestIstioIngressOverlaysCognito(t *testing.T) {
	th := NewKustTestHarness(t, "/manifests/aws/istio-ingress/overlays/cognito")
	writeIstioIngressOverlaysCognito(th)
	m, err := th.makeKustTarget().MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	expected, err := m.EncodeAsYaml()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	targetPath := "../aws/istio-ingress/overlays/cognito"
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
