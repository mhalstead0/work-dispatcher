# Overview
This contains a few different algorithms for dispatching work to pools of workers.  The algorithms are:
1. ``SimpleThreadsWorkDispatcher``: Simply dispatches work to new threads.
1. ``SimpleCoroutineWorkDispatcher``: Simply dispatches work to new coroutines.
1. ``ThreadPoolWorkDispatcher``: Uses a fixed thread pool.
1. ``CoroutinePoolWorkDispatcher``: Uses coroutines in a fixed pool.
1. ``SeparateThreadPoolsWorkDispatcher``: Uses a fixed thread pool per partition key.
1. ``SeparateCoroutinePoolsWorkDispatcher``: Uses coroutines in a fixed pool per partition key.
1. ``ElasticThreadPoolWorkDispatcher``: Uses a fixed thread pool for each partition key plus a large pool
of common processors that can use any partition key.
   
# Note
This is not production-level code; these are sample algorithms for benchmarking purposes.