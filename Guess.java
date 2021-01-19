/**
* This program is created to solver the Guess My Number game - a version of the famous mastermind game.
* @author  Tran Minh Quang - s3757281
* @author  Le Thanh Tai – s3760615
* @author Tran Khang Duy - s3754942
* @version 1.0
* @since   2021-01-09
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/*
 * You need to implement an algorithm to make guesses
 * for a 4-digits number in the method make_guess below
 * that means the guess must be a number between [1000-9999]
 * PLEASE DO NOT CHANGE THE NAME OF THE CLASS AND THE METHOD
 */
public class Guess {

	public static int make_guess(int hits, int strikes) {

		CodeBreaker codeBreaker = CodeBreaker.getInstance();
		Response hitsAndStrikes = new Response(strikes, hits);

		Code myguess = codeBreaker.getCode(hitsAndStrikes);

		return Integer.parseInt(myguess.toString());
	}

}


/**
 * This class implement the main algorithm for solving the game
 */
class CodeBreaker {

	//Properties initialization
	final HashSet<Code> allNumbers;
	protected final Code[] listOfOptimalFirstGuess;
	protected final HashSet<Response> allHitsAndStrikes;
	protected HashSet<Code> pruneList;
	protected Code lastGuess;

	//Singleton to save memory
	private static CodeBreaker instance = new CodeBreaker();
	public static CodeBreaker getInstance(){return instance;}

	/**
	 * Constructor for the class
	 */
	public CodeBreaker() {
		this.allNumbers = Code.createAllPotentialNums();
		this.allHitsAndStrikes = Response.createAllHitsAndStrikes();
		this.pruneList = new HashSet<Code>();
		this.pruneList.addAll(allNumbers);
		this.listOfOptimalFirstGuess = generateListOptimalFirstGuesses();

	}


	/**
	 * Calculate a list of 20 optimal first guess
	 * @return Number[]
	 */
	private Code[] generateListOptimalFirstGuesses() {
		Code[] firstGuesses = new Code[20];
		int index = 0;

		//For each potential solution
		for (Code number: this.pruneList){
			int currentMax = 0;
			//Calculate the maximum remaining candidates
			for (Response a : this.allHitsAndStrikes){
				int remainingGuesses = getPotentialSolutions(this.pruneList, number, a).size();
				currentMax = Math.max(remainingGuesses, currentMax);
			}

			//If a number has the remaining candidates = 2092, add it to the array
			//The number 2092 is the smallest remaining candidates for first guess.
			//We got this number by running knuthGuess on allNumbers
			if (currentMax == 2092){
				if (index < 20)
					firstGuesses[index++] = number;
				else
					break;
			}
		}

		return firstGuesses;
	}



	/**
	 * Pick a random first guess from the array of optimalFirstGuess
	 * @return Number
	 */
	protected Code getFirstGuess() {
		this.lastGuess = this.listOfOptimalFirstGuess[new Random().nextInt(20)];
		return this.lastGuess;
	}



	/**
	 * Filter out numbers that don't produce the same HitsAndStrikes when compared with the guess
	 * @param pruneList : List of potential number
	 * @param lastGuess : The last guess
	 * @param hitsAndStrikes : number of hits and strikes
	 */
	private void filterPotentialNumbers(HashSet<Code> pruneList, Code lastGuess, Response hitsAndStrikes) {
		Code number;
		for (Iterator<Code> potentialSolutions = pruneList.iterator(); potentialSolutions.hasNext();){
			number = potentialSolutions.next();
			Result result = processGuess(number.hashCode(), lastGuess.toString());
			Response temp = new Response(result.getStrikes(), result.getHits());
			if (!temp.equals(hitsAndStrikes))
				potentialSolutions.remove();
		}
	}


	/**
	 * Use Knuth's algorithm to make the next guess
	 * @param hitsAndStrikes
	 * @return Number the next guess 
	 * @see filterPotentialSolutions
	 * @see getPotentialSolutions
	 */
	protected Code getCode(Response hitsAndStrikes) {

		if (hitsAndStrikes.getHits() == 0 && hitsAndStrikes.getStrikes() == 0) {
			return getFirstGuess();
		}
		//Filter out numbers that cannot be the solution
		this.filterPotentialNumbers(this.pruneList, this.lastGuess, hitsAndStrikes);

		int minimumMax = Integer.MAX_VALUE;
		//For each potential solution
		for (Code number: this.pruneList){
			int currentMax = 0;
			//Calculate the maximum remaining candidates
			for (Response a : this.allHitsAndStrikes){
				int remainingGuesses = getPotentialSolutions(this.pruneList, number, a).size();
				currentMax = Math.max(remainingGuesses, currentMax);
			}
			//If a number has smaller maximum remaining candidates than the global max, pick it
			//This is to minimize the remaining sample size
			if (currentMax < minimumMax){
				minimumMax = currentMax;
				this.lastGuess = number;
			}
		}

		return this.lastGuess;
	}

	/**
	 * Also filter out potential candidates, but this one does not delete any number from the set
	 * @param numbers
	 * @param lastGuess
	 * @param hitsAndStrikes
	 * @return HashSet<Number>
	 */
	protected HashSet<Code> getPotentialSolutions(HashSet<Code> numbers, Code lastGuess, Response hitsAndStrikes) {
		HashSet<Code> result = new HashSet<Code>();
		for (Code number : numbers) {
			if (lastGuess.compare(number).equals(hitsAndStrikes)) {
				result.add(number);
			}
		}
		return result;
	}


	/**
	 * Lecture's methods to process the guess
	 * @param target
	 * @param guess
	 * @return
	 */
	static Result processGuess(int target, String guess) {
		char des[] = Integer.toString(target).toCharArray();
		char src[] = guess.toCharArray();
		int hits=0;
		int strikes=0;

		// process strikes
		for (int i=0; i<4; i++) {
			if (src[i] == des[i]) {
				strikes++;
				des[i] = 'a';
				src[i] = 'a';
			}
		}
		// process hits
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				if (src[i]!='a') {
					if (src[i]==des[j]) {
						hits++;
						des[j] = 'a';
						break;
					}
				}
			}
		}

		return new Result(hits, strikes);
	}

}

/**
 * This class handle operation related to the guess number
 */
class Code{
	//Properties
	private final byte[] num;

	/**
	 * Constructor
	 * @param secretNum
	 */
	public Code(byte[] secretNum) {
		this.num = secretNum.clone();
	}

	/**
	 * Compare the guess with the secretNum and return Hits & Strikes
	 * @param secretNum
	 * @return
	 */
	public Response compare(Code secretNum) {
		//Add all digits of the number to a HashSet
		HashSet<Integer> set = new HashSet<>(4);
		set.add(secretNum.get(0));
		set.add(secretNum.get(1));
		set.add(secretNum.get(2));
		set.add(secretNum.get(3));

		int strikes = 0, hits = 0;
		for (int i = 0; i < 4; i++) {
			//If the value and the position are both the same, it's a strike
			if (this.num[i] == secretNum.get(i))
					strikes++;
			//Otherwise, if the value exists in the set, it's a hit
			else if (set.contains((int) this.num[i]))
				hits++;
		}
		return new Response(strikes, hits);
	}

	/**
	 * Create a set of 4 digit number from 0000 to 9999
	 * @return HashSet<Number>
	 */
	public static HashSet<Code> createAllPotentialNums() {
		final HashSet<Code> result = new HashSet<>(10000);
		generatelistOfNums(result, 4, new byte[4]);
		return result;
	}

	/**
	 * Generate a list of number based on the length
	 * @param numbers
	 * @param length
	 * @param temp
	 */
	private static void generatelistOfNums(HashSet<Code> numbers, int length, byte[] temp) {
		if (length == 0) {
			if (temp[0] != 0)
				numbers.add(new Code(temp));
		} else {
			for (byte digit : new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}) {
				temp[length-1] = digit;
				generatelistOfNums(numbers, length - 1, temp);
			}
		}
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Code)) return false;
		Code number = (Code) o;

		for (int i = 0; i < 4; i++) {
			if (num[i] != number.num[i])
				return false;
		}
		return true;
	}

	/**
	 * Overide hash function to minimize memory usage
	 */
	@Override
	public int hashCode() {
		return num[3] + num[2] * 10 + num[1] * 100 + num[0] * 1000;
	}

	/**
	 * Get a element in the number by its index
	 * @param i : int
	 * @return byte
	 */
	public int get(int i) {
		return this.num[i];
	}

	/**
	 * Overide toString methods
	 */
	@Override
	public String toString() {
		return num[0] + "" + num[1] + "" + num[2] + "" + num[3];
	}

}


/**
 * This class is the data structure to store hits and strikes value
 */
class Response {

	//Class properties
	public final int strikes;
	public final int hits;

	/**
	 * Constructor for the class
	 * @param strikes : int
	 * @param hits : int
	 */
	public Response(int strikes, int hits) {
		this.strikes = strikes;
		this.hits = hits;
	}


	/**
	 * Get methods for number hits
	 * @return int 
	 */
	public int getHits() {
		return hits;
	}

	/**
	 * Get methods for number hits
	 * @return int
	 */
	public int getStrikes() {
		return strikes;
	}

	/**
	 * Check if the number of hits and strikes are equal or not
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Response)) return false;
		Response that = (Response) o;
		return strikes == that.strikes && hits == that.hits;
	}


	/**
	 * Calculate all combination of Hits and Strikes
	 * @return HashSet<HitsAndStrikes>
	 */
	public static HashSet<Response> createAllHitsAndStrikes() {
		HashSet<Response> result = new HashSet<>(16);
		for (int strikes = 0; strikes < 4; strikes++) {
			for (int hits = 0; hits <= 4; hits++) {
				int sum = hits + strikes;
				if (sum <= 4) {
					result.add(new Response(strikes, hits));
				}
			}
		}
		return result;
	}


	/**
	 * To string methods
	 * @return String
	 */
	@Override
	public String toString() {
		return "strikes: " + this.strikes + ", hits: " + this.hits;
	}
}