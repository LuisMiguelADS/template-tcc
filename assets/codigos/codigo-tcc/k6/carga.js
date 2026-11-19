import http from 'k6/http';
import exec from 'k6/execution';
import { sleep } from 'k6';

const num = (v, fallback) => (v === undefined || v === '' ? fallback : Number(v));

const API_URL = __ENV.API_URL || 'http://api-mvc:8080';
const USERS = num(__ENV.USERS, 100);
const RAMP = num(__ENV.RAMP_SEC, 20);
const WARMUP = num(__ENV.WARMUP_SEC, 30);
const MEASUREMENT = num(__ENV.MEASUREMENT_SEC, 120);
const QUERIES = num(__ENV.QUERIES, 3);
const THINK_MS = num(__ENV.THINK_MS, 0);
const LOAD_MODEL = __ENV.LOAD_MODEL || 'closed'; // closed = N usuarios simultaneos; open = taxa fixa de chegada
const RATE_RPS = num(__ENV.RATE_RPS, 1000);
const RUN_ID = __ENV.RUN_ID || 'run';
const OUT_DIR = __ENV.OUT_DIR || '/resultados';
const TOTAL_CUSTOMERS = 10000;

const RAMP_END_MS = RAMP * 1000;
const WARMUP_END_MS = (RAMP + WARMUP) * 1000;
const MEASUREMENT_END_MS = (RAMP + WARMUP + MEASUREMENT) * 1000;

const closedScenario = {
  executor: 'ramping-vus',
  startVUs: 0,
  stages: [
    { duration: `${RAMP}s`, target: USERS },
    { duration: `${WARMUP + MEASUREMENT}s`, target: USERS },
  ],
  gracefulRampDown: '10s',
  gracefulStop: '10s',
};

const openScenario = {
  executor: 'ramping-arrival-rate',
  startRate: 1,
  timeUnit: '1s',
  stages: [
    { duration: `${RAMP}s`, target: RATE_RPS },
    { duration: `${WARMUP + MEASUREMENT}s`, target: RATE_RPS },
  ],
  preAllocatedVUs: Math.min(USERS, 1000),
  maxVUs: USERS,
  gracefulStop: '10s',
};

export const options = {
  discardResponseBodies: true,
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  scenarios: { load: LOAD_MODEL === 'open' ? openScenario : closedScenario },
  // Limiares sempre verdadeiros: existem apenas para o k6 expor as metricas da fase "measurement" no resumo final.
  thresholds: {
    'http_req_duration{phase:measurement}': ['max>=0'],
    'http_req_failed{phase:measurement}': ['rate>=0'],
    'http_reqs{phase:measurement}': ['count>=0'],
  },
};

export default function () {
  const elapsed = Date.now() - exec.scenario.startTime;
  let phase = 'shutdown';
  if (elapsed < RAMP_END_MS) phase = 'ramp';
  else if (elapsed < WARMUP_END_MS) phase = 'warmup';
  else if (elapsed < MEASUREMENT_END_MS) phase = 'measurement';

  const id = 1 + Math.floor(Math.random() * TOTAL_CUSTOMERS);
  http.get(`${API_URL}/api/clients/${id}/summary?queries=${QUERIES}`, {
    tags: { name: 'summary', phase },
    timeout: '30s',
  });

  if (THINK_MS > 0) sleep(THINK_MS / 1000);
}

const metricValues = (data, name) => {
  const m = data.metrics[name];
  if (!m) return null;
  return m.values || m;
};

export function handleSummary(data) {
  const duration = metricValues(data, 'http_req_duration{phase:measurement}');
  const requests = metricValues(data, 'http_reqs{phase:measurement}');
  const failures = metricValues(data, 'http_req_failed{phase:measurement}');

  const total = requests ? requests.count : 0;
  const errorRate = failures ? (failures.rate !== undefined ? failures.rate : failures.value) : null;

  const summary = {
    runId: RUN_ID,
    endUtc: new Date().toISOString(),
    parameters: {
      apiUrl: API_URL, users: USERS, queries: QUERIES, loadModel: LOAD_MODEL,
      rateRps: RATE_RPS, thinkMs: THINK_MS, rampSec: RAMP, warmupSec: WARMUP, measurementSec: MEASUREMENT,
    },
    measurement: {
      requests: total,
      throughputRps: MEASUREMENT > 0 ? total / MEASUREMENT : null,
      errorRate: errorRate,
      latencyMs: duration,
    },
  };

  const line = duration
    ? `${RUN_ID}: ${total} req | ${(total / MEASUREMENT).toFixed(1)} req/s | erro ${(errorRate * 100).toFixed(3)}% | ` +
      `med ${duration.med.toFixed(1)} ms | p95 ${duration['p(95)'].toFixed(1)} ms | p99 ${duration['p(99)'].toFixed(1)} ms\n`
    : `${RUN_ID}: sem dados na fase de medicao\n`;

  return {
    [`${OUT_DIR}/${RUN_ID}.resumo.json`]: JSON.stringify(summary, null, 2),
    [`${OUT_DIR}/${RUN_ID}.k6-completo.json`]: JSON.stringify(data),
    stdout: line,
  };
}
