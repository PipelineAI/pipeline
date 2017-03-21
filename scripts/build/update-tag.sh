# $1: old tag
# $2: new tag
#find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/:master/:latest/g' {} \;
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/:latest/:r1.0/g' {} \;
find ../../ -name *-rc*.yaml -type f -exec sed -i -e 's/revision: "master"/revision: "r1.0"/g' {} \;
