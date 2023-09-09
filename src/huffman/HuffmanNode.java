package huffman;

class HuffmanNode implements Comparable<HuffmanNode> {
    int frequency;
    char character;
    HuffmanNode left, right;

    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return frequency - node.frequency;
    }
}

