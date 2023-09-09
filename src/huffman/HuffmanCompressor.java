package huffman;

import java.io.*;
import java.util.PriorityQueue;


public class HuffmanCompressor {
    private static final int BYTE_SIZE = 8;

    public static void compressFile(String inputFile, String outputFile) {
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            DataOutputStream dos = new DataOutputStream(fos);

            // Read file and count frequencies
            int[] frequencies = new int[256];
            int totalChars = 0;
            int byteRead;
            while ((byteRead = fis.read()) != -1) {
                frequencies[byteRead]++;
                totalChars++;
            }
            for(int i =0;i<frequencies.length;i++){
                System.out.println((char) i+" "+frequencies[i]);
            }

            // Build Huffman tree
            HuffmanNode root = buildHuffmanTree(frequencies);

            // Generate Huffman codes
            String[] codes = new String[256];
            generateCodes(root, "", codes);

            // Write total characters to the output file
            dos.writeInt(totalChars);

            // Write Huffman tree to the output file //Header 
            writeHuffmanTree(root, dos);

            // Reset file pointer
            fis.getChannel().position(0);

            // Compress the file
            int bitBuffer = 0;
            int bitCount = 0;
            while ((byteRead = fis.read()) != -1) {
                String code = codes[byteRead];
                for (char c : code.toCharArray()) {
                    bitBuffer <<= 1;
                    if (c == '1') {
                        bitBuffer |= 1;
                    }
                    bitCount++;
                    if (bitCount == BYTE_SIZE) {
                        dos.write(bitBuffer);
                        bitBuffer = 0;
                        bitCount = 0;
                    }
                }
            }

            // Write remaining bits to the output file
            if (bitCount > 0) {
                bitBuffer <<= (BYTE_SIZE - bitCount);
                dos.write(bitBuffer);
            }

            // Close the streams
            fis.close();
            dos.close();

            System.out.println("File compressed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompressFile(String inputFile, String outputFile) {
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            DataInputStream dis = new DataInputStream(fis);

            // Read total characters from the input file
            int totalChars = dis.readInt();

            // Read Huffman tree from the input file
            HuffmanNode root = readHuffmanTree(dis);

            // Decompress the file
            int bitBuffer = 0;
            int bitCount = 0;
            HuffmanNode currentNode = root;
            int charCount = 0;
            while (charCount < totalChars) {
                if (bitCount == 0) {
                    bitBuffer = dis.read();
                    bitCount = BYTE_SIZE;
                }

                if ((bitBuffer & (1 << (bitCount - 1))) != 0) {
                    currentNode = currentNode.right;
                } else {
                    currentNode = currentNode.left;
                }

                bitCount--;
                if (currentNode.isLeaf()) {
                    fos.write(currentNode.character);
                    currentNode = root;
                    charCount++;
                }
            }

            // Close the streams
            dis.close();
            fos.close();

            System.out.println("File decompressed successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static HuffmanNode buildHuffmanTree(int[] frequencies) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        for (char c = 0; c < frequencies.length; c++) {
            if (frequencies[c] > 0) {
                queue.add(new HuffmanNode(c, frequencies[c]));
            }
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();

            HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;

            queue.add(parent);
        }

        return queue.poll();
    }

    private static void generateCodes(HuffmanNode node, String code, String[] codes) {
        if (node.isLeaf()) {
            codes[node.character] = code;
            return;
        }

        generateCodes(node.left, code + '0', codes);
        generateCodes(node.right, code + '1', codes);
    }

    private static void writeHuffmanTree(HuffmanNode node, DataOutputStream dos) throws IOException {
        if (node.isLeaf()) {
            dos.writeBoolean(true);
            dos.writeChar(node.character);
        } else {
            dos.writeBoolean(false);
            writeHuffmanTree(node.left, dos);
            writeHuffmanTree(node.right, dos);
        }
    }

    private static HuffmanNode readHuffmanTree(DataInputStream dis) throws IOException {
        boolean isLeaf = dis.readBoolean();
        if (isLeaf) {
            char character = dis.readChar();
            return new HuffmanNode(character, 0);
        } else {
            HuffmanNode left = readHuffmanTree(dis);
            HuffmanNode right = readHuffmanTree(dis);
            HuffmanNode parent = new HuffmanNode('\0', 0);
            parent.left = left;
            parent.right = right;
            return parent;
        }
    }

    public static void main(String[] args) {
        String inputFile = "C:\\Users\\iFix\\Desktop\\nn.txt";
       // String compressedFile = "compressed.huf";
       String decompressedFile = "decompressed.txt";
        // compressFile(inputFile, compressedFile);
       decompressFile("C:\\Users\\iFix\\eclipse-workspace\\huffman\\compressed.huf", decompressedFile);
    }
}
