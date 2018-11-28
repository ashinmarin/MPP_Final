import lists.*;
import benchmark.*;
import java.lang.reflect.InvocationTargetException;

public class Measurement {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (args.length != 4) {
			throw new IllegalArgumentException(
					"Please use cmd: java Measurement <Lock Name> <# of thread> <add percentage> <rm percentage>");
		}
		String lockClass = args[0];
		int THREAD_COUNT = Integer.parseInt(args[1]);
		int add_percentage = Integer.parseInt(args[2]);
		int rm_percentage = Integer.parseInt(args[3]);
		int WARMUP = 5;
		int Timeout = 100;
		int ITERS = 1000;

		boolean valid = true;
		Set<Integer> set = null;
		switch (lockClass) {
		case "Coarse":
			set = new CoarseList();
			break;
		case "Fine":
			set = new FineList();
			break;
		case "Lazy":
			set = new LazyList();
			break;
		case "LockFree":
			set = new LockFreeList();
			break;
		case "Optimistic":
			set = new OptimisticList();
			break;
		case "EliminationBackoff":
			set = new EliminationBackoffList(THREAD_COUNT, Timeout);
			break;
		case "EliminationCombining":
			set = new EliminationCombiningList(THREAD_COUNT, Timeout);
			break;
		default:
			valid = false;
			break;
		}

		if (valid == false) {
			throw new IllegalArgumentException("Invalid list");
		}

		MicroBenchmark[] threads;
		long elapsed_time = 0;
		for (int i = 0; i < WARMUP; i++) {
			threads = new MicroBenchmark[THREAD_COUNT];

			long start = System.currentTimeMillis();
			for (int t = 0; t < THREAD_COUNT; t++) {
				threads[t] = new MicroBenchmark(set, WARMUP, ITERS, add_percentage, rm_percentage, t);
			}

			for (int t = 0; t < THREAD_COUNT; t++) {
				threads[t].start();
			}

			for (int t = 0; t < THREAD_COUNT; t++) {
				threads[t].join();
			}

			elapsed_time = System.currentTimeMillis() - start;
		}
		System.out.println(
				lockClass + "List -- " + "Avg. Throughput (ops/ms): " + (double) (ITERS * THREAD_COUNT / elapsed_time));
	}
}
