rm -rf build/
rm -rf dist/
rm -rf pipeline_model.egg-info
rm -rf pipeline_model/__pycache__

python setup.py bdist_wheel --universal upload
