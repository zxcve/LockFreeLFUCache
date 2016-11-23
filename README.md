# Multithreaded Network Cache
## Usage
Your program should be invoked with five arguments:
```bash
java project.benchmarks.Main c s v l n
```
c: type of cache [Sequential, Coarsegrain]  
s: cache size [int]  
v: variance of addresses to read [%]  
l: number of reads [int]  
n: number of threads [int]  
### Running
To run the cache simulation make and execute Main.class
```bash
$ make
$ java project.benchmarks.Main Coarsegrain 16 0 10000 4
```
## Sample
```bash
$ java project.benchmarks.Main Coarsegrain 5 0 100 4
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
33 ms
```
```bash
$ java project.benchmarks.Main Coarsegrain 5 100 100 4
lzuhrhkhfhvifysnbssknm phqwlo tktchgxuvpyctbfsdytjjwexeyptxrirahvvkuciyvnecvbykqwkllllerqdxjqsttgycs
221 ms
```