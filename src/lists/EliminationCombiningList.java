package lists;

import util.*;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


//import lists.EliminationBackoffList.Cell;
//import lists.EliminationBackoffList.Operation;

/**
 * List using coarse-grained synchronization.
 * 
 * {@link CoarseList.java and EliminationBackoffStack.java}
 * 
 * @param T Item type.
 */
public class EliminationCombiningList<T> implements lists.Set<T> {
	private Node head;
	private Node tail;
	private final int capacity;
	private final int timemout;
	Random random;
	private Lock lock = new ReentrantLock();
	util.EliminationArray<Cell<T>> eliminationArray;

	// cell status
	static final int INIT = 0, FINISHED = 1, RETRY = 2;

	// combination queue stamp
//	static final int UNMODIFIED = 0, MODIFIED = 1;
//	ConcurrentLinkedQueue<AtomicStampedReference<T>>[] combinationQueue;
	ConcurrentLinkedQueue<Cell<T>>[] combinationArray;

	private enum Operation {
		ADD, REMOVE
	}

	@SuppressWarnings("unchecked")
	public EliminationCombiningList(int capacity, int timeout) {
		// Add sentinels to start and end
		this.head = new Node<T>(Integer.MIN_VALUE);
		head.next = new Node<T>(Integer.MAX_VALUE);

		this.random = new Random();
		this.capacity = capacity;
		this.timemout = timeout;
		this.eliminationArray = new EliminationArray<Cell<T>>(this.capacity, this.timemout);
		this.combinationArray = (ConcurrentLinkedQueue<Cell<T>>[]) new ConcurrentLinkedQueue[this.capacity];
		for (int i = 0; i < this.capacity; i++) {
			this.combinationArray[i] = new ConcurrentLinkedQueue<Cell<T>>();
		}
//		for (int i = 0; i < this.capacity; i++) {
//			if (combinationArray[i] == null) {
//				System.out.println("null  " + i);
//			}
////			this.combinationArray[i] = new ConcurrentLinkedQueue<Cell<T>>();
//		}
////		this.combinationArray[0].add(new Cell(Operation.ADD, 1));
//		System.exit(0);
		
	}

	void combineOperation() {
		int slot = random.nextInt(this.capacity);
		int count = slot;
		do {
			if (!this.combinationArray[count%this.capacity].isEmpty())
				break;
			count += 1;
		} while (count % this.capacity != slot);
		if (this.combinationArray[count % this.capacity].isEmpty()) {
			return;
		} else {
			slot = count % this.capacity;
		}

		while (true) {
			Cell<T> value = this.combinationArray[slot].poll();
			if (value != null) {
				if (value.status.get() == INIT) {
					boolean result;
					if (value.operation == Operation.ADD) {
						result = tryAdd(value.data);
					} else {
						result = tryRemove(value.data);
					}
					value.operationResult = result;
					value.status.compareAndSet(INIT, FINISHED);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Add an element.
	 * 
	 * @param item element to add
	 * @return true iff element was not there already
	 */

	public boolean add(T item) {
		while (true) {
			boolean isLocked = lock.tryLock();
			if (isLocked) {
				try {
					combineOperation();
					return tryAdd(item);
				} finally {
					lock.unlock();
				}
			} else {
				Cell<T> value = new Cell<T>(Operation.ADD, item);
				try {
					Cell<T> exchange = eliminationArray.visit(value);
					// eliminate
					if (exchange.operation == Operation.REMOVE && exchange.data == item) {
						return true;
					}
					// eliminat-combining
					else {
						int slot = random.nextInt(this.capacity);
						// empty combining queue
						if (this.combinationArray[slot].isEmpty()) {
							this.combinationArray[slot].add(value);
							long upperBound = System.currentTimeMillis() + this.timemout;
							while (value.status.get() != FINISHED) {
								if (System.currentTimeMillis() > upperBound
										&& value.status.compareAndSet(INIT, RETRY)) {
									break;
								}
							}
							if (value.status.get() == FINISHED)
								return true;
							if (value.status.get() == RETRY)
								return false;
						}
						// non-empty
						else {
							Cell<T> tmp = this.combinationArray[slot].peek();
							// top node is reverse semantic of current one, eliminate
							if (tmp != null && tmp.status.get() == INIT && Operation.REMOVE == tmp.operation && tmp.data == item) {
								if (tmp.status.compareAndSet(INIT, FINISHED))
									return true;
							}
							// current node and top node are identical semantic, combination
							else if (tmp != null && Operation.ADD == tmp.operation) {
								this.combinationArray[slot].add(value);
								long upperBound = System.currentTimeMillis() + this.timemout;
								while (value.status.get() != FINISHED) {
									if (System.currentTimeMillis() > upperBound
											&& value.status.compareAndSet(INIT, RETRY)) {
										break;
									}
								}
								if (value.status.get() == FINISHED)
									return value.operationResult;
								if (value.status.get() == RETRY)
									return false;
							}
						}
					}
				} catch (TimeoutException e) {
				}
			}

		}
	}

	boolean tryAdd(T item) {
		int key = item.hashCode();
		Node<T> pred = head;
		Node<T> curr = pred.next;
		while (curr.key < key) {
			pred = curr;
			curr = curr.next;
		}
		if (curr.key == key) {
			return false;
		}
		Node<T> node = new Node<T>(item);
		node.next = curr;
		pred.next = node;
		return true;
	}

	/**
	 * Remove an element.
	 * 
	 * @param item element to remove
	 * @return true iff element was present
	 */
	public boolean remove(T item) {
		while (true) {
			boolean isLocked = lock.tryLock();
			if (isLocked) {
				try {
					combineOperation();
					return tryRemove(item);
				} finally {
					lock.unlock();
				}
			} else {
				Cell<T> value = new Cell<T>(Operation.REMOVE, item);
				try {
					Cell<T> exchange = eliminationArray.visit(value);
					// eliminate
					if (exchange.operation == Operation.ADD && exchange.data == item) {
						return true;
					}
					// eliminat-combining
					else {
						int slot = random.nextInt(this.capacity);
						// empty combining queue
						if (this.combinationArray[slot].isEmpty()) {
							this.combinationArray[slot].add(value);
							long upperBound = System.currentTimeMillis() + this.timemout;
							while (value.status.get() != FINISHED) {
								if (System.currentTimeMillis() > upperBound
										&& value.status.compareAndSet(INIT, RETRY)) {
									break;
								}
							}
							if (value.status.get() == FINISHED)
								return true;
							if (value.status.get() == RETRY)
								return false;
						}
						// non-empty
						else {
							Cell<T> tmp = this.combinationArray[slot].peek();
							// top node is reverse semantic of current one, eliminate
							if (tmp != null && tmp.status.get() == INIT && Operation.ADD == tmp.operation && tmp.data == item) {
								if (tmp.status.compareAndSet(INIT, FINISHED))
									return true;
							}
							// current node and top node are identical semantic, combination
							else if (tmp != null && Operation.REMOVE == tmp.operation) {
								this.combinationArray[slot].add(value);
								long upperBound = System.currentTimeMillis() + this.timemout;
								while (value.status.get() != FINISHED) {
									if (System.currentTimeMillis() > upperBound
											&& value.status.compareAndSet(INIT, RETRY)) {
										break;
									}
								}
								if (value.status.get() == FINISHED)
									return value.operationResult;
								if (value.status.get() == RETRY)
									return false;
							}
						}
					}
				} catch (TimeoutException e) {
				}

			}
		}
	}

	@SuppressWarnings("unchecked")
	boolean tryRemove(T item) {
		int key = item.hashCode();
		Node<T> pred = head;
		Node<T> curr = pred.next;
		while (curr.key < key) {
			pred = curr;
			curr = curr.next;
		}
		if (key == curr.key) {
			pred.next = curr.next;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Test whether element is present
	 * 
	 * {@link CoarseList.java contains}
	 * 
	 * @param item element to test
	 * @return true iff element is present
	 */
	public boolean contains(T item) {
		Node<T> pred, curr;
		int key = item.hashCode();
		lock.lock();
		try {
			pred = head;
			curr = pred.next;
			while (curr.key < key) {
				pred = curr;
				curr = curr.next;
			}
			return (key == curr.key);
		} finally { // always unlock
			lock.unlock();
		}
	}

	/**
	 * list Node
	 * 
	 * {@link CoarseList.java Node class}
	 */
	private class Node<T> {
		private T item;
		int key;
		Node<T> next;

		@SuppressWarnings("unused")
		Node(T item) {
			this.setItem(item);
			this.key = item.hashCode();
			this.next = null;
		}

		Node(int key) {
			this.setItem(null);
			this.key = key;
			this.next = null;
		}

		@SuppressWarnings("unused")
		public T getItem() {
			return item;
		}

		public void setItem(T item) {
			this.item = item;
		}
	}

	@SuppressWarnings("hiding")
	private class Cell<T> {
		int key;
		volatile T data;
		Operation operation;
		AtomicInteger status;
		boolean operationResult;

		@SuppressWarnings("unused")
		public Cell(Operation operation, T data) {
			this.operation = operation;
			this.data = data;
			status = new AtomicInteger(INIT);
		}

	}

}