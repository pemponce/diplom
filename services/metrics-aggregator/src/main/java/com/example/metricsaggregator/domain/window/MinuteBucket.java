package com.example.metricsaggregator.domain.window;

public class MinuteBucket {
  private long n;
  private double sumRps, sumCpu, sumQueue;
  private final java.util.List<Double> latencies = new java.util.ArrayList<>();

  public void add(double rps, double p95, double cpu, double q) {
    n++;
    sumRps += rps;
    sumCpu += cpu;
    sumQueue += q;
    latencies.add(p95); // упрощённо: берём p95 входного сэмпла
  }

  public MinuteAggregate toAggregate(String service, java.time.Instant ts) {
    latencies.sort(Double::compare);
    int idx = Math.max(0, (int)Math.ceil(0.95 * latencies.size()) - 1);
    double p95 = latencies.isEmpty() ? 0.0 : latencies.get(idx);

    return new MinuteAggregate(
        service, ts,
        (n == 0 ? 0 : sumRps / n),
        p95,
        (n == 0 ? 0 : sumCpu / n),
        (n == 0 ? 0 : sumQueue / n)
    );
  }
}
