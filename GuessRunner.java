import java.util.Random;

public class GuessRunner {

	static Result processGuess(int target, int guess) {
		char des[] = Integer.toString(target).toCharArray();
		char src[] = Integer.toString(guess).toCharArray();
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
		System.out.printf("\t");
		if (strikes==4)	{ // game over
			System.out.printf("4 strikes - Game over\n");
			return new Result(hits, strikes);
		}
		if (hits==0 && strikes==0)
			System.out.printf("Miss\n");
		else if(hits>0 && strikes==0)
			System.out.printf("%d hits\n", hits);
		else if(hits==0 && strikes>0)
			System.out.printf("%d strikes\n", strikes);
		else if(hits>0 && strikes>0)
			System.out.printf("%d strikes and %d hits\n", strikes, hits);
		
		return new Result(hits, strikes);
	}

	public static void main(String[] args) {
		int guess_cnt = 0;
		int[] possibleTargets = {1914, 2295, 9102, 2296, 7139, 2309, 9302, 9010, 1085};
		// the strategy to increase the number of guesses
		int target = possibleTargets[new Random().nextInt(9)];
		Result res = new Result();
		System.out.println("Guess\tResponse\n");
		while(res.getStrikes() < 4) {
			/* take a guess from user provided class
			 * the user provided class must be a Guess.class file
			 * that has implemented a static function called make_guess()
			 */			
			int guess = Guess.make_guess(res.getHits(), res.getStrikes());
			System.out.printf("%d\n", guess);
			
			if (guess == -1) {	// user quits
				System.out.printf("you quit: %d\n", target);
				return;
			}
			guess_cnt++;
			
			/* You need to code this method to process a guess
			 * provided by your oponent
			 */
			res = processGuess(target, guess);
		}
		System.out.printf("Target: %d - Number of guesses: %d\n", target, guess_cnt);
	}
}
