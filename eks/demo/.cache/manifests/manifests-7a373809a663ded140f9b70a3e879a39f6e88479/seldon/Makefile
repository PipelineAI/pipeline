
seldon-core:
	git clone -b release-0.4 --single-branch git@github.com:SeldonIO/seldon-core.git

seldon-core-operator/base: seldon-core clean-kustomize
	helm convert -f values.yaml seldon-core/helm-charts/seldon-core-operator --skip-transformers=image,secret,namePrefix,labels,configMap --destination seldon-core-operator/base --namespace kubeflow
	# Convert seems to lose subresources so we add the crd explicitly
	cp seldon-core/helm-charts/seldon-core-operator/templates/crd.yaml seldon-core-operator/base/seldondeployments.machinelearning.seldon.io-crd.yaml
	echo "  volumeClaimTemplates: []" >> seldon-core-operator/base/seldon-operator-controller-manager-statefulset.yaml

.PHONY: clean
clean:
	rm -rf seldon-core

.PHONY:clean-kustomize
clean-kustomize:
	rm -rf seldon-core-operator/base
