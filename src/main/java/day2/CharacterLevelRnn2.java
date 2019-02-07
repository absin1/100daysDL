package day2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import com.google.common.io.Files;

public class CharacterLevelRnn2 {
	public static void main(String[] args) {
		// Hyperparameters start.
		// Hyperparameters end.
		// Unlike the last example we are going to use a fancy new iterator called the
		// characteriterator. So here we also end up learning how to create custom
		// datasetiterator

	}
}

/**
 * We implement the DataSetIterator interface and we can see that it requires us
 * to implement a few methods. Now I am trying to get a hold of this particular
 * implementation. What are the different types of iterators available?
 * Regardless of the choice we have to implement duck-load of methods. So let's
 * take a look at what data this should contain, there needs to be input or as
 * we fancily call 'feature vectors' and output or labels if its a
 * classification problem, which ours is. But hang on in a character level RNN
 * we want to just predict the next alphabet so our input is a series of string
 * and the output well basically the same thing but moved by a character. Like
 * 'She was we' and 'he was wet'. Notice how I have carefully chosen the input
 * and output to be of the same length. But you can't really pass alphabets to a
 * computer for multiplication so you pass the one-hot vector for each character
 * in your string, so to say.
 * 
 * @author absin
 *
 */
class CharacterIterator implements DataSetIterator {
	private static final long serialVersionUID = -6103836732387660829L;
	/*
	 * We need a holder for all the characters in our file. Let's take a char array
	 * for this.
	 */
	private char[] fileCharacters;
	/*
	 * We also need to have a holder of the characters which will be used to
	 * generate the one-hot representation of the character encountered. Let's have
	 * another character array for this. Since we may not want our RNN to predict
	 * some characters we can choose to filter them out completely.
	 */
	private char[] validCharacters;
	/*
	 * We will also need to get the index of a particular character in the
	 * validCharacters array while making one-hot representation. To do this let's
	 * also have a map of characters vs the index. Alternatively if we need
	 * character at a particular index we can quickly get it from the
	 * validCharacters array.
	 */
	private Map<Character, Integer> charToIndexMap;

	/*
	 * While training the RNN on a huge file (like Shakespeare's complete works) we
	 * won't want to send all the characters at once to the RNN, as this can be
	 * computationally quite expensive. So we perform a batching of the characters
	 * from the file. While batching we return a few sentences of constant size.
	 * Let's have 2 integers corresponding to the number of characters in one
	 * sentence and the number of sentences we are going to send in the batch.
	 */
	private int sentenceSize;
	private int batchSentenceCount;
	/*
	 * Now we need to send batchSentenceCount number of one hot vectors for
	 * sentences of length sentenceSize from the fileCharacters every time as a
	 * batch to the RNN. This can be done by having a holder of all the possible
	 * indexes of the fileCharacters array. We will use a holder for this. It will
	 * store values like 0, sentenceSize, 2*sentenceSize.. etc. Since we would want
	 * the elements to be accessed as quickly as possible one possible collection
	 * for this is a linkedList which offers O(1) complexity for popping which is
	 * suitable for us.
	 */
	private LinkedList<Integer> sentenceStartIndexes = new LinkedList<>();
	/*
	 * A random generator instance
	 */
	private Random ran;

	/**
	 * The constructor for this class will mainly initialize the
	 * sentenceStartIndices keeping all the sanity checks like comment portions,
	 * valid characters etc in mind. Additionally it will also shuffle the start
	 * indices so the data fed to the RNN has no temporal pattern.
	 * 
	 * @param filePath           The path of the text file
	 * @param charset            The charset used in the file
	 * @param batchSentenceCount Number of sentences to be passed in one batch
	 * @param sentenceSize       The number of characters in one sentence
	 * @param validCharacters    The characters the rnn should learn on
	 * @param ran                A random generator instance
	 * @param commentCharacters  The lines sarting with these characters in the file
	 *                           will be skipped
	 * @author absin
	 * @throws IOException
	 */
	public CharacterIterator(String filePath, Charset charset, int batchSentenceCount, int sentenceSize,
			char[] validCharacters, Random ran, String commentCharacters) throws IOException {
		if (!new File(filePath).exists())
			throw new IOException("The file couldn't be located at path -->" + filePath);
		if (batchSentenceCount <= 0)
			throw new IllegalArgumentException("The number of sentences fed to the RNN can't be non natural");
		this.validCharacters = validCharacters;
		this.batchSentenceCount = batchSentenceCount;
		this.sentenceSize = sentenceSize;
		this.ran = ran;
		// Let's put all the valid characters in a map and then index them
		charToIndexMap = new HashMap<>();
		for (int i = 0; i < validCharacters.length; i++)
			charToIndexMap.put(validCharacters[i], i);
		// Now let's convert the entire file into a character array, this can be
		// dangerous if the file is huge
		List<String> lines = Files.readLines(new File(filePath), charset);
		if (commentCharacters != null) {
			List<String> linesWithoutComment = new ArrayList<>();
			for (String line : lines) {
				if (!line.startsWith(commentCharacters))
					linesWithoutComment.add(line);
			}
			lines = linesWithoutComment;
		}
		// Let's start counting the number of characters in the file
		// Let's take into account the new lines first
		int fileCharCount = lines.size();
		for (String line : lines)
			fileCharCount += line.length();
		char[] fileCharacters = new char[fileCharCount];
		int i = 0;
		for (String line : lines) {
			for (char c : line.toCharArray()) {
				if (charToIndexMap.containsKey(c))
					fileCharacters[i++] = c;
				else
					continue;
			}
			// So now we have copied all the valid characters from the line
			// Let's also add a new line if it's valid
			if (charToIndexMap.containsKey('\n'))
				fileCharacters[i++] = '\n';
		}
		if (i == fileCharacters.length)
			this.fileCharacters = fileCharacters;
		else
			this.fileCharacters = Arrays.copyOfRange(fileCharacters, 0, i);
		if (sentenceSize > this.fileCharacters.length)
			throw new IllegalArgumentException(
					"The size of a batch sentence can not be more than the size of the file");
		int rejectedCharCount = fileCharCount - this.fileCharacters.length;
		System.out.println(
				"Read and converted the file: " + filePath + ". The total number of characters in the file were: "
						+ fileCharCount + ", out of which " + rejectedCharCount + " were rejected and "
						+ this.fileCharacters.length + " are the valid characters on which training will now be done");
		// Now we have read the file into a character array. Next we shall try to
		// initialize the sentenceStartIndices as they are used to fetch sentences from
		// the character array.
		initializeSentenceIndices();
	}

	/**
	 * When a batch of sentences is demanded we should be able to quickly deliver
	 * it. We already have all the file characters in fileCharacters array. If we
	 * already know the index from which the sentence has to be fetched, we can
	 * perform the fetching in O(1) time. The idea is also to randomize what we are
	 * feeding to the array and at the same time not compromise on time.
	 */
	private void initializeSentenceIndices() {
		int numberOfSentencesPossible = (fileCharacters.length - 1) / (sentenceSize - 1);
		for (int i = 0; i < numberOfSentencesPossible; i++)
			sentenceStartIndexes.add(i * sentenceSize);
		Collections.shuffle(sentenceStartIndexes, ran);
	}

	/**
	 * Since we are managing the possible iterations in a LinkedList if that list
	 * has more elements then so does the iterator
	 */
	@Override
	public boolean hasNext() {
		return sentenceStartIndexes.size() > 0;
	}

	@Override
	public DataSet next() {
		return next(batchSentenceCount);
	}

	@Override
	public DataSet next(int num) {
		// Let's perform some sanity checks
		// If all the sentences have been exhausted then the next should throw an exception
		if (sentenceStartIndexes.size() == 0)
			throw new NoSuchElementException("You have exhausted all the sentences in the file");
		// If the number of sentences requested is less than the numnbher opf sentecxnece aa
	}

	@Override
	public int inputColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int totalOutcomes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean resetSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean asyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public int batch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor preProcessor) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataSetPreProcessor getPreProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getLabels() {
		// TODO Auto-generated method stub
		return null;
	}

}