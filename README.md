# Concurrent Network Cache
## Usage
Your program should be invoked with six arguments:
```bash
java project.benchmarks.Main c s v l n v
```
c: type of cache [Nocache, Sequential, Coarsegrain, Concurrent, Nonlinearizable]  
s: cache size [%]  
v: variance of addresses to read [%]  
l: number of reads [int]  
n: number of threads [int]  
v: verbosity [boolean]  
### Running
To run the cache simulation make and execute Main.class
```bash
$ make
$ java project.benchmarks.Main Concurrent 50 100 10000 4 false
```
## Sample
```bash
$ java project.benchmarks.Main Coarsegrain 25 0 100 4 true
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
52 ms
Cache print
************
100 -> a
************
```
```bash
$ java project.benchmarks.Main Coarsegrain 25 100 100 4 true
udpsmooxmun rqasatcuvlnlriyvbvryiaeisqrhtrus mjgovjnjnvprizejezn kchvgngzto bdepgezyjdeshprwjhbmhoql
291 ms
Cache print
************
1 -> q
************
3 -> l a
************
4 -> m
************
5 -> o v
************
```

