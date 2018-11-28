rm -rf build/
rm -rf dist/
rm -rf pipeline_monitor.egg-info
rm -rf pipeline_monitor/__pycache__

python setup.py bdist_wheel --universal upload
