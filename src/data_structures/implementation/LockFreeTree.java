package data_structures.implementation;

import data_structures.Sorted;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
//only needed for toString
import java.util.ArrayList;
import java.util.List;

public class LockFreeTree<Key extends Comparable<Key>> implements Sorted<Key> {
    // State array
	final static int CLEAN = 1, MARK = 2, IFLAG = 3, DFLAG = 4, INTERNAL = 5, LEAF = 6, DUMMY = 7;
    Internal root;

    /*** Class ***/
    /* Update */
    class Update {
        AtomicStampedReference<Info> infoASR;

        Update(Info info, int state) {
            infoASR = new AtomicStampedReference<Info>(info, state);
        }

        int getState() {
            return infoASR.getStamp();
        }

        Info getInfo() {
            return infoASR.getReference();
        }

        Info get(int[] stateHolder) {
            return infoASR.get(stateHolder);
        }

        boolean compareAndSet(Info er, Info nr, int es, int ns) {
            return infoASR.compareAndSet(er, nr, es, ns);
        }

        public Update clone() {
            int[] stateHolder = new int[1];
            Info info = get(stateHolder);

            return new Update(info, stateHolder[0]);
        }
        
        //@Override
        public String toString() {
        	String state = "CLEAN";
        	switch(getState()) {
                case IFLAG:
                    state="IFLAG";
                    break;
                case MARK:
                	state="MARK";
                    break;
                case DFLAG:
                	state="DFLAG";
                    break;
        	}
        	String output = "Update:(info:" + (getInfo() == null ? "null" :getInfo().toString()) +
        					", state:" + state +")";
        	return output;
        }
    }

    /* LFTNode */
    abstract class LFTNode implements Comparable<Key> {
        final static int SIZE = 4;
        Key key;

        abstract boolean isLeaf();
        abstract void toString(List<Key> leafs);

        public int compareTo(Key k) {
            if(k == null)
                return -1;

            if(key == null)
                return 1;

            return key.compareTo(k);
        }
        
        abstract int getType();
    }

    /* Internal LFTNode */
    class Internal extends LFTNode {
        AtomicReference<LFTNode> left, right;
        Update update;

        Internal(Key k, LFTNode l, LFTNode r) {
            key = k;
            left = new AtomicReference<LFTNode>(l);
            right = new AtomicReference<LFTNode>(r);
            update = new Update(null, CLEAN);
        }
        
        boolean isLeaf() {
        	return false;
        }
        
        int getType() {
            return INTERNAL;
        }

        LFTNode getLeft() {
            return left.get();
        }

        LFTNode getRight() {
            return right.get();
        }
        
        public String toString() {
        	String output = key == null ? "" : String.valueOf(key);
			return output;
        }

        void toString(List<Key> lfs) {
            LFTNode l = left.get();
            LFTNode r = right.get();
           	if (l != null)
           		l.toString(lfs);

           	if (r != null)
	       		r.toString(lfs);
        }
    }

    /* Leaf LFTNode */
    class Leaf extends LFTNode {
        Leaf(Key k) {
            key = k;
        }

        boolean isLeaf() {
        	return true;
        }
        
        int getType() {
            return LEAF;
        }
        
        public String toString() {
        	return key.toString();
        }

        void toString(List<Key> leafs) {
            leafs.add(key);
        }
    }
    
    class DummyLeaf extends Leaf {
    	
    	String id = "left";
    	DummyLeaf() {
    		super(null);
    	}
    	
    	DummyLeaf(String _id) {
    		super(null);
    		id = _id;
    	}
    	@Override
    	public int compareTo(Key k) {
    		return -1;
    	}
    	
    	boolean isLeaf() {
        	return true;
        }
    	
    	int getType() {
            return DUMMY;
        }
    	
    	public String toString() {
    		return "";
    	}
    	
    	void toString(List<Key> leafs) {
            return;
        }
    }

    /* Info */
    abstract class Info {
        Leaf l;
    }

    /* IInfo */
    class IInfo extends Info {
        Internal p, newInternal;

        IInfo(Internal p, Leaf l, Internal newInternal) {
            this.p = p;
            this.l = l;
            this.newInternal = newInternal;
        }
        
        public String toString() {
        	return "IInfo:(p:" + p.toString(0) + ", l:" + l.toString(0) + ", new:" + newInternal.toString(0) + ")";
        }
    }

    /* DInfo */
    class DInfo extends Info {
        Internal gp, p;
        Update pupdate;

        DInfo(Internal gp, Internal p, Leaf l, Update pupdate) {
            this.gp = gp;
            this.p = p;
            this.l = l;
            this.pupdate = pupdate;
        }
    }

    /* SearchLFT */
    class SearchLFT {
        Internal gp, p;
        Leaf l;
        Update pupdate, gpupdate;

        SearchLFT(Internal gp, Internal p, Leaf l,
                Update pupdate, Update gpupdate) {
            this.gp = gp;
            this.p = p;
            this.l = l;
            this.pupdate = pupdate == null ? null : pupdate.clone();
            this.gpupdate = gpupdate == null ? null : gpupdate.clone();
        }
        
        public String toString() {
        	String output = "SearchLFT:\tgp:" + ((gp == null) ? "null" : gp.toString()) +
        			"\n\t\tp:" + ((p == null) ? "null" : p.toString()) +
        			"\n\t\tl:" + ((l == null) ? "null" : l.toString()) +
        			"\n\t\tpupdate:" + (pupdate == null ? "null" : pupdate.toString()) +
        			"\n\t\tgpupdate:" + (gpupdate == null ? "null" : gpupdate.toString());
        	return output;
        }
    }

    
    /*** Method ***/
    
    public LockFreeTree() {
        DummyLeaf left = new DummyLeaf(), right = new DummyLeaf("right");
        root = new Internal(null, left, right);
    }
    
    public Key max(Key a, Key b) {
        if (a == null) {
            if (b == null) return a;
            else return b;
        }
        if (b == null)
            return a;
        return a.compareTo(b) > 0 ? a : b;
    }

    /* search */
    private SearchLFT search(Key k) {
        LFTNode l = root;
        Internal p = null, gp = null;
        Update gpupdate = null, pupdate = null;

        while(!l.isLeaf()) {
            gp = p;
            p = (Internal)l;
            gpupdate = pupdate;
            pupdate = p.update;
            l = l.compareTo(k) > 0 ? p.getLeft() : p.getRight();
        }
        return new SearchLFT(gp, p, (Leaf)l, pupdate, gpupdate);
    }
    
    /* add */
    public void add(Key k) {
        Internal newInternal;
        Leaf newSibling, newLeaf = new Leaf(k);
        Update result;
        IInfo op;
        // contains the p and l from the pseudocode
        SearchLFT r;
        int[] expState = new int[1];
        Info expInfo;

        while(true) {
            r = search(k);
            // Do not allow double key occurrences
            if(r.l.compareTo(k) == 0) {
                return;
            }

            if(r.pupdate.getState() != CLEAN) {
                help(r.pupdate);
            } else {
            	if (r.l.key == null) {
            		//this is the dummy leave so we need to construct a new one to save the universal key
            		newSibling = new DummyLeaf();
            		// if we're dealing with dummy this is the first insert
            		// we can construct the new subtree immediately
            		newInternal = new Internal(null, new Leaf(k), newSibling);
            	} else {
            		newSibling = new Leaf(r.l.key);
            		newInternal = r.l.compareTo(k) < 0 
            			? new Internal(max(k, r.l.key), newSibling, newLeaf)
            			: new Internal(max(k, r.l.key), newLeaf, newSibling);
            	}
            	op = new IInfo(r.p, r.l, newInternal);				// New InsertInfo with p,l,newInternal
                // iflag CAS step
                if(r.p.update.compareAndSet(r.pupdate.get(expState), op, expState[0], IFLAG)) {
                	helpInsert(op);		// the iflag CAS was succesful finish the insertion
                    return;
                }
                help(r.p.update);
            }
        }
    }

    /* helpInsert */
    private void helpInsert(IInfo op) {
    	if (op == null) {
    		return;
    	}

    	// ichild CAS step
        casChild(op.p, op.l, op.newInternal);

        // iunflag CAS step
        op.p.update.compareAndSet(op, op, IFLAG, CLEAN);
    }

    /* remove */
    public void remove(Key k) {
        DInfo op;
        SearchLFT r;
        int[] expState = new int[1];
        Info expInfo;

        while(true) {
            r = search(k);

            // Simply return if the key is not in the tree
            if(r.l.compareTo(k) != 0)
                return;

            // Parent and grandparent must be CLEAN in order to perform the
            // remove operation, make sure they are
            if(r.gpupdate.getState() != CLEAN) {
                help(r.gpupdate);
            } else if(r.pupdate.getState() != CLEAN) {
                help(r.pupdate);
            } else {
                op = new DInfo(r.gp, r.p, r.l, r.pupdate);
                // dflag CAS step
                if(r.gp.update.compareAndSet(r.gpupdate.get(expState), op, expState[0], DFLAG)) {
                    if(helpDelete(op))
                        return;
                } else {
                    // Failed to perform dflag CAS. First help other operation
                    help(r.gp.update);
                }
            }
        }
    }

    /* helpDelete */
    private boolean helpDelete(DInfo op) {
    	if (op == null)
    		return false;
    	
        int[] stateHolder = new int[1];
        Info info = op.pupdate.get(stateHolder);

        // mark CAS step
        if(op.p.update.compareAndSet(info, op, stateHolder[0], MARK)) {
            helpMarked(op);
            return true;
        } else {
            //if(info == op && stateHolder[0] == MARK) {
            if(op.p.update.getState() == MARK && op.p.update.getInfo() == op) {
                helpMarked(op);
                return true;
            }

            help(op.p.update);

            // Failed to mark the parent LFTNode. Remove DFLAG and restart.
            // backtrack CAS step
            op.gp.update.compareAndSet(op, op, DFLAG, CLEAN);
            return false;
        }
    }

    /* helpMarked */
    private void helpMarked(DInfo op) {
    	if (op == null)
    		return;
 
        LFTNode right = op.p.getRight(),
             other = right == op.l ? op.p.getLeft() : right;

        // dchild CAS step
        casChild(op.gp, op.p, other);

        // dunflag CAS step
        op.gp.update.compareAndSet(op, op, DFLAG, CLEAN);
    }

    /* help */
    private void help(Update u) {
    	if (u == null)
    		return;
    				
        int[] stateHolder = new int[1];
        Info info = u.get(stateHolder);

        switch(stateHolder[0]) {
            case IFLAG:
                helpInsert((IInfo)info);
                break;
            case MARK:
                helpMarked((DInfo)info);
                break;
            case DFLAG:
                helpDelete((DInfo)info);
                break;
        }
    }

    /* casChild */
    private void casChild(Internal parent, LFTNode oldLFTNode, LFTNode newLFTNode) {
    	if (parent == null || newLFTNode == null) {
    		return;
    	}
    	
    	if(newLFTNode.compareTo(parent.key) < 0) {
    		parent.left.compareAndSet(oldLFTNode, newLFTNode);
    	}
        else {
            parent.right.compareAndSet(oldLFTNode, newLFTNode);
        }
    }
    

    public String toString() {
    	List<Key> leafs = new ArrayList<Key>();
    	root.toString(leafs);
    	return leafs.toString();
    }
}
