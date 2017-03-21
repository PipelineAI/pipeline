# $1: old tag
# $2: new tag
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/latest/r1.0/g' {} \;
