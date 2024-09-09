# Multithreaded Utilities
A framework for running multithreaded tasks.

[![Build](https://github.com/davidbokor/multithreaded-utils/actions/workflows/gradle.yml/badge.svg)](https://github.com/davidbokor/multithreaded-utils/actions/workflows/gradle.yml)

## Step 1
Implement a producer for generating input for the executor.

For example, a producer that returns integers from 1 to 100
```java
Producer<Integer> producer = IterableProducer.from(
  IntStream
    .rangeClosed(1, 100)
    .boxed()
);
```

## Step 2
Implement an executor that takes each input, does something, and returns some output.

For example, an executor that divides each integer input by 100.0
```java
Executor<Integer, Double> executor = new Executor<>() {
  @Override
  public Double execute(Integer request) {
    return request.doubleValue() / 100.0;
  }

  @Override
  public void close() { 
      // DO NOTHING
  }
};
```

## Step 3
Implement a consumer that does something with the result of each execution.

For example, a consumer that prints out the result of the operation.
```java
Consumer<Integer, Double> consumer = new Consumer<>() {
  @Override
  public void consume(Integer input, Double output, long executionTime) {
    System.out.println(input + " --> " + output);
  }

  @Override
  public void close() { 
      // DO NOTHING 
  }
};
```

## Step 4
Run the test with a specified number of threads.

```java
MultiThreadedRunner<Integer, Double> runner = new MultiThreadedRunner<>(producer, executor, consumer, 10);
ExecutionStatistics stats = runner.run();
```
