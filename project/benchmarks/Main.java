package project.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import project.coarsegrain.CoarseGrainLFUCache;
import project.sequential.LFUCache;

public class Main {
	private static final String file = "http://people.cs.vt.edu/applewil/CS5510/dict.bin";
	private static final int fileSize = 27;
	private URL fileURL;
	private Cache<Integer, Character> cache;

	public Main(Cache<Integer, Character> cache, float variance, int len, int nThreads) {
		/* Initialize variables */
		try {
			fileURL = new URL(file);
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		}
		this.cache = cache;
		int reads = len / nThreads;
		/* Initialize threads */
		Thread[] threads = new Thread[nThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = makeThread(variance, reads);
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
		System.out.print("\n");
		System.out.println(end + " ms");
	}

	private char readRandom(float variance) {
		int bound = Math.round((new Float(fileSize) - 1) * variance / 100f) + 1;
		int addr = ThreadLocalRandom.current().nextInt(bound);
		return read(addr);
	}

	private synchronized char read(int addr) {
		if (cache.contains(addr)) {
			/* Cache hit */
			return cache.get(addr);
		} else {
			/* Cache miss */
			char read = readHard(addr);
			cache.put(addr, read);
			return read;
		}
	}

	private char readHard(int addr) {
		int read = 0;
		try {
			InputStream inputStream = fileURL.openStream();
			inputStream.skip(addr);
			read = inputStream.read();
			inputStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return (char) (read & 0xFF);
	}

	private Thread makeThread(float variance, int reads) {
		return new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < reads; i++) {
					System.out.print(readRandom(variance));
				}
			}
		});
	}

	public static void main(String[] args) {
		/* Check args */
		if (args.length < 5 || args.length > 6 || Integer.parseInt(args[1]) < 0 || Integer.parseInt(args[1]) > fileSize
				|| Float.parseFloat(args[2]) > 100 || Float.parseFloat(args[2]) < 0 || Integer.parseInt(args[4]) < 1)
			exit("Invalid args");
		float variance = Float.parseFloat(args[2]);
		int len = Integer.parseInt(args[3]);
		int nThreads = Integer.parseInt(args[4]);
		/* Initialize cache */
		int cacheSize = Integer.parseInt(args[1]);
		Cache<Integer, Character> cache = null;
		if ("Sequential".equals(args[0])) {
			if (nThreads > 1)
				exit("Cannot do sequential cache with multiple threads");
			cache = new LFUCache<>(cacheSize);
		} else if ("Coarsegrain".equals(args[0])) {
			cache = new CoarseGrainLFUCache<>(cacheSize);
		} else {
			exit("No cache with name: " + args[0]);
		}
		new Main(cache, variance, len, nThreads);
	}

	private static void exit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
}
