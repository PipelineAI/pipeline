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

func writeNvidiaDevicePluginBase(th *KustTestHarness) {
	th.writeF("/manifests/aws/nvidia-device-plugin/base/daemonset.yaml", `
apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
  name: nvidia-device-plugin-daemonset
spec:
  updateStrategy:
    type: RollingUpdate
  template:
    metadata:
      # Mark this pod as a critical add-on; when enabled, the critical add-on scheduler
      # reserves resources for critical add-on pods so that they can be rescheduled after
      # a failure.  This annotation works in tandem with the toleration below.
      annotations:
        scheduler.alpha.kubernetes.io/critical-pod: ""
    spec:
      tolerations:
        # Allow this pod to be rescheduled while the node is in "critical add-ons only" mode.
        # This, along with the annotation above marks this pod as a critical add-on.
        - key: CriticalAddonsOnly
          operator: Exists
        - key: nvidia.com/gpu
          operator: Exists
          effect: NoSchedule
      containers:
        - image: nvidia/k8s-device-plugin:1.0.0-beta
          name: nvidia-device-plugin-ctr
          securityContext:
            allowPrivilegeEscalation: false
            capabilities:
              drop: ["ALL"]
          volumeMounts:
            - name: device-plugin
              mountPath: /var/lib/kubelet/device-plugins
      volumes:
        - name: device-plugin
          hostPath:
            path: /var/lib/kubelet/device-plugins`)
	th.writeK("/manifests/aws/nvidia-device-plugin/base", `
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: kube-system
resources:
- daemonset.yaml
commonLabels:
  kustomize.component: nvidia-device-plugin
images:
- name: nvidia/k8s-device-plugin
  newName: nvidia/k8s-device-plugin
  newTag: 1.0.0-beta
`)
}

func TestNvidiaDevicePluginBase(t *testing.T) {
	th := NewKustTestHarness(t, "/manifests/aws/nvidia-device-plugin/base")
	writeNvidiaDevicePluginBase(th)
	m, err := th.makeKustTarget().MakeCustomizedResMap()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	expected, err := m.EncodeAsYaml()
	if err != nil {
		t.Fatalf("Err: %v", err)
	}
	targetPath := "../aws/nvidia-device-plugin/base"
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
