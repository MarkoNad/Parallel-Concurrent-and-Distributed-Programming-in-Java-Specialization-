package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        SieveActorActor actor = new SieveActorActor(2);
        finish(() -> {
            for(int i = 3; i < limit; i += 2) {
                actor.send(i);
            }
        });

        int totalPrimes = 0;
        for(SieveActorActor current = actor; current != null; current = current.getNextActor()) {
            totalPrimes += current.getPrimesCount();
        }

        return totalPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

        private static final int MAX_LOCAL_PRIMES = 1000;
        private final int[] primes;
        private int primesCount;
        private SieveActorActor nextActor;

        public SieveActorActor(int initialPrime) {
            primes = new int[MAX_LOCAL_PRIMES];
            primesCount = 0;
            primes[0] = initialPrime;
            primesCount++;
        }

        public SieveActorActor getNextActor() {
            return nextActor;
        }

        public int getPrimesCount() {
            return primesCount;
        }

        /**
         * Process a single message sent to this actor.
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            int candidate = (Integer)msg;

            boolean isLocallyPrime = isLocallyPrime(candidate);

            if (!isLocallyPrime) {
                return;
            }

            if (primesCount < MAX_LOCAL_PRIMES) {
                primes[primesCount] = candidate;
                primesCount++;
            } else if (nextActor == null) {
                nextActor = new SieveActorActor(candidate);
            } else {
                nextActor.send(candidate);
            }
        }

        /**
         * Checks if the candidate is a prime, where the possible prime factors are
         * located in the primes array.
         *
         * @param candidate Value we are checking to see if it is prime.
         */
        private boolean isLocallyPrime(final int candidate) {
            for (int i = 0; i < primesCount; i++) {
                final int prime = primes[i];
                if (candidate % prime == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
