import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
  stages: [
    { duration: '10s', target: 50 }, // Ramp up to 50 virtual users
    { duration: '30s', target: 50 }, // Stay at 50 concurrent users for 30 seconds
    { duration: '10s', target: 0 },  // Ramp down gracefully to 0
  ],
  thresholds: {
    // 95% of redirect endpoints must resolve within 50ms, showing massive hit rates backed by Redis
    'http_req_duration{scenario:redirect}': ['p(95)<50'],
    // 95% of URL generation should complete under 200ms
    'http_req_duration': ['p(95)<200']
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // 1. Simulate a client writing a new URL
  const originalUrl = `https://example.com/target/${randomString(10)}`;
  const payload = JSON.stringify({ originalUrl: originalUrl });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const shortenRes = http.post(`${BASE_URL}/api/v1/urls`, payload, params);
  
  check(shortenRes, {
    'shorten status is 201': (r) => r.status === 201,
    'has short code': (r) => JSON.parse(r.body).code !== undefined,
  });

  if (shortenRes.status === 201) {
    const code = JSON.parse(shortenRes.body).code;

    // 2. Simulate heavy reads matching high availability patterns (ex: 5 quick clicks on the new URL)
    for (let i = 0; i < 5; i++) {
        // redirects: 0 explicitly tells k6 not to follow the 302 URL to example.com and just capture the header target
        const getRes = http.get(`${BASE_URL}/${code}`, { redirects: 0, tags: { scenario: 'redirect' } });
        
        check(getRes, {
            'redirect status is 302': (r) => r.status === 302,
        });
        
        // Slight micro-pause for user interaction simulation
        sleep(0.1); 
    }
  }

  // Brief pause before generating another URL
  sleep(1);
}
