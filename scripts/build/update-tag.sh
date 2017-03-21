# $1: old tag
# $2: new tag
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/:r1.0/:latest/g' {} \;
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "r1.0"/revision: "master"/g' {} \;
