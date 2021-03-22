# Overview

Ran ``RunBenchmark`` (commit ``ed74a0cf``) with the following:
```kotlin
val dispatcherType = DispatcherType.valueOf(args[0].toUpperCase())
val targetThreadCount = 1000
val numLargeProducers = 5
val numSmallProducers = 800
val largeProducerBatchSize = 1000
val throttleSize = 50
```

# Results

## SeparateCoroutinePoolsWorkDispatcher
```
Run time: 10912ms
Peak thread count: 1610
bigProducerTracker.start: Stats(count=50000, min=0, max=1145, average=452.07136, median=452)
bigProducerTracker.run: Stats(count=50000, min=1, max=144, average=1.69412, median=2)
bigProducerTracker.total: Stats(count=50000, min=1, max=1147, average=453.76548, median=454)
smallProducerTracker.start: Stats(count=160000, min=0, max=1005, average=5.90505625, median=0)
smallProducerTracker.run: Stats(count=160000, min=10, max=149, average=11.30894375, median=11)
smallProducerTracker.total: Stats(count=160000, min=10, max=1016, average=17.214, median=11)
```

## SeparateThreadPoolsWorkDispatcher
```
Run time: 10249ms
Peak thread count: 1610
bigProducerTracker.start: Stats(count=50000, min=0, max=1005, average=432.83872, median=429)
bigProducerTracker.run: Stats(count=50000, min=1, max=118, average=1.68664, median=2)
bigProducerTracker.total: Stats(count=50000, min=1, max=1007, average=434.52536, median=430)
smallProducerTracker.start: Stats(count=160000, min=0, max=488, average=2.16006875, median=0)
smallProducerTracker.run: Stats(count=160000, min=10, max=127, average=11.95829375, median=11)
smallProducerTracker.total: Stats(count=160000, min=10, max=504, average=14.1183625, median=11)
```

##  ElasticThreadPoolWorkDispatcher
```
Running implementation: ElasticThreadPoolWorkDispatcher
Run time: 11153ms
Peak thread count: 1000
bigProducerTracker.start: Stats(count=50000, min=0, max=703, average=281.929, median=275)
bigProducerTracker.run: Stats(count=50000, min=1, max=137, average=1.64966, median=2)
bigProducerTracker.total: Stats(count=50000, min=1, max=705, average=283.57866, median=277)
smallProducerTracker.start: Stats(count=160000, min=0, max=606, average=2.90895625, median=0)
smallProducerTracker.run: Stats(count=160000, min=10, max=143, average=14.2139125, median=11)
smallProducerTracker.total: Stats(count=160000, min=10, max=625, average=17.12286875, median=11)
```

# Summary
``ElasticThreadPoolWorkDispatcher`` is the clear winner here.  It ran about 40% faster with 40% fewer threads (because
the calculation from a targetThreadCount caused it to create 1,600 instead of 1,000 threads).  The elastic dispatcher
ran faster because it can use the pool of common threads to service the large producers.