package innovimax.quixproc.datamodel.generator;

import java.math.BigInteger;

public abstract class AReversibleRandom {
	long seed;

	protected AReversibleRandom(long seed) {
		this.seed = seed;
	}

	public abstract int next();

	public abstract int prev();

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public static class SimpleReversibleRandom extends AReversibleRandom {
		private final int a, b;

		public SimpleReversibleRandom(long seed, int a, int b) {
			super(seed);
			this.a = a;
			this.b = b;
			this.seed = seed;
			if (seed == 0 && a == 0 && b == 0)
				throw new IllegalArgumentException("Cannot be all zero");
		}

		@Override
		public int next() {
			long v = a * this.seed + b;
			this.seed++;
			return (int) (v & 0x7FFF);
		}

		@Override
		public int prev() {
			this.seed--;
			long v = a * this.seed + b;
			return (int) (v & 0x7FFF);
		}
	}

	public static void main(String[] args) {

		SimpleReversibleRandom rr = new SimpleReversibleRandom(0, 1, 1);
		for (int i = 0; i < 20; i++)
			System.out.println(rr.next());
		System.out.println("------");
		for (int i = 0; i < 20; i++)
			System.out.println(rr.prev());
		System.out.println("---A---");
		/*
		 * BReversibleRandom rr = new BReversibleRandom(0); for (int i = 0; i <
		 * 20; i++) System.out.println(rr.next()); System.out.println("------");
		 * for (int i = 0; i < 20; i++) System.out.println(rr.prev());
		 * System.out.println("---A---");
		 * 
		 * BReversibleRandom.ReversibleRandomBI rrb = new
		 * BReversibleRandom.ReversibleRandomBI(0); for (int i = 0; i < 20; i++)
		 * System.out.println(rrb.next()); System.out.println("------"); for
		 * (int i = 0; i < 20; i++) System.out.println(rrb.prev());
		 */
	}

	public int nextInt(int i, boolean directionForward) {
		return (directionForward ? this.next() : this.prev()) % i;
	}

	public int nextInt(int i) {
		return nextInt(i, true);
	}

}

/*
 * RLCG - Reversible Linear Congruential Generator Copyright (c) 2013 Johan
 * Klokkhammer Helsing This software is provided 'as-is', without any express or
 * implied warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software. Permission is granted to anyone to use
 * this software for any purpose, including commercial applications, and to
 * alter it and redistribute it freely, subject to the following restrictions:
 * 1. The origin of this software must not be misrepresented; you must not claim
 * that you wrote the original software. If you use this software in a product,
 * an acknowledgment in the product documentation would be appreciated but is
 * not required. 2. Altered source versions must be plainly marked as such, and
 * must not be misrepresented as being the original software. 3. This notice may
 * not be removed or altered from any source distribution. Johan Klokkhammer
 * Helsing (johanhelsing@gmail.com)
 * 
 * https://github.com/bobbaluba/rlcg/blob/master/include/rlcg.hpp
 */
class BReversibleRandom {
	long M = 1L << 63;
	long A = 6364136223846793005L;
	long ainverse;
	long C = 1442695040888963407L;
	int D = 32;
	long x;

	// modulus M, multiplicand A, increment C, least significant bits to discard
	// D
	// template<uint64_t M = 1ul<<63ul, uint64_t A = 6364136223846793005,
	// uint64_t C = 1442695040888963407, uint64_t D = 32>
	// class ReversibleLCG {
	// static_assert(isPowerOfTwo(M), "M is not a power of two as it should
	// be");
	// uint64_t x;
	// public:
	// ReversibleLCG(unsigned int seed) : x(seed){}
	BReversibleRandom(long seed) {
		this.x = seed;
		this.ainverse = extendedEuclidX(BigInteger.valueOf(A), BigInteger.valueOf(M)).longValue();
	}

	// unsigned int next() {
	public int next() {
		// nextx = (a * x + c) % m;
		x = (A * x + C) & (M - 1);
		return (int) (x >> D);
	}

	// unsigned int prev() {
	public int prev() {

		// const uint64_t ainverse = extendedEuclidX(A, M);
		// prevx = (ainverse * (x - c)) mod m
		x = ainverse * (x - C) & (M - 1);
		return (int) (x >> D);
	}

	// unsigned int max() const {
	int max() {
		return (int) ((M - 1) >> D);
	}

	// constexpr implementation of euclids algorithm
	/*
	 * Based on this recursive definition from wikipedia: function
	 * extended_gcd(a, b) if b == 0 return (1, 0) else (q, r) := divide (a, b)
	 * (s, t) := extended_gcd(b, r) return (t, s - q * t) This assumes a
	 * "divide" procedure exists that returns a (quotient,remainder) pair (one
	 * could alternatively put q := a div b, and then r = a − b * q).
	 */
	// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b);
	// constexpr uint64_t extendedEuclidX(uint64_t a, uint64_t b){
	long extendedEuclidX(long a, long b) {
		return (b == 0) ? 1 : extendedEuclidY(b, a - b * (a / b));
	}

	// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b){
	long extendedEuclidY(long a, long b) {
		return (b == 0) ? 0 : extendedEuclidX(b, a - b * (a / b)) - (a / b) * extendedEuclidY(b, a - b * (a / b));
	}

	BigInteger extendedEuclidX(BigInteger a, BigInteger b) {
		return (b.equals(BigInteger.ZERO)) ? BigInteger.ONE : extendedEuclidY(b, a.subtract(b.multiply(a.divide(b))));
	}

	// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b){
	BigInteger extendedEuclidY(BigInteger a, BigInteger b) {
		return (b.equals(BigInteger.ZERO)) ? BigInteger.ZERO
				: extendedEuclidX(b, a.subtract(b.multiply(a.divide(b))))
						.subtract(a.divide(b).multiply(extendedEuclidY(b, a.subtract(b.multiply(a.divide(b))))));
	}

	static class ReversibleRandomBI {
		BigInteger M = BigInteger.ONE.shiftLeft(63);
		BigInteger A = new BigInteger("6364136223846793005");
		BigInteger ainverse;
		BigInteger C = new BigInteger("1442695040888963407");
		BigInteger x;
		int D = 32;

		// modulus M, multiplicand A, increment C, least significant bits to
		// discard D
		// template<uint64_t M = 1ul<<63ul, uint64_t A = 6364136223846793005,
		// uint64_t C = 1442695040888963407, uint64_t D = 32>
		// class ReversibleLCG {
		// static_assert(isPowerOfTwo(M), "M is not a power of two as it should
		// be");
		// uint64_t x;
		// public:
		// ReversibleLCG(unsigned int seed) : x(seed){}
		ReversibleRandomBI(long seed) {
			this.x = BigInteger.valueOf(seed);
			this.ainverse = extendedEuclidX(A, M);
		}

		// unsigned int next() {
		public int next() {
			// nextx = (a * x + c) % m;
			x = A.multiply(x).add(C).mod(M.subtract(BigInteger.ONE));
			return x.shiftRight(D).intValue();
		}

		// unsigned int prev() {
		public int prev() {

			// const uint64_t ainverse = extendedEuclidX(A, M);
			// prevx = (ainverse * (x - c)) mod m
			x = ainverse.multiply(x.subtract(C)).mod(M.subtract(BigInteger.ONE));
			return x.shiftRight(D).intValue();
		}

		// unsigned int max() const {
		int max() {
			return M.subtract(BigInteger.ONE).shiftRight(D).intValue();
		}

		// constexpr implementation of euclids algorithm
		/*
		 * Based on this recursive definition from wikipedia: function
		 * extended_gcd(a, b) if b == 0 return (1, 0) else (q, r) := divide (a,
		 * b) (s, t) := extended_gcd(b, r) return (t, s - q * t) This assumes a
		 * "divide" procedure exists that returns a (quotient,remainder) pair
		 * (one could alternatively put q := a div b, and then r = a − b * q).
		 */
		// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b);
		// constexpr uint64_t extendedEuclidX(uint64_t a, uint64_t b){
		BigInteger extendedEuclidX(BigInteger a, BigInteger b) {
			return (b.equals(BigInteger.ZERO)) ? BigInteger.ONE
					: extendedEuclidY(b, a.subtract(b).multiply(a.divide(b)));
		}

		// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b){
		BigInteger extendedEuclidY(BigInteger a, BigInteger b) {
			return (b.equals(BigInteger.ZERO)) ? BigInteger.ZERO
					: extendedEuclidX(b, a.subtract(b).multiply(a.divide(b))
							.subtract(a.divide(b).multiply(extendedEuclidY(b, a.subtract(b).multiply(a.divide(b))))));
		}

	}
}
