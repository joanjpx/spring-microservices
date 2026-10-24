import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    warmup: {
      executor: 'constant-vus',
      vus: 12,
      duration: '1m',
    },
    sample: {
      executor: 'constant-vus',
      vus: 12,
      duration: '3m',
      startTime: '1m',
    },
  },
};

export default function () {
  const response = http.get('http://gateway-service:8080/api/product');
  check(response, {
    'product endpoint returns 200': (result) => result.status === 200,
  });
}
