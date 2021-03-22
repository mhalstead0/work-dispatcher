# Overview

Ran ``RunBenchmark`` (commit ``ed74a0cf``) with the following:
```kotlin
val dispatcherType = DispatcherType.valueOf(args[0].toUpperCase())
val targetThreadCount = 1000
val numLargeProducers = 5
val numSmallProducers = 200
val largeProducerBatchSize = 1000
val throttleSize = 50
```

# Results

## SeparateCoroutinePoolsWorkDispatcher
```
Run time: 10673ms
Peak thread count: 1025
bigProducerTracker.start: Stats(count=50000, min=0, max=354, average=154.23226, median=151)
bigProducerTracker.run: Stats(count=50000, min=1, max=9, average=1.6516, median=2)
bigProducerTracker.total: Stats(count=50000, min=1, max=356, average=155.88386, median=153)
smallProducerTracker.start: Stats(count=40000, min=0, max=254, average=2.8136, median=0)
smallProducerTracker.run: Stats(count=40000, min=10, max=86, average=15.455925, median=15)
smallProducerTracker.total: Stats(count=40000, min=10, max=277, average=18.269525, median=15)
```

## SeparateThreadPoolsWorkDispatcher
```
Run time: 10431ms
Peak thread count: 1025
bigProducerTracker.start: Stats(count=50000, min=0, max=365, average=161.931, median=160)
bigProducerTracker.run: Stats(count=50000, min=1, max=5, average=1.67442, median=2)
bigProducerTracker.total: Stats(count=50000, min=1, max=367, average=163.60542, median=162)
smallProducerTracker.start: Stats(count=40000, min=0, max=147, average=1.11375, median=0)
smallProducerTracker.run: Stats(count=40000, min=10, max=82, average=15.600875, median=15)
smallProducerTracker.total: Stats(count=40000, min=10, max=159, average=16.714625, median=15)
```

##  ElasticThreadPoolWorkDispatcher
```
Run time: 10457ms
Peak thread count: 1000
bigProducerTracker.start: Stats(count=50000, min=0, max=626, average=275.75848, median=274)
bigProducerTracker.run: Stats(count=50000, min=1, max=45, average=1.67372, median=2)
bigProducerTracker.total: Stats(count=50000, min=1, max=628, average=277.4322, median=275)
smallProducerTracker.start: Stats(count=40000, min=0, max=201, average=0.831, median=0)
smallProducerTracker.run: Stats(count=40000, min=10, max=54, average=13.845575, median=11)
smallProducerTracker.total: Stats(count=40000, min=10, max=214, average=14.676575, median=11)
```

# Summary
This shows similar performance between the implementations.  The elastic pool ran slower for the big producers, but made
the smaller producers had less wait time.