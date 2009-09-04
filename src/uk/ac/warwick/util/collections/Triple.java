package uk.ac.warwick.util.collections;

/**
 * Similar to pair, but for triplets
 */
public final class Triple<L,M,R> {
    private final L left;
    private final M middle;
    private final R right;
	public Triple (L left, M middle, R right){
		this.left = left;
		this.middle = middle;
		this.right = right;		
	}
	public L getLeft() {
		return left;
	}
	public M getMiddle() {
		return middle;
	}
	public R getRight() {
		return right;
	}
	
	public static <L,M,R> Triple<L,M,R> of(L left, M middle, R right){
		return new Triple<L,M,R>(left, middle, right);
	}
}
