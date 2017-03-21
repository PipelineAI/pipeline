# $1: old tag
# $2: new tag
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/:latest/:v1.0.0/g' {} \;
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "r1.0"/revision: "v1.0.0"/g' {} \;
