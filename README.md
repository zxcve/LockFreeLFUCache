# Concurrent Network Cache
## Usage
Your program should be invoked with five arguments:
```bash
java project.benchmarks.Main c s v l n
```
c: type of cache [Nocache, Sequential, Coarsegrain, Concurrent, Nonlinearizable]  
s: cache size [%]  
v: variance of addresses to read [%]  
l: number of reads [int]  
n: number of threads [int]  
### Running
To run the cache simulation make and execute Main.class
```bash
$ make
$ java project.benchmarks.Main Concurrent 50 100 10000 4
```
## Sample
```bash
$ java project.benchmarks.Main Coarsegrain 25 0 100 4
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
37 ms
```
```bash
$ java project.benchmarks.Main Coarsegrain 25 100 100 4
sqk mcheuxabduvcxdv gctqyrcajnuqrti eecdcqqzxihoyuvbvdzwmjocvzlyqfpum ebhjvvksngvlbncrtgnqpice ishmn
299 ms
```
