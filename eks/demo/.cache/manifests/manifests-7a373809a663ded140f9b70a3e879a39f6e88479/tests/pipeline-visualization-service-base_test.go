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

func writePipelineVisualizationServiceBase(th *KustTestHarness) {
	th.writeF("/manifests/pipeline/pipeline-visualization-service/base/service.yaml", `
apiVersion: v1
kind: Service
metadata:
  name: ml-pipeline-visualizationserver
spec:
  ports:
  - name: http
    port: 8888
    protocol: TCP
    targetPort: 8888
  selector:
    app: ml-pipeline-visualizationserver
`)
	th.writeK("/manifests/pipeline/pipeline-visualization-service/base", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
nameprefix: ml-pipeline-
commonLabels:
  app: ml-pipeline-visualizationserver
resources:
- service.yaml
images:
- name: gcr.io/ml-pipeline/visualization-server
  newTag: '0.1.27'
`)
}

func TestPipelineVisualizationServiceBase(t *testing.T) {
	th := NewKustTestHarness(t, "/manifests/pipeline/pipeline-visualization-service/base")
	writePipelineVisualizationServiceBase(th)
	m, err := th.makeKustTarget().MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	expected, err := m.EncodeAsYaml()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	targetPath := "../pipeline/pipeline-visualization-service/base"
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
