package data_structures.implementation;
import data_structures.Sorted;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedList<T extends Comparable<T>> implements Sorted<T> {
    private FineNode<T> head;

    public FineGrainedList() {
        head = new HeadNode();
        head.next = new TailNode();
    }

    public void add(T t) {
        FineNode<T> pred = head;
        head.lock();
        try {
            FineNode<T> curr = pred.next;
            curr.lock();
            try  {
                while(curr.compareTo(t) < 0) {
                    //  if current node is smaller than addNode, keep traversing 
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }

                // when place found, create node and add it
                FineNode<T> newNode = new ListNode(t);
                newNode.next = curr;
                pred.next = newNode;
            }
            finally {
                curr.unlock();
            }
        }
        finally {
            pred.unlock();
        }
    }

    public void remove(T t) {
        FineNode<T> pred = head;
        head.lock();
        try {
            FineNode<T> curr = pred.next;
            curr.lock();
            try {
                while(curr.compareTo(t) < 0) {
                    // if current node is smaller than addNode, keep traversing 
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if(curr.compareTo(t) == 0)
                    // when removeNode found, remove it.
                    pred.next = curr.next;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }
    
    public String toString() {
    	String output = "";
    	FineNode<T> pred = head;
        pred.lock();
        try {
            FineNode<T> curr = pred.next;
            curr.lock();
            try {
                while(curr.next != null) {
                    // traverse the list until reaching tail node.
                	output += curr.data;
                	pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                    if (curr.next != null) output += ", ";
                }
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
        return "["+output+"]";
    }

  /* FindNode class */
    class FineNode<T extends Comparable<T>> 
    {
    	public T data;
    	public FineNode<T> next = null;
    	public Lock lock = new ReentrantLock();

    	public FineNode(T data) {
    		this.data = data;
    		this.next = null;
    	}
    	
    	public FineNode() {
    		this.data = null;
    		this.next = null;
    	}
    	
    	 public void lock() {
             lock.lock();
         }

         public void unlock() {
             lock.unlock();
         }
         
     	 int compareTo(T t) {
    		
    		return this.data.compareTo(t);
    	}
    }

    class ListNode extends FineNode<T> {
        public ListNode(T t) {
            data = t;
        }

        int compareTo(T t) {
            return data.compareTo(t);
        }
    }

    class HeadNode extends FineNode<T> {
        int compareTo(T t) {
            return -1;
        }
    }

    class TailNode extends FineNode<T> {
        int compareTo(T t) {
            return 1;
        }
    }
}
