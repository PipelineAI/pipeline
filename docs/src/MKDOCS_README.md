# docs 
PipelineIO Documentation [http://docs.pipeline.io/](http://docs.pipeline.io/)

## Steps to build and deploy documentation
* Install requirements.txt with `pip install -r requirements.txt`
* Use `mkdocs serve` to run a local server and build/preview documentation
* To build the static site, use `mkdocs build`. This step is not necessary if hosting the site on Github Pages (which is what is currently done)
* To deploy static site to gh-pages, run `mkdocs gh-deploy`. This will build the static site, commit it to the gh-pages branch and push the branch to Github
