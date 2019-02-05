package day1;

import java.util.List;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

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
 * Okay, so 
 * @author basin
 *
 */
class CharacterIterator implements DataSetIterator {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataSet next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSet next(int num) {
		// TODO Auto-generated method stub
		return null;
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