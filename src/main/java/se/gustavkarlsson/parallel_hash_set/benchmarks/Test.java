package se.gustavkarlsson.parallel_hash_set.benchmarks;

public interface Test {

	void prepare();

	Runnable createExecution();

	String getDescription();
}
