package data_structures.implementation;

import java.util.concurrent.atomic.AtomicMarkableReference;

import data_structures.Sorted;
import data_structures.implementation.FineGrainedList.FineNode;

public class LockFreeList<T extends Comparable<T>> implements Sorted<T> {

	private LockFreeNode<T> head;
	private LockFreeNode<T> tail;
	
	public LockFreeList() {
		head = new HeadNode();
		tail = new TailNode();
		head.next = new AtomicMarkableReference<LockFreeList<T>.LockFreeNode<T>>(tail, false);
	}
	
	public void add(T t) {
		while (true) {
			Window window = find(head, t);
			LockFreeNode<T> pred = window.pred, curr = window.curr;
			LockFreeNode<T> node = new LockFreeNode<T>(t);
			node.next = new AtomicMarkableReference<LockFreeNode<T>>(curr, false);
			if (pred.next.compareAndSet(curr,  node, false, false)) {
				return;// true;
			}
		}
	}

	public void remove(T t) {
		boolean snip;
		while (true) {
			Window window = find(head, t);
			LockFreeNode<T> pred = window.pred, curr = window.curr;
			LockFreeNode<T> succ = curr.next.getReference();
			// try to mark curr's next ref
			snip =  curr.next.compareAndSet(succ,  succ,  false,  true);
			if (!snip)
				continue; // if failed to mark restart
			// else set pred's next ref to succ so curr is unrefed, set mark to false
			pred.next.compareAndSet(curr, succ,  false, false);
			// if it fails we don't care since we could mark someone already helped
			return;
		}
	}
	
	public String toString() {
		String output = "";
		boolean[] marked = {false};
    	LockFreeNode<T> pred = head;
        LockFreeNode<T> curr = pred.next.get(marked);
        while(curr.next.get(marked) != null) {
            // traverse the list until reaching tail node.
        	output += curr.data;
            pred = curr;
            curr = curr.next.get(marked);
            if (curr.next.get(marked) != null) output += ", ";
        }
        return "["+output+"]";
	}
	
	class LockFreeNode<T extends Comparable<T>> 
    {
    	public T data;
    	public AtomicMarkableReference<LockFreeNode<T>> next;

    	public LockFreeNode(T data) {
    		this.data = data;
    		this.next = new AtomicMarkableReference<LockFreeNode<T>>(null, false);
    	}
    	
    	public LockFreeNode() {
    		this.data = null;
    		this.next = new AtomicMarkableReference<LockFreeNode<T>>(null, false);
    	}
    	
    	int compareTo(T t) {	
    		return this.data.compareTo(t);
    	}
    }
	
	class HeadNode extends LockFreeNode<T> {
        int compareTo(T t) {
            return -1;
        }
    }
	
	class TailNode extends LockFreeNode<T> {
        int compareTo(T t) {
            return 1;
        }
    }
	
	class Window {
		public LockFreeNode<T> pred, curr;
		Window(LockFreeNode<T> myPred, LockFreeNode<T> myCurr) {
			pred = myPred; 
			curr = myCurr;
		}
	}
	
	public Window find(LockFreeNode<T> head, T item) {
		LockFreeNode<T> pred = null, curr = null, succ = null;
		boolean[] marked = {false};
		boolean snip;
		retry: while (true) {
			pred = head;
			curr = pred.next.getReference();
			while (true) {
				// succeeding node
				succ = curr.next.get(marked);
				while (marked[0]) {
					snip = pred.next.compareAndSet(curr, succ, false, false);
					if (!snip) continue retry;
					curr = succ;
					succ = curr.next.get(marked);
				}
				if (curr.compareTo(item) >= 0)
					return new Window(pred, curr);
				pred = curr;
				curr = succ;
			}
		}
	}
}
