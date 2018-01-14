/*
 * QuiXProc: efficient evaluation of XProc Pipelines.
 * Copyright (C) 2011-2018 Innovimax
 * All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0*/
package innovimax.quixproc.datamodel.generator;

import java.math.BigInteger;

public abstract class AReversibleRandom {
	long seed;

	AReversibleRandom(final long seed) {
		this.seed = seed;
	}

	protected abstract int next();

	public abstract int prev();

	public void setSeed(final long seed) {
		this.seed = seed;
	}

	public static class SimpleReversibleRandom extends AReversibleRandom {
		private final int a;
        private final int b;

		public SimpleReversibleRandom(final long seed, final int a, final int b) {
			super(seed);
			this.a = a;
			this.b = b;
			this.seed = seed;
			if (seed == 0 && a == 0 && b == 0)
				throw new IllegalArgumentException("Cannot be all zero");
		}

		@Override
		public int next() {
			final long v = this.a * this.seed + this.b;
			this.seed++;
			return (int) (v & 0x7FFF);
		}

		@Override
		public int prev() {
			this.seed--;
			final long v = this.a * this.seed + this.b;
			return (int) (v & 0x7FFF);
		}
	}

	public static void main(final String[] args) {

		final SimpleReversibleRandom rr = new SimpleReversibleRandom(0, 1, 1);
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

	public int nextInt(final int i, final boolean directionForward) {
		return (directionForward ? this.next() : this.prev()) % i;
	}

	public int nextInt(final int i) {
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
	private static final long M = 1L << 63;
	private static final long A = 6364136223846793005L;
	private final long ainverse;
	private static final long C = 1442695040888963407L;
	private static final int D = 32;
	private long x;

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
	BReversibleRandom(final long seed) {
		this.x = seed;
		this.ainverse = extendedEuclidX(BigInteger.valueOf(this.A), BigInteger.valueOf(this.M)).longValue();
	}

	// unsigned int next() {
	public int next() {
		// nextx = (a * x + c) % m;
		this.x = (this.A * this.x + this.C) & (this.M - 1);
		return (int) (this.x >> this.D);
	}

	// unsigned int prev() {
	public int prev() {

		// const uint64_t ainverse = extendedEuclidX(A, M);
		// prevx = (ainverse * (x - c)) mod m
		this.x = this.ainverse * (this.x - this.C) & (this.M - 1);
		return (int) (this.x >> this.D);
	}

	// unsigned int max() const {
	int max() {
		return (int) ((this.M - 1) >> this.D);
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
    private long extendedEuclidX(final long a, final long b) {
		return b == 0 ? 1 : extendedEuclidY(b, a - b * (a / b));
	}

	// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b){
    private long extendedEuclidY(final long a, final long b) {
		return b == 0 ? 0 : extendedEuclidX(b, a - b * (a / b)) - (a / b) * extendedEuclidY(b, a - b * (a / b));
	}

	private BigInteger extendedEuclidX(final BigInteger a, final BigInteger b) {
		return b.equals(BigInteger.ZERO) ? BigInteger.ONE : extendedEuclidY(b, a.subtract(b.multiply(a.divide(b))));
	}

	// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b){
    private BigInteger extendedEuclidY(final BigInteger a, final BigInteger b) {
		return b.equals(BigInteger.ZERO) ? BigInteger.ZERO
				: extendedEuclidX(b, a.subtract(b.multiply(a.divide(b))))
						.subtract(a.divide(b).multiply(extendedEuclidY(b, a.subtract(b.multiply(a.divide(b))))));
	}

	static class ReversibleRandomBI {
		final BigInteger M = BigInteger.ONE.shiftLeft(63);
		final BigInteger A = new BigInteger("6364136223846793005");
		final BigInteger ainverse;
		final BigInteger C = new BigInteger("1442695040888963407");
		BigInteger x;
		static final int D = 32;

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
		ReversibleRandomBI(final long seed) {
			this.x = BigInteger.valueOf(seed);
			this.ainverse = extendedEuclidX(this.A, this.M);
		}

		// unsigned int next() {
		public int next() {
			// nextx = (a * x + c) % m;
			this.x = this.A.multiply(this.x).add(this.C).mod(this.M.subtract(BigInteger.ONE));
			return this.x.shiftRight(this.D).intValue();
		}

		// unsigned int prev() {
		public int prev() {

			// const uint64_t ainverse = extendedEuclidX(A, M);
			// prevx = (ainverse * (x - c)) mod m
			this.x = this.ainverse.multiply(this.x.subtract(this.C)).mod(this.M.subtract(BigInteger.ONE));
			return this.x.shiftRight(this.D).intValue();
		}

		// unsigned int max() const {
		int max() {
			return this.M.subtract(BigInteger.ONE).shiftRight(this.D).intValue();
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
		BigInteger extendedEuclidX(final BigInteger a, final BigInteger b) {
			return b.equals(BigInteger.ZERO) ? BigInteger.ONE
					: extendedEuclidY(b, a.subtract(b).multiply(a.divide(b)));
		}

		// constexpr uint64_t extendedEuclidY(uint64_t a, uint64_t b){
		BigInteger extendedEuclidY(final BigInteger a, final BigInteger b) {
			return b.equals(BigInteger.ZERO) ? BigInteger.ZERO
					: extendedEuclidX(b, a.subtract(b).multiply(a.divide(b))
							.subtract(a.divide(b).multiply(extendedEuclidY(b, a.subtract(b).multiply(a.divide(b))))));
		}

	}
}
