package benchmark;

import java.util.Random;
import lists.*;

public class MicroBenchmark extends Thread implements ThreadId {
	private static int ID_GEN = 0;

	private volatile Set<Integer> set;
	private volatile int id;
	private volatile int warmup;
	private volatile int repeat;
	private volatile int add_percentage;
	private volatile int contains_perentage;
	private volatile Random rand;
	private volatile int rand_val;
	private volatile long elapsed;
	
	public MicroBenchmark(Set<Integer> set, 
						  int warmup,
						  int repeat,
//						  int contains_perentage,
						  int add_percentage,
						  int rm_percentage,
						  int id) {
		this.id = id;
		this.set = set;
		this.warmup = warmup;
		this.repeat = repeat;
		this.add_percentage = add_percentage;
		this.contains_perentage = 100 - rm_percentage;
		this.rand = new Random();
		this.rand_val = 0;
	}
	
	@Override
	public void run() {
		// warmup
		for(int i = 0; i < this.warmup; i++) {
			this.rand_val = this.rand.nextInt(100);
			if (this.rand_val < this.add_percentage) {
				this.rand_val = this.rand.nextInt(100);
				set.add(this.rand_val);
			}
			else if(this.rand_val < this.contains_perentage) {
				this.rand_val = this.rand.nextInt(100);
				set.contains(this.rand_val);
			}
			else {
				this.rand_val = this.rand.nextInt(100);
				set.remove(this.rand_val);
			}
		}
		
		// repeat
		long start = System.currentTimeMillis();
		for(int i = 0; i < this.repeat; i++) {
			this.rand_val = this.rand.nextInt(100);
			if (this.rand_val < this.add_percentage) {
				this.rand_val = this.rand.nextInt(100);
				set.add(this.rand_val);
			}
			else if(this.rand_val < this.contains_perentage) {
				this.rand_val = this.rand.nextInt(100);
				set.contains(this.rand_val);
			}
			else {
				this.rand_val = this.rand.nextInt(100);
				set.remove(this.rand_val);
			}
		}
		this.elapsed = System.currentTimeMillis() - start;
	}

	@Override
	public int getThreadId(){
		return this.id;
	}

	public long getElapsedTime() {
		return elapsed;
	}
//	public static void bench(String name, 
//							 long runMillis, 
//							 int loop, 
//							 int warmup, 
//							 int repeat, 
//							 Runnable runnable) {
//
//		System.out.println("Running: " + name);
//		int max = repeat + warmup;
//		long average = 0L;
//		for (int i = 0; i < max; i++) {
//			long nops = 0;
//			long duration = 0L;
//			long start = System.currentTimeMillis();
//			while (duration < runMillis) {
//				for (int j = 0; j < loop; j++) {
//					runnable.run();
//						nops++;
//		}
//			          duration = System.currentTimeMillis() - start;
//			        }
//			        long throughput = nops / duration;
//			        boolean benchRun = i >= warmup;
//			        if (benchRun) {
//			          average = average + throughput;
//			        }
//			        System.out.print(throughput + " ops/ms" + ([
//			  !benchRun ? " (warmup) | " : " | "));
//			      }
//			      average = average / repeat;
//			      System.out.println("\n[ ~" + average + " ops/ms ]\n");
//	}

}
