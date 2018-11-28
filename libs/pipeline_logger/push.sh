rm -rf build/
rm -rf dist/
rm -rf pipeline_logger.egg-info
rm -rf pipeline_logger/__pycache__

python setup.py bdist_wheel --universal upload
