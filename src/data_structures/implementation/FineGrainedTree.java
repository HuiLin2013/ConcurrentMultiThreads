package data_structures.implementation;

import data_structures.Sorted;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedTree<T extends Comparable<T>> implements Sorted<T> {
   
    private FineNode root = new FineNode(null);

	public void add(T t) {
        FineNode curr, pred = root, next;
        boolean left;

        // Dummy FineNode for locking purposes
    	// the actual root is root.left
        root.lock();
        try {
            // first check if root exists
            if(root.left == null) {
                // the tree is empty, create root node
                root.left = new FineNode(t);
            } else { 
            	// traverse the tree until a free leaf is found.
                curr = root.left;
                curr.lock();
                try {
                    while(true) {
                        // when addNode is smaller than or equal to current node
                    	// it goes to the left
                        left = curr.compareTo(t) >= 0;
                        next = left ? curr.left : curr.right;
                        if(next == null) {
                            // if leaf not available, item is new leaf.
                            if(left)
                                curr.left = new FineNode(t);
                            else
                                curr.right = new FineNode(t);
                            return;
                        } else { 
                            // Continue traversal in the subtree
                            pred.unlock();
                            pred = curr;
                            curr = next;
                        }
                        curr.lock();
                    }
                } finally {
                    curr.unlock();
                }
            }
        } finally {
            pred.unlock();
        }
    }

    public void remove(T t) {
        FineNode curr, pred = root;
        
        // Dummy FineNode for locking purposes
    	// the actual root is root.left
        root.lock();
        try {
            curr = root.left;

            if(curr == null) {
                return;
            }

            // traverse the tree to find the removeNode to remove
            curr.lock();
            try {
                while(true) {
                    if(curr.compareTo(t) == 0) {
                        // removeNode found, now remove (with 3 cases)
                        if(curr.left != null && curr.right != null) {
                            // Case 1: removeNode has two children
                            T newdata = searchAndRemoveMinData(curr);
                            curr.data = newdata;
                        } else if(curr.left != null) {
                            // Case 2: 
                            // removeNode only has a left child
                            replaceFineNode(curr, pred, curr.left);
                        } else if(curr.right != null) {
                            // remNode only has a right child
                            replaceFineNode(curr, pred, curr.right);
                        } else {
                            // Case 3: removeNode without children, remove link from pred
                            replaceFineNode(curr, pred, null);
                        }
                        return;
                    } else { 
                    	// removeNode not yet found, continue traversal.
                        pred.unlock();
                        pred = curr;
                        curr = curr.compareTo(t) >= 0 ? curr.left : curr.right;

                        if(curr == null)
                            return;

                        curr.lock();
                    }
                }
            } finally {
                if(curr != null)
                    curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

  
    private T searchAndRemoveMinData(FineNode parent) {
        FineNode pred = parent, curr = parent.right;

        curr.lock();
        try {
            while(true) { 
            	// find the removeNode with the smallest data in the right subtree
                if(curr.left != null) { 
                // when there is a left child, continue traverse
                    pred = curr;
                    curr = curr.left;
                    curr.lock();
                    pred.unlock();
                } else {
                    replaceFineNode(curr, pred, curr.right);
                    return curr.data;
                }
            }
        } finally {
            curr.unlock();
        }
    }

 
    private void replaceFineNode(FineNode curr, FineNode parent, FineNode replaceNode) {
        // if Node is not root, replaceNode now replaces
        // current node as child of parent.
        if(parent.data == null || curr == parent.left) {
            // left child of parent.
            parent.left = replaceNode;
        } else {
            // right child of parent.
            parent.right = replaceNode;
        }
    }

    public String toString() {
        return root.isLeaf() ? "[]" : "["+root.left.printString(FineNode.SIZE)+"]";
    }

    
    /* FindNode class */
    class FineNode implements Comparable<T> {
        final static int SIZE = 4;
        T data = null;
        FineNode left = null, right = null;
        Lock lock = new ReentrantLock();

        public FineNode(T t) {
            data = t;
        }

        public int compareTo(T t) {
            return data.compareTo(t);
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        public String printString() {
            return printString(SIZE);
        }

        public String printString(int indt) {
            if(this.isLeaf())
                return data.toString();

            String output = data.toString();
	    if (left != null) output += ", " + left.printString(indt + SIZE);
	    if (right != null) output += ", " + right.printString(indt + SIZE);
            return output;
        }
    }
}
