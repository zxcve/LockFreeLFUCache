package project.benchmarks;

import java.util.concurrent.ThreadLocalRandom;

import project.cache.Cache;
import project.cache.CoarsegrainLFU;
import project.cache.ConcurrentLFU;
import project.cache.NonLinearizableLFU;
import project.cache.SequentialLFU;
import project.util.IODevice;

public class Main {
	private Cache cache;

	public Main(Cache cache, float variance, int len, int nThreads, boolean verbose) {
		/* Initialize variables */
		this.cache = cache;
		int reads = len / nThreads;
		/* Initialize threads */
		Thread[] threads = new Thread[nThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = makeThread(variance, reads, verbose);
		}
		/* Run threads */
		long start = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.start();
		}
		/* Wait */
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		long end = System.currentTimeMillis() - start;
		/* Results */
		if (verbose) {
			System.out.print("\n");
		}
		System.out.println(end + " ms");
		System.out.println("Cache print\n************");
		cache.print();
	}

	private char readRandom(float variance) {
		int bound = Math.round((new Float(IODevice.fileSize) - 1) * variance / 100f) + 1;
		int addr = ThreadLocalRandom.current().nextInt(bound);
		return cache.get(addr);
	}

	private Thread makeThread(float variance, int reads, boolean verbose) {
		return new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < reads; i++) {
					char read = readRandom(variance);
					if (verbose) {
						System.out.print(read);
					}
				}
			}
		});
	}

	public static void main(String[] args) {
		/* Check args */
		if (args.length < 6 || args.length > 7 || Integer.parseInt(args[1]) < 0 || Integer.parseInt(args[1]) > 100
				|| Float.parseFloat(args[2]) > 100 || Float.parseFloat(args[2]) < 0 || Integer.parseInt(args[4]) < 1
				|| !(Boolean.parseBoolean(args[5]) == false || Boolean.parseBoolean(args[5]) == true))
			exit("Invalid args");
		float variance = Float.parseFloat(args[2]);
		int len = Integer.parseInt(args[3]);
		int nThreads = Integer.parseInt(args[4]);
		if (len < nThreads) {
			exit("Not enough reads for threads");
		}
		boolean verbose = Boolean.parseBoolean(args[5]);
		/* Initialize cache */
		int cacheSize = (int) (Integer.parseInt(args[1]) / 100.0 * IODevice.fileSize);
		if (cacheSize == 0) {
			exit("Cache size too small");
		}
		Cache cache = null;
		if ("Sequential".equals(args[0])) {
			if (nThreads > 1)
				exit("Cannot do sequential cache with multiple threads");
			cache = new SequentialLFU(cacheSize);
		} else if ("Coarsegrain".equals(args[0])) {
			cache = new CoarsegrainLFU(cacheSize);
		} else if ("Concurrent".equals(args[0])) {
			cache = new ConcurrentLFU(cacheSize);
		} else if ("Nonlinearizable".equals(args[0])) {
			cache = new NonLinearizableLFU(cacheSize);
		} else if ("Nocache".equals(args[0])) {
			class NoCache implements Cache {
				@Override
				public char get(int key) {
					return IODevice.read(key);
				}

				@Override
				public void print() {
				}
			}
			cache = new NoCache();
		} else {
			exit("No cache with name: " + args[0]);
		}
		new Main(cache, variance, len, nThreads, verbose);
	}

	private static void exit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
}
