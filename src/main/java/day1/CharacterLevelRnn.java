package day1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.Builder;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class CharacterLevelRnn {

	public static void main(String[] args) throws IOException {
		charLevelRnn();
		// Observing this on shakespearean text I am not impressed, I am going to try a
		// different model
	}

	private static void charLevelRnn() throws IOException {
		// In our first day with Neural Nets using DL4j we are going to jump right in
		// and write an RNN and see what we can do with it
		// Let's take a string
		String inputText = readtextFile();
		char[] inputCharArray = inputText.toCharArray();
		// Let's first create a collection of all the characters
		// Why do we have characters as the unit of our collection? Because we are
		// making what is called a character level RNN, to learn more about it go though
		// Karpathy's post
		LinkedHashSet<Character> charsSet = new LinkedHashSet<Character>();
		// Next let's add some characters from the string
		for (char c : inputCharArray)
			charsSet.add(c);
		// Lets convert this set into a list
		List<Character> charsList = new ArrayList<Character>(charsSet);
		System.out.println(charsList.size());
		// Now let's get to the actual neural network
		Builder builder = new NeuralNetConfiguration.Builder();
		builder.seed(123);
		builder.biasInit(0);
		builder.miniBatch(false);
		builder.updater(new RmsProp(0.001));
		builder.weightInit(WeightInit.XAVIER);
		// Ok so now our neural net has some base configurations
		// Let's add some hidden layers
		int hiddenLayerCount = 3;
		int hiddenLayerWidth = 50;
		// We will use an interface called ListBuilder to add layers to our network
		ListBuilder listBuilder = builder.list();
		for (int i = 0; i < hiddenLayerCount; i++) {
			LSTM.Builder hiddenLayerBuilder = new LSTM.Builder();
			hiddenLayerBuilder.nIn(i == 0 ? charsList.size() : hiddenLayerWidth);
			hiddenLayerBuilder.nOut(hiddenLayerWidth);
			hiddenLayerBuilder.activation(Activation.TANH);
			Layer hiddenLayer = hiddenLayerBuilder.build();
			listBuilder.layer(i, hiddenLayer);
		}
		// We need an output layer for our neural net
		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunction.MCXENT);
		outputLayerBuilder.activation(Activation.SOFTMAX);
		outputLayerBuilder.nIn(hiddenLayerWidth);
		// Since in a character level rnn we predict what can be the next character,
		// this is usually the length of all the possible characters read
		outputLayerBuilder.nOut(charsList.size());
		listBuilder.layer(hiddenLayerCount, outputLayerBuilder.build());
		listBuilder.pretrain(false);
		listBuilder.backprop(true);
		// So now out neural network configuration is almost done, lets create the
		// network now
		MultiLayerConfiguration configuration = listBuilder.build();
		MultiLayerNetwork network = new MultiLayerNetwork(configuration);
		network.init();
		network.setListeners(new ScoreIterationListener(1));

		// Now let's feed input to our network
		// We are going to use nd4j as our base vector library, ned4j is similar to
		// numpy
		INDArray input = Nd4j.zeros(1, charsList.size(), inputCharArray.length);
		INDArray labels = Nd4j.zeros(1, charsList.size(), inputCharArray.length);
		// Now in a char level network in the subsequent passes we pass the string and
		// keep adding one character to the string as labels because that is what we
		// expect the neural network to predict
		int position = 0;
		for (char currentChar : inputCharArray) {
			char nextChar = inputCharArray[(position + 1) % (inputCharArray.length)];
			input.putScalar(new int[] { 0, charsList.indexOf(currentChar), position }, 1);
			labels.putScalar(new int[] { 0, charsList.indexOf(nextChar), position }, 1);
			position++;
		}
		DataSet trainingData = new DataSet(input, labels);
		// Now let's perform some epochs, an epoch is the movement of all the inputs
		// through the network once
		for (int epoch = 0; epoch < 100; epoch++) {
			System.out.println("Epoch --> " + epoch);
			network.fit(trainingData);
			network.rnnClearPreviousState();

		}
		System.out.println(
				"Neural net is now trained. Yore are now going to type something and watch the neural network do 1 character autofill for you");
		String c = "a";
		Scanner scanner = new Scanner(System.in);
		c = scanner.nextLine();
		while (!c.equalsIgnoreCase("q")) {

			INDArray testInit = Nd4j.zeros(1, charsList.size(), 1);
			testInit.putScalar(charsList.indexOf(inputCharArray[c.toCharArray()[0]]), 1);
			// Lets run the rnn once and see what outoput it gives us
			INDArray output = network.rnnTimeStep(testInit);
			// The neuron which is activate in this neuron shows the next possible character
			// Now a neat thing we can do here is give that input to the RNN and ask for
			// more characters, this is like getting one predicted character by the RNN and
			// asking it to base more predictions based on that one output thus enabling it
			// of continuous speech!!
			for (int i = 0; i < 10; i++) {
				// Lets get which neuron was activated in this particular run
				int sampleCharacterIndex = Nd4j.getExecutioner().exec(new IMax(output), 1).getInt(0);
				// Let's print this character
				System.out.print(charsList.get(sampleCharacterIndex));
				// Now lets feed the last output as input to the RNN and observe it truly speeak
				INDArray nextInput = Nd4j.zeros(1, charsList.size(), 1);
				nextInput.putScalar(sampleCharacterIndex, 1);
				output = network.rnnTimeStep(nextInput);
			}
			System.out.print("\n");
			c = scanner.nextLine();
		}
	}

	private static String readtextFile() {
		int maxCharLength = 1000;
		File file = new File("/home/absin/Downloads/t8.shakespeare.txt");
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
			String fullText = "";
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				fullText += line.trim().replace("\\s+", " ").toLowerCase();
				if (fullText.length() > maxCharLength)
					break;
			}
			System.out.println(fullText);
			return fullText;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "an angry fox jumped over a lazy";
	}
}
