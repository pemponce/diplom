package com.example.metricsaggregator.domain.window;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WindowState {
  private final Map<String, Map<Instant, MinuteBucket>> state = new HashMap<>();

  public void add(String service, Instant ts, double rps, double p95, double cpu, double q) {
    var keyTs = ts.truncatedTo(ChronoUnit.MINUTES);
    var svcMap = state.computeIfAbsent(service, s -> new HashMap<>());
    var bucket = svcMap.computeIfAbsent(keyTs, t -> new MinuteBucket());
    bucket.add(rps, p95, cpu, q);
  }

  /** Вернёт и удалит все «закрытые» окна (раньше текущей минуты) */
  public List<MinuteAggregate> extractClosed() {
    var nowKey = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    var out = new ArrayList<MinuteAggregate>();
    state.forEach((svc, buckets) -> {
      var closedKeys = buckets.keySet().stream().filter(ts -> ts.isBefore(nowKey)).sorted().toList();
      for (var ts : closedKeys) {
        out.add(buckets.remove(ts).toAggregate(svc, ts));
      }
    });
    return out;
  }
}
