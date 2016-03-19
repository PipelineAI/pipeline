# Used for System-Level Performance Monitoring (Linux "perf" Command)
apt-get install -y linux-tools-common linux-tools-generic linux-tools-`uname -r | sed -e "s/-.*//" | sed -e "s/\.[0]*$//"`

# Used for Building Flame Graphs from Linux "perf" Command
cd ~ 
git clone --depth=1 https://github.com/jrudolph/perf-map-agent 
cd perf-map-agent 
apt-get install -y cmake 
cmake . 
make 
cd ~ 
git clone --depth=1 https://github.com/brendangregg/FlameGraph 

# (Optional) Useful UI for Profiling - Works with Linux "perf" Command and Flame Graphs
# && cd ~ \
# && apt-get install -y nodejs \
# && apt-get install -y npm \
# && git clone git://git.pcp.io/pcp \
# && cd pcp \
# && ./configure --prefex=/usr --sysconfdir=/etc --localstatedir=/var \
# && make \
# && make install \
# && cd ~ \
# && git clone https://github.com/Netflix/vector.git \

# 100 TB Daytona GraySort Challenge Data Generator (gensort) and Data Sort Validator (valsort)
cd ~ 
wget http://www.ordinal.com/try.cgi/gensort-linux-${GENSORT_VERSION}.tar.gz 
mkdir gensort-linux-${GENSORT_VERSION}/ 
tar xvzf gensort-linux-${GENSORT_VERSION}.tar.gz -C gensort-linux-${GENSORT_VERSION}/ 
rm gensort-linux-${GENSORT_VERSION}.tar.gz

# Smaller version of the 100 TB Daytona GraySort Challenge Dataset used by the sorting examples
cat $DATASETS_HOME/sort/sort.txt.bz2-part-* > $DATASETS_HOME/sort/sort.txt.bz2
bzip2 -d -k $DATASETS_HOME/sort/sort.txt.bz2
