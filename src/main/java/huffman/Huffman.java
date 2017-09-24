package huffman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import tree.Tree;
import tree.Tree.Node;

/**
 * huffman
 *
 * @author bullet
 * @time 2017-09-20 下午4:17
 */
public class Huffman {

  public <T> Tree<Word<T>> createHuffmanTree(Word<T>[] words) {

    if (words.length == 0) {
      return null;
    }

    Comparator<Node<Word<T>>> comparator = new Comparator<Node<Word<T>>>() {
      @Override
      public int compare(Node<Word<T>> o1, Node<Word<T>> o2) {
        if (o1.data.frequency > o2.data.frequency) {
          return 1;
        } else if (o1.data.frequency == o2.data.frequency) {
          return 0;
        } else {
          return -1;
        }
      }
    };

    PriorityQueue<Node<Word<T>>> priorityQueue = new PriorityQueue<>(comparator);
    for (Word<T> word : words) {
      Node<Word<T>> node = new Node();
      node.data = word;
      priorityQueue.add(node);
    }

    while (priorityQueue.size() > 1) {
      Node<Word<T>> node = new Node<>();
      node.left = priorityQueue.poll();
      node.right = priorityQueue.poll();
      node.data = new Word<T>();
      node.data.symbol = null;
      node.data.frequency = node.left.data.frequency + node.right.data.frequency;
      priorityQueue.add(node);
    }

    Tree<Word<T>> tree = new Tree<>();
    tree.root = priorityQueue.poll();
    return tree;
  }

  public <T> EncodeTable<T> createHuffmanEncodeTable(Tree<Word<T>> tree) {
    EncodeTable<T> encodeTable = new EncodeTable();
    Node root = tree.root;
    traverForEncode(root, "", encodeTable);
    return encodeTable;
  }

  private <T> void traverForEncode(Node<Word<T>> node, String prefix,
      EncodeTable<T> encodeTable) {
    if (node.left == null && node.right == null) {
      encodeTable.table.put(node.data.symbol, prefix);
      return;
    }

    if (node.left != null) {
      traverForEncode(node.left, prefix + "0", encodeTable);
    }

    if (node.right != null) {
      traverForEncode(node.right, prefix + "1", encodeTable);
    }

    return;
  }

  public String compressFile(String source, String dist) throws IOException {
    BufferedInputStream bin = new BufferedInputStream(new FileInputStream(source));
    byte[] bytes = new byte[bin.available()];
    bin.read(bytes);
    bin.close();

    Map<Byte, Integer> map = new HashMap<>();
    for (Byte aByte : bytes) {
      if (!map.containsKey(aByte)) {
        map.put(aByte, 1);
      } else {
        map.put(aByte, map.get(aByte) + 1);
      }
    }
    Word<Byte>[] words = new Word[map.size()];
    int i = 0;
    for (Entry<Byte, Integer> byteIntegerEntry : map.entrySet()) {
      Word word = new Word<Byte>();
      word.symbol = byteIntegerEntry.getKey();
      word.frequency = byteIntegerEntry.getValue();
      words[i++] = word;
    }

    Huffman huffman = new Huffman();
    HuffmanConfiguration<Byte> huffmanConfiguration = new HuffmanConfiguration();
    Tree<Word<Byte>> tree = huffman.createHuffmanTree(words);
    EncodeTable<Byte> encodeTable = huffman.createHuffmanEncodeTable(tree);
    huffmanConfiguration.encodeTable = encodeTable;
    huffmanConfiguration.tree = tree;
    byte[] encodeBytes = getEncodeBytesAndFillConfigurationSize(bytes, huffmanConfiguration);

    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dist));
    bos.write(encodeBytes);
    bos.close();

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dist + ".config"));
    oos.writeObject(huffmanConfiguration);
    oos.close();

    return dist;
  }

  private byte[] getEncodeBytesAndFillConfigurationSize(byte[] bytes, HuffmanConfiguration<Byte> huffmanConfiguration) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      sb.append(huffmanConfiguration.encodeTable.table.get(aByte));
    }
    String encodeCode = sb.toString();

    byte temp = 0x00;
    int currentLength = 0;
    long sum = 0;
    for (int i = 0; i < encodeCode.length(); i++) {
      if (currentLength == 8) {
        baos.write(temp);
        temp = 0x00;
        currentLength = 0;
      }
      sum += 1;
      temp <<= 1;
      if (encodeCode.charAt(i) == '1') {
        temp += 1;
      }
      currentLength++;
    }

    while (currentLength++ < 8) {
      temp <<= 1;
    }
    baos.write(temp);

    huffmanConfiguration.size = sum;

    return baos.toByteArray();
  }

  public static class EncodeTable<T> implements Serializable{

    public Map<T, String> table = new HashMap<>();
  }

  public static class HuffmanConfiguration<T> implements Serializable {
    // 压缩文件的长度，单位是bit
    public long size;
    public EncodeTable<T> encodeTable;
    public Tree<Word<T>> tree;
  }

  public String uncompressFile(String source, String dist)
      throws IOException, ClassNotFoundException {

    String configPath = source + ".config";
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configPath));
    HuffmanConfiguration<Byte> huffmanConfiguration = (HuffmanConfiguration<Byte>) ois.readObject();
    ois.close();

    BufferedInputStream bin = new BufferedInputStream(new FileInputStream(source));
    byte[] bytes = new byte[bin.available()];
    bin.read(bytes);
    bin.close();

    byte temp = 0x00;
    long byteCount = huffmanConfiguration.size / Byte.SIZE;
    long remainBitCount = huffmanConfiguration.size % Byte.SIZE;
    StringBuilder sb = new StringBuilder();

    // 以下的代码写的这么复杂，是因为long类型不能作为数组下标
    long current = 0;
    for (byte aByte : bytes) {
      if (current > byteCount) {
        break;
      }
      if (current == byteCount) {
        temp = aByte;                   // 当完成所有的完整的byte时，将后一个字节（即包含有部分有效位的字节）赋值给temp
        break;
      }
      temp = aByte;
      for (int i = 0; i < Byte.SIZE; i++) {
        if ((temp & 0x80) == 0x00) {
          sb.append("0");
        } else {
          sb.append("1");
        }
        temp <<= 1;
      }
      current++;
    }
    for (int i = 0; i < remainBitCount; i++) {
      if ((temp & 0x80) == 0x00) {
        sb.append("0");
      } else {
        sb.append("1");
      }
      temp <<= 1;
    }
    String encodeCode = sb.toString();
    byte[] result = getBytesFromEncode(encodeCode, huffmanConfiguration);

    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dist));
    bos.write(result);
    bos.close();

    return dist;
  }

  private byte[] getBytesFromEncode(String encodeCode,
      HuffmanConfiguration<Byte> huffmanConfiguration) {

    Tree<Word<Byte>> tree = huffmanConfiguration.tree;

    Node<Word<Byte>> current = tree.root;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for (int i = 0; i < encodeCode.length(); i++) {
      if (encodeCode.charAt(i) == '0') {
        current = current.left;
      } else {
        current = current.right;
      }
      if (current.left == null && current.right == null) {
        baos.write(current.data.symbol);
        current = tree.root;
      }
    }

    byte[] bytes = baos.toByteArray();
    return bytes;
  }

}
