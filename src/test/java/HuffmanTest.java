import huffman.Huffman;
import huffman.Huffman.EncodeTable;
import huffman.Word;
import java.io.IOException;
import java.util.Map.Entry;
import org.junit.Test;
import tree.Tree;

/**
 * huffman test
 *
 * @author bullet
 * @time 2017-09-20 下午5:00
 */
public class HuffmanTest {

  private String testDir = "./src/test/resources/";

  @Test
  public void createHuffmanTreeAndEncodeTableTest() {

    System.out.println(testDir);
    Huffman huffman = new Huffman();
    Byte[] bytes = new Byte[] { 'A', 'B', 'C', 'D', 'E', 'F'};
    double[] frenquencies = new double[] { 45, 13, 12, 16, 9, 5};
    Word<Byte>[] words = new Word[bytes.length];
    for (int i = 0; i < words.length; i++) {
      Word<Byte> temp = new Word<>();
      temp.symbol = bytes[i];
      temp.frequency = frenquencies[i];
      words[i] = temp;
    }
    Tree<Word<Byte>> tree = huffman.createHuffmanTree(words);
    System.out.println("111");

    huffman.createHuffmanEncodeTable(tree);
    System.out.println("222");

  }


  // yhl.hfm should be 7900
  @Test
  public void compressFileTest() throws IOException {    Huffman huffman = new Huffman();
    Byte[] bytes = new Byte[] { 'Y', 'H', 'L'};
    double[] frenquencies = new double[] { 3, 2, 1};
    Word<Byte>[] words = new Word[bytes.length];
    for (int i = 0; i < words.length; i++) {
      Word<Byte> temp = new Word<>();
      temp.symbol = bytes[i];
      temp.frequency = frenquencies[i];
      words[i] = temp;
    }
    Tree<Word<Byte>> tree = huffman.createHuffmanTree(words);

    EncodeTable<Byte> encodeTable = huffman.createHuffmanEncodeTable(tree);
    for (Entry<Byte, String> encode : encodeTable.table.entrySet()) {
      System.out.println(encode.getKey() + "=" + encode.getValue());
    }
    huffman.compressFile(testDir + "yhl.txt", testDir + "yhl.hfm");
  }

  @Test
  public void uncompressFileTest() throws IOException, ClassNotFoundException {
    Huffman huffman = new Huffman();
    huffman.uncompressFile(testDir + "yhl.hfm", testDir + "yhl.uncompress.txt");
  }

}
