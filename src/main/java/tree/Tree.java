package tree;

import java.io.Serializable;

/**
 * tree
 *
 * @author bullet
 * @time 2017-09-20 下午4:13
 */
public class Tree<T> implements Serializable {

  public Node<T> root;

  public static class Node<T> implements Serializable{
    public T data;
    public Node<T> left;
    public Node<T> right;

    public <T> void preTraverse(Node node, TreeTraverseListener<T> listener) {
      if (node == null)
        return ;
      listener.onTraversal(node);
      if (node.left != null) {
        node.left.preTraverse(node.left, listener);
      }
      if (node.right != null) {
        node.right.preTraverse(node.right, listener);
      }
    }
  }

  @FunctionalInterface
  public interface TreeTraverseListener<T> {
    void onTraversal(Node<T> element);
  }

  public void preTraverse(TreeTraverseListener<T> listener) {
    Node node = this.root;
    node.preTraverse(node, listener);
  }

}
