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
 * to implement a few methods. 
 * 
 * @author absin
 *
 */
class CharacterIterator implements DataSetIterator {

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